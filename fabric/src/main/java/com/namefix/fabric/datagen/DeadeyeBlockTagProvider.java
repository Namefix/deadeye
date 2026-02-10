package com.namefix.fabric.datagen;

import com.namefix.registry.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class DeadeyeBlockTagProvider extends FabricTagProvider.BlockTagProvider {
	public DeadeyeBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	public static final TagKey<Block> CROPS = BlockTags.CROPS;
	public static final TagKey<Block> MAINTAINS_FARMLAND = BlockTags.MAINTAINS_FARMLAND;

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		getOrCreateTagBuilder(CROPS).add(BlockRegistry.TOBACCO_CROP.get());
		getOrCreateTagBuilder(MAINTAINS_FARMLAND).add(BlockRegistry.TOBACCO_CROP.get());
	}
}
