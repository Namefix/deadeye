package com.namefix.platform.neoforge;

import net.neoforged.fml.ModList;

public final class ModPlatformImpl {
	private ModPlatformImpl() {}

	public static boolean isModLoaded(String modId) {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded(modId);
	}
}
