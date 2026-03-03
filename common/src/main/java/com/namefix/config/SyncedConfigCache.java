package com.namefix.config;

import com.namefix.DeadeyeMod;
import com.namefix.network.payload.ConfigSyncPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;

public class SyncedConfigCache {
	public static boolean bowPullCompensation;
	public static boolean instantGunReload;

	public static void receiveConfigData(ConfigSyncPayload payload, NetworkManager.PacketContext context) {
		bowPullCompensation = payload.bowPullCompensation();
		instantGunReload = payload.instantGunReload();
		DeadeyeMod.LOGGER.debug("Received config data from the server");
	}

	public static void sendConfigData(ServerPlayer player) {
		NetworkManager.sendToPlayer(player, new ConfigSyncPayload(DeadeyeConfig.Server.bowPullCompensation, DeadeyeConfig.Server.instantGunReload));
		DeadeyeMod.LOGGER.debug("Sent config data to {}", player.getDisplayName());
	}

	public static void reloadAndSyncIntegratedServer(IntegratedServer integratedServer) {
		integratedServer.execute(() -> {
			for(ServerPlayer player : integratedServer.getPlayerList().getPlayers()) {
				sendConfigData(player);
			}
			DeadeyeMod.LOGGER.debug("Reload/sync button executed for integrated server. Synced {} players.", integratedServer.getPlayerList().getPlayerCount());
		});
	}
}
