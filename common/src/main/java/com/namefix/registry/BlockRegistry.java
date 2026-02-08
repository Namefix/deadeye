package com.namefix.registry;

import com.namefix.DeadeyeMod;
import com.namefix.block.TobaccoCropBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockRegistry {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(DeadeyeMod.MOD_ID, Registries.BLOCK);

	public static final RegistrySupplier<CropBlock> TOBACCO_CROP = registerBlock("tobacco_crop", TobaccoCropBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
	public static final RegistrySupplier<Block> WILD_TOBACCO = registerBlock("wild_tobacco", BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));

	public static void register() {
		BLOCKS.register();
	}

	public static RegistrySupplier<Block> registerBlock(String name, BlockBehaviour.Properties base) {
		return registerBlock(name, Block::new, base);
	}
	public static RegistrySupplier<Block> registerBlock(String name) {
		return registerBlock(name, Block::new);
	}
	public static <T extends Block> RegistrySupplier<T> registerBlock(String name, BlockFactory<T> factory) {
		return registerBlock(name, factory, BlockBehaviour.Properties.of());
	}
	public static <T extends Block> RegistrySupplier<T> registerBlock(String name, BlockFactory<T> factory, BlockBehaviour.Properties base) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, name));
		return BLOCKS.register(name, () -> factory.create(base));
	}
	public static Tuple<RegistrySupplier<Block>, RegistrySupplier<BlockItem>> registerBlockWithItem(String name) {
		return registerBlockWithItem(name, BlockBehaviour.Properties.of());
	}
	public static Tuple<RegistrySupplier<Block>, RegistrySupplier<BlockItem>> registerBlockWithItem(String name, BlockBehaviour.Properties base) {
		return registerBlockWithItem(name, Block::new, base);
	}
	public static <T extends Block> Tuple<RegistrySupplier<T>, RegistrySupplier<BlockItem>> registerBlockWithItem(String name, BlockFactory<T> factory) {
		return registerBlockWithItem(name, factory, BlockBehaviour.Properties.of());
	}
	public static <T extends Block> Tuple<RegistrySupplier<T>, RegistrySupplier<BlockItem>> registerBlockWithItem(String name, BlockFactory<T> factory, BlockBehaviour.Properties base) {
		return registerBlockWithItem(name, factory, base, (settings, block) -> new BlockItem(block.get(),settings));
	}
	public static <T extends Block, U extends BlockItem> Tuple<RegistrySupplier<T>, RegistrySupplier<U>> registerBlockWithItem(String name, BlockFactory<T> blockFactory, BlockItemFactory<T,U> itemFactory) {
		return registerBlockWithItem(name, blockFactory, BlockBehaviour.Properties.of(), itemFactory);
	}
	public static <T extends Block, U extends BlockItem> Tuple<RegistrySupplier<T>, RegistrySupplier<U>> registerBlockWithItem(String name, BlockFactory<T> blockFactory, BlockBehaviour.Properties base, BlockItemFactory<T,U> itemFactory) {
		RegistrySupplier<T> block = registerBlock(name, blockFactory, base);
		return new Tuple<>(
				block,
				ItemRegistry.registerItem(name, (settings) -> itemFactory.create(settings,block))
		);
	}

	@FunctionalInterface
	public interface BlockFactory<T extends Block> {
		T create(BlockBehaviour.Properties settings);
	}
	@FunctionalInterface
	public interface BlockItemFactory<T extends Block, U extends BlockItem> {
		U create(Item.Properties settings, RegistrySupplier<T> block);
	}
}
