package com.namefix.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ModPlatform {
	private ModPlatform() {}

	@ExpectPlatform
	public static boolean isModLoaded(String modId) {
		throw new AssertionError();
	}
}
