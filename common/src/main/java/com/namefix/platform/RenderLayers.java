package com.namefix.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;

public class RenderLayers {
	@ExpectPlatform
	public static void registerCutout(Block... blocks) {
		throw new AssertionError();
	}
}
