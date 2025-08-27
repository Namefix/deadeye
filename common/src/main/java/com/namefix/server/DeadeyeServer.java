package com.namefix.server;

import com.namefix.config.DeadeyeConfig;
import com.namefix.config.SyncedConfigCache;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.util.ServerUtils;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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

		NetworkManager.sendToPlayer((ServerPlayer) player, new DeadeyeStatePayload(false, PREVIOUS_TICK_RATE));
	}

	public static void enableDeadeye(Player player) {
		var level = player.level();
		DeadeyeStates.put(player, new PlayerDeadeyeState());

		if(ServerUtils.canModifyTickRate(level.getServer())) {
			if(DeadeyeStates.size() == 1) PREVIOUS_TICK_RATE = level.tickRateManager().tickrate();
			level.tickRateManager().setTickRate(DeadeyeConfig.Server.deadeyeTickRate);
		}

		NetworkManager.sendToPlayer((ServerPlayer) player, new DeadeyeStatePayload(true, PREVIOUS_TICK_RATE));
	}

	public static void handleDeadeyeRequest(RequestDeadeyePayload payload, NetworkManager.PacketContext packetContext) {
		Player player = packetContext.getPlayer();
		toggleDeadeye(player);
	}
}
