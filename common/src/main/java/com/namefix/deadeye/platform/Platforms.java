package com.namefix.deadeye.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class Platforms {
	@ExpectPlatform
	public static RenderEvents getRenderEvents() {
		throw new AssertionError();
	}
}
