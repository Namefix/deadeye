package com.namefix.fabric.datagen;

import com.namefix.block.TobaccoCropBlock;
import com.namefix.registry.BlockRegistry;
import com.namefix.registry.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

public class DeadeyeModelProvider extends FabricModelProvider {
	public DeadeyeModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
		blockModelGenerators.createCrossBlock(BlockRegistry.TOBACCO_CROP.get(), BlockModelGenerators.TintState.NOT_TINTED, TobaccoCropBlock.AGE, 0, 1, 2, 3);
		blockModelGenerators.createCrossBlock(BlockRegistry.WILD_TOBACCO.get(), BlockModelGenerators.TintState.NOT_TINTED);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerators) {
		itemModelGenerators.generateFlatItem(ItemRegistry.TOBACCO.get(), ModelTemplates.FLAT_ITEM);

		itemModelGenerators.generateFlatItem(ItemRegistry.CHEWING_TOBACCO.get(), ModelTemplates.FLAT_ITEM);
		itemModelGenerators.generateFlatItem(ItemRegistry.CIGARETTE.get(), ModelTemplates.FLAT_ITEM);
		itemModelGenerators.generateFlatItem(ItemRegistry.CIGAR.get(), ModelTemplates.FLAT_ITEM);

		itemModelGenerators.generateFlatItem(ItemRegistry.TOBACCO_WATER.get(), ModelTemplates.FLAT_ITEM);
		itemModelGenerators.generateFlatItem(ItemRegistry.POTENT_TOBACCO_WATER.get(), ModelTemplates.FLAT_ITEM);
		itemModelGenerators.generateFlatItem(ItemRegistry.SPECIAL_TOBACCO_WATER.get(), ModelTemplates.FLAT_ITEM);

		itemModelGenerators.generateFlatItem(ItemRegistry.SNAKE_OIL.get(), ModelTemplates.FLAT_ITEM);
		itemModelGenerators.generateFlatItem(ItemRegistry.POTENT_SNAKE_OIL.get(), ModelTemplates.FLAT_ITEM);
		itemModelGenerators.generateFlatItem(ItemRegistry.SPECIAL_SNAKE_OIL.get(), ModelTemplates.FLAT_ITEM);
	}
}
