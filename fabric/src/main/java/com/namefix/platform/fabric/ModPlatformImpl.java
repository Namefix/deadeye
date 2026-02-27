package com.namefix.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;

public final class ModPlatformImpl {
	private ModPlatformImpl() {}

	public static boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
}
