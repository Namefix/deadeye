package com.namefix.server;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
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

	public static void onPlayerQuit(ServerPlayer serverPlayer) {
		disableDeadeye(serverPlayer);
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

		if(DeadeyeStates.isEmpty()) {
			level.tickRateManager().setTickRate(20.0f);
		}

		NetworkManager.sendToPlayer((ServerPlayer) player, new DeadeyeStatePayload(false));
	}

	public static void enableDeadeye(Player player) {
		var level = player.level();
		DeadeyeStates.put(player, new PlayerDeadeyeState());
		level.tickRateManager().setTickRate(5.0f);

		NetworkManager.sendToPlayer((ServerPlayer) player, new DeadeyeStatePayload(true));
	}

	public static void handleDeadeyeRequest(RequestDeadeyePayload payload, NetworkManager.PacketContext packetContext) {
		Player player = packetContext.getPlayer();
		toggleDeadeye(player);
	}
}
