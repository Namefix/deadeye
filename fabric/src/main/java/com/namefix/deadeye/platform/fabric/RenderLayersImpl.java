package com.namefix.deadeye.platform.fabric;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class RenderLayersImpl {
	public static void registerCutout(Block... blocks) {
		for (Block block : blocks) {
			BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
		}
	}
}
