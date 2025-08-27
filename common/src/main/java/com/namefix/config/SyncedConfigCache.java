package com.namefix.config;

import com.namefix.network.payload.ConfigSyncPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;

public class SyncedConfigCache {
	public static boolean bowPullCompensation;

	public static void receiveConfigData(ConfigSyncPayload payload, NetworkManager.PacketContext context) {
		bowPullCompensation = payload.bowPullCompensation();
	}

	public static void sendConfigData(ServerPlayer player) {
		NetworkManager.sendToPlayer(player, new ConfigSyncPayload(DeadeyeConfig.Server.bowPullCompensation));
	}
}
