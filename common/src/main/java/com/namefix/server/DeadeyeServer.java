package com.namefix.server;

import com.namefix.config.DeadeyeConfig;
import com.namefix.config.SyncedConfigCache;
import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.data.PlayerDeadeyeState.Phase;
import com.namefix.data.PlayerSavedData;
import com.namefix.data.StateManager;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.JEGDeadeyeInteraction;
import com.namefix.interactions.SGDeadeyeInteraction;
import com.namefix.interactions.SAGDeadeyeInteraction;
import com.namefix.interactions.TACZDeadeyeInteraction;
import com.namefix.network.DeadeyeNetwork;
import com.namefix.network.payload.*;
import com.namefix.platform.JEGIntegration;
import com.namefix.platform.SGIntegration;
import com.namefix.platform.SAGIntegration;
import com.namefix.platform.TACZIntegration;
import com.namefix.util.ServerUtils;
import com.namefix.util.TickManager;
import com.namefix.util.Utils;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DeadeyeServer {
	public static Map<Player, PlayerDeadeyeState> DeadeyeStates = new HashMap<>();
	private static final Map<UUID, Deque<Vec3>> POINTBLANK_PENDING_SHOT_MARKS = new HashMap<>();
	public static float PREVIOUS_TICK_RATE = -1.0f;

	public static void onTick(ServerLevel serverLevel) {
		Iterator<Map.Entry<Player, PlayerDeadeyeState>> iterator = DeadeyeStates.entrySet().iterator();
		List<Player> toRemove = new ArrayList<>();

		while(iterator.hasNext()) {
			Map.Entry<Player, PlayerDeadeyeState> entry = iterator.next();
			ServerPlayer player = (ServerPlayer) entry.getKey();
			PlayerDeadeyeState state = entry.getValue();
			PlayerSavedData data = StateManager.getPlayerState(player);

			if(state.phase != Phase.SHOOTING) {
				PlayerSavedData.addDeadeyeXP(player, 0.02f);
				PlayerSavedData.subDeadeyeTotal(player, data.deadeyeConsumeRate);
				if(data.deadeyeMeter == 0 && data.deadeyeCore == 0) {
					if(state.phase == Phase.MARKED) {
						updatePlayerPhase(player, Phase.SHOOTING);
					} else {
						toRemove.add(player);
					}
				}
			} else {
				if(state.markItem != null && !state.markItem.getItem().equals(player.getMainHandItem().getItem())) {
					toRemove.add(player);
				}
			}

			if(state.phase == Phase.SHOOTING) {
				if(state.targets.isEmpty()) toRemove.add(player);
			}
		}

		for (Player player : toRemove) {
			disableDeadeye(player);
		}

		// Natural regeneration
		if(DeadeyeConfig.Server.naturalDeadeyeRegeneration > 0.0f) {
			for(ServerPlayer player : serverLevel.players()) {
				if(DeadeyeStates.containsKey(player)) continue;
				PlayerSavedData.addDeadeyeMeter(player, DeadeyeConfig.Server.naturalDeadeyeRegeneration, true);
			}
		}
	}

	public static void onPlayerJoin(ServerPlayer serverPlayer) {
		PlayerSavedData data = StateManager.getPlayerState(serverPlayer);

		serverPlayer.server.execute(() -> {
			SyncedConfigCache.sendConfigData(serverPlayer);
			updatePlayerLevelData(serverPlayer, data);
			updatePlayerMeterData(serverPlayer, data);
		});
	}

	public static void onPlayerQuit(ServerPlayer serverPlayer) {
		DeadeyeStates.remove(serverPlayer);
		POINTBLANK_PENDING_SHOT_MARKS.remove(serverPlayer.getUUID());
		if(DeadeyeStates.isEmpty()) disableDeadeye(serverPlayer);
	}

	public static EventResult onPlayerDeath(LivingEntity livingEntity, DamageSource damageSource) {
		if(!(livingEntity instanceof Player player)) return EventResult.pass();
		disableDeadeye(player);
		return EventResult.interruptDefault();
	}

	public static EventResult onEntityDeath(LivingEntity livingEntity, DamageSource damageSource) {
		if(damageSource.getEntity() instanceof ServerPlayer player) {
			PlayerSavedData data = StateManager.getPlayerState(player);
			if(DeadeyeStates.containsKey(player)) {
				PlayerSavedData.addDeadeyeXP(player, 0.5f);
			} else {
				PlayerSavedData.addDeadeyeMeter(player, Math.max(data.deadeyeKillReward*(data.deadeyeCore/10), 0.5f), true);
			}
		}
		return EventResult.interruptDefault();
	}

	public static void drinkTonic(ServerPlayer player, int tonicLevel) {
		PlayerSavedData data = StateManager.getPlayerState((ServerPlayer) player);
		if(tonicLevel <= 0 || data.deadeyeSkill <= 0) return;
		PlayerSavedData.setDeadeyeMeter(player, PlayerSavedData.getMaxMeter(data, tonicLevel));
	}

	public static void toggleDeadeye(Player player) {
		if(DeadeyeStates.containsKey(player)) {
			disableDeadeye(player);
		} else {
			enableDeadeye(player);
		}
	}

	public static void disableDeadeye(Player player) {
		var level = player.level();
		DeadeyeStates.remove(player);

		if(DeadeyeStates.isEmpty() && PREVIOUS_TICK_RATE >= 0) {
			TickManager.setTickRate(level, PREVIOUS_TICK_RATE);
			PREVIOUS_TICK_RATE = -1f;
		}

		DeadeyeStatePayload payload = new DeadeyeStatePayload(false, PREVIOUS_TICK_RATE, Phase.IDLE.ordinal());
		var buffer = DeadeyeNetwork.createBuffer();
		payload.write(buffer);
		DeadeyeNetwork.sendToPlayer((ServerPlayer) player, DeadeyeNetwork.DEADEYE_STATE, buffer);
	}

	public static void enqueuePointBlankShotMark(Player player, Vec3 markPos) {
		POINTBLANK_PENDING_SHOT_MARKS.computeIfAbsent(player.getUUID(), ignored -> new ArrayDeque<>()).addLast(markPos);
	}

	public static Vec3 consumePointBlankShotMark(ServerPlayer player) {
		Deque<Vec3> queue = POINTBLANK_PENDING_SHOT_MARKS.get(player.getUUID());
		if(queue == null || queue.isEmpty()) return null;
		Vec3 mark = queue.pollFirst();
		if(queue.isEmpty()) {
			POINTBLANK_PENDING_SHOT_MARKS.remove(player.getUUID());
		}
		return mark;
	}

	public static boolean hasPendingPointBlankShotMark(Player player) {
		Deque<Vec3> queue = POINTBLANK_PENDING_SHOT_MARKS.get(player.getUUID());
		return queue != null && !queue.isEmpty();
	}

	public static void enableDeadeye(Player player) {
		PlayerSavedData data = StateManager.getPlayerState((ServerPlayer) player);
		if(data.deadeyeSkill <= 0 || data.deadeyeMeter + data.deadeyeCore <= 0f) return;

		var level = player.level();
		POINTBLANK_PENDING_SHOT_MARKS.remove(player.getUUID());
		DeadeyeStates.put(player, new PlayerDeadeyeState());

		if(ServerUtils.canModifyTickRate(level.getServer())) {
			if(DeadeyeStates.size() == 1) PREVIOUS_TICK_RATE = TickManager.getTickRate(level);
			TickManager.setTickRate(level, DeadeyeConfig.Server.deadeyeTickRate);
		}

		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(DeadeyeStates.get(player), player, player.getMainHandItem());
		if(interaction != null && interaction.isGun) {
			if(interaction instanceof TACZDeadeyeInteraction) TACZIntegration.refillAmmo(player, player.getMainHandItem());
			if(interaction instanceof SAGDeadeyeInteraction) SAGIntegration.refillAmmo(player, player.getMainHandItem());
			if(interaction instanceof JEGDeadeyeInteraction) JEGIntegration.refillAmmo(player, player.getMainHandItem());
			if(interaction instanceof SGDeadeyeInteraction) SGIntegration.refillAmmo(player, player.getMainHandItem());
		}

		DeadeyeStatePayload payload = new DeadeyeStatePayload(true, PREVIOUS_TICK_RATE, Phase.IDLE.ordinal());
		var buffer = DeadeyeNetwork.createBuffer();
		payload.write(buffer);
		DeadeyeNetwork.sendToPlayer((ServerPlayer) player, DeadeyeNetwork.DEADEYE_STATE, buffer);
	}

	public static void updatePlayerPhase(ServerPlayer player, Phase phase) {
		if(!DeadeyeStates.containsKey(player)) return;
		PlayerDeadeyeState state = DeadeyeStates.get(player);
		state.phase = phase;
		DeadeyeStatePayload payload = new DeadeyeStatePayload(true, PREVIOUS_TICK_RATE, phase.ordinal());
		var buffer = DeadeyeNetwork.createBuffer();
		payload.write(buffer);
		DeadeyeNetwork.sendToPlayer(player, DeadeyeNetwork.DEADEYE_STATE, buffer);
	}

	public static void updatePlayerLevelData(ServerPlayer player, PlayerSavedData data) {
		LevelDataPayload payload = new LevelDataPayload(data.deadeyeSkill, data.deadeyeLevel, data.deadeyeXp);
		var buffer = DeadeyeNetwork.createBuffer();
		payload.write(buffer);
		DeadeyeNetwork.sendToPlayer(player, DeadeyeNetwork.LEVEL_DATA, buffer);
	}

	public static void updatePlayerMeterData(ServerPlayer player, PlayerSavedData data) {
		MeterDataPayload payload = new MeterDataPayload(data.deadeyeMeter, data.deadeyeCore);
		var buffer = DeadeyeNetwork.createBuffer();
		payload.write(buffer);
		DeadeyeNetwork.sendToPlayer(player, DeadeyeNetwork.METER_DATA, buffer);
	}

	public static void handleDeadeyeRequest(RequestDeadeyePayload payload, NetworkManager.PacketContext packetContext) {
		Player player = packetContext.getPlayer();
		toggleDeadeye(player);
	}

	public static void handleMarkRequest(RequestMarkPayload payload, NetworkManager.PacketContext packetContext) {
		ServerPlayer player = (ServerPlayer) packetContext.getPlayer();
		if(!DeadeyeStates.containsKey(player)) return;
		PlayerDeadeyeState state = DeadeyeStates.get(player);
		if(state.targets.size() > 50) return;

		ItemStack markItem = player.getMainHandItem();
		if(markItem.isEmpty()) return;
		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(state, player, markItem);
		if(interaction == null) return;
		if(!interaction.preMark()) return;

		Entity entity = player.level().getEntity(payload.entityId());
		if(entity == null) return;
		if(!(entity instanceof LivingEntity target)) return;

		updatePlayerPhase(player, Phase.MARKED);
		Vec3 pos = new Vec3(payload.markPos());
		state.targets.add(new DeadeyeTargetData(entity, pos));
		state.markItem = player.getMainHandItem().copy();
		RequestMarkPayload responsePayload = RequestMarkPayload.forServerToClient(payload.markPos(), payload.entityId());
		var responseBuffer = DeadeyeNetwork.createBuffer();
		responsePayload.write(responseBuffer);
		DeadeyeNetwork.sendToPlayer(player, DeadeyeNetwork.REQUEST_MARK_S2C, responseBuffer);

		interaction.postMark();
	}

	public static void handleShotInfo(InformShotPayload payload, NetworkManager.PacketContext packetContext) {
		ServerPlayer player = (ServerPlayer) packetContext.getPlayer();
		PlayerDeadeyeState state = DeadeyeStates.get(packetContext.getPlayer());
		if(state == null || state.phase != Phase.SHOOTING) {
			disableDeadeye(packetContext.getPlayer()); // Failsafe. Player probably went out of sync.
			return;
		}

		enqueuePointBlankShotMark(player, new Vec3(payload.targetPos()));

		if(state.targets.isEmpty()) {
			disableDeadeye(packetContext.getPlayer());
			return;
		}

		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(state, packetContext.getPlayer(), state.markItem);
		if(!interaction.clientSideShoot) interaction.shoot();

		state.targets.remove(0);
		boolean hasMoreTargets = !state.targets.isEmpty();

		interaction.postShot(hasMoreTargets);
		if(!hasMoreTargets) {
			disableDeadeye(packetContext.getPlayer());
		}
	}

	public static void handleShootingPhase(InformShootingPhasePayload payload, NetworkManager.PacketContext packetContext) {
		if(!DeadeyeStates.containsKey(packetContext.getPlayer())) return;
		PlayerDeadeyeState state = DeadeyeStates.get(packetContext.getPlayer());
		if(state.phase != Phase.MARKED) return;
		state.phase = Phase.SHOOTING;
	}
}
