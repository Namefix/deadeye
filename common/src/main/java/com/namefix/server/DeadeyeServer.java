package com.namefix.server;

import com.namefix.config.DeadeyeConfig;
import com.namefix.config.SyncedConfigCache;
import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.data.PlayerDeadeyeState.Phase;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.network.payload.*;
import com.namefix.util.ServerUtils;
import com.namefix.util.Utils;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class DeadeyeServer {
	public static Map<Player, PlayerDeadeyeState> DeadeyeStates = new HashMap<>();
	public static float PREVIOUS_TICK_RATE = -1.0f;

	public static void onPlayerJoin(ServerPlayer serverPlayer) {
		serverPlayer.server.execute(() -> {
			SyncedConfigCache.sendConfigData(serverPlayer);
		});
	}

	public static void onPlayerQuit(ServerPlayer serverPlayer) {
		DeadeyeStates.remove(serverPlayer);
		if(DeadeyeStates.isEmpty()) disableDeadeye(serverPlayer);
	}

	public static EventResult onPlayerDeath(LivingEntity livingEntity, DamageSource damageSource) {
		if(!(livingEntity instanceof Player player)) return EventResult.pass();
		disableDeadeye(player);
		return EventResult.interruptDefault();
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
			level.tickRateManager().setTickRate(PREVIOUS_TICK_RATE);
			PREVIOUS_TICK_RATE = -1f;
		}

		NetworkManager.sendToPlayer((ServerPlayer) player, new DeadeyeStatePayload(false, PREVIOUS_TICK_RATE, Phase.IDLE.ordinal()));
	}

	public static void enableDeadeye(Player player) {
		var level = player.level();
		DeadeyeStates.put(player, new PlayerDeadeyeState());

		if(ServerUtils.canModifyTickRate(level.getServer())) {
			if(DeadeyeStates.size() == 1) PREVIOUS_TICK_RATE = level.tickRateManager().tickrate();
			level.tickRateManager().setTickRate(DeadeyeConfig.Server.deadeyeTickRate);
		}

		NetworkManager.sendToPlayer((ServerPlayer) player, new DeadeyeStatePayload(true, PREVIOUS_TICK_RATE, Phase.IDLE.ordinal()));
	}

	public static void updatePlayerPhase(ServerPlayer player, Phase phase) {
		if(!DeadeyeStates.containsKey(player)) return;
		PlayerDeadeyeState state = DeadeyeStates.get(player);
		state.phase = phase;
		NetworkManager.sendToPlayer(player, new DeadeyeStatePayload(true, PREVIOUS_TICK_RATE, phase.ordinal()));
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
		NetworkManager.sendToPlayer(player, RequestMarkPayload.forServerToClient(payload.markPos(), payload.entityId()));

		interaction.postMark();
	}

	public static void handleShotInfo(InformShotPayload payload, NetworkManager.PacketContext packetContext) {
		PlayerDeadeyeState state = DeadeyeStates.get(packetContext.getPlayer());
		if(state == null || state.phase != Phase.SHOOTING) {
			disableDeadeye(packetContext.getPlayer()); // Failsafe. Player probably went out of sync.
			return;
		}

		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(state, packetContext.getPlayer(), state.markItem);
		interaction.shoot();

		boolean hasMoreTargets = state.targets.size() > 1;
		state.targets.removeFirst();
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
