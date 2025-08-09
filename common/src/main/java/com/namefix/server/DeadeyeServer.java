package com.namefix.server;

import com.namefix.data.PlayerDeadeyeData;
import com.namefix.network.payload.RequestDeadeyePayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DeadeyeServer {
	public static Map<Player, PlayerDeadeyeData> DeadeyeData = new HashMap<>();

	public static void toggleDeadeye(Player player) {
		Level level = player.level();
		if(DeadeyeData.containsKey(player)) {
			DeadeyeData.remove(player);
			level.tickRateManager().setTickRate(20.0f);
		} else {
			DeadeyeData.put(player, new PlayerDeadeyeData());
			level.tickRateManager().setTickRate(5.0f);
		}
	}

	public static void handleDeadeyeRequest(RequestDeadeyePayload payload, NetworkManager.PacketContext packetContext) {
		Player player = packetContext.getPlayer();
		toggleDeadeye(player);
	}
}
