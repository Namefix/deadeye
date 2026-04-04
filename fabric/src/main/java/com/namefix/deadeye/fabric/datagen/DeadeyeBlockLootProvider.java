package com.namefix.deadeye.fabric.datagen;

import com.namefix.deadeye.block.TobaccoCropBlock;
import com.namefix.deadeye.registry.BlockRegistry;
import com.namefix.deadeye.registry.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.concurrent.CompletableFuture;

public class DeadeyeBlockLootProvider extends FabricBlockLootTableProvider {
	protected DeadeyeBlockLootProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(dataOutput, registryLookup);
	}

	@Override
	public void generate() {
		LootItemBlockStatePropertyCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(BlockRegistry.TOBACCO_CROP.get())
						.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TobaccoCropBlock.AGE, TobaccoCropBlock.MAX_AGE));
		add(BlockRegistry.TOBACCO_CROP.get(), createCropDrops(BlockRegistry.TOBACCO_CROP.get(), ItemRegistry.TOBACCO.get(), ItemRegistry.TOBACCO_SEEDS.get(), builder));

		dropOther(BlockRegistry.WILD_TOBACCO.get(), ItemRegistry.TOBACCO.get());
	}
}
