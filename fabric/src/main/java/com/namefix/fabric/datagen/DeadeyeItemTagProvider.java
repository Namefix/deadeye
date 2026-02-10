package com.namefix.fabric.datagen;

import com.namefix.registry.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class DeadeyeItemTagProvider extends FabricTagProvider.ItemTagProvider {
	public DeadeyeItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(output, completableFuture);
	}

	public static final TagKey<Item> VILLAGER_PLANTABLE_SEEDS = ItemTags.VILLAGER_PLANTABLE_SEEDS;

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		getOrCreateTagBuilder(VILLAGER_PLANTABLE_SEEDS).add(ItemRegistry.TOBACCO_SEEDS.get());
	}
}
