package com.namefix.util;

import com.namefix.config.DeadeyeConfig;
import net.minecraft.server.MinecraftServer;

public class ServerUtils {
	public static boolean canModifyTickRate(MinecraftServer server) {
		return server.getPlayerCount() <= 1 || DeadeyeConfig.Server.forceTickRate;
	}
}
