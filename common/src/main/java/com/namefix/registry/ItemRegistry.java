package com.namefix.registry;

import com.namefix.DeadeyeMod;
import com.namefix.item.TonicItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

public class ItemRegistry {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(DeadeyeMod.MOD_ID, Registries.ITEM);

	public static final RegistrySupplier<Item> TOBACCO = registerItem("tobacco", Item::new, CreativeModeTabs.INGREDIENTS);
	public static final RegistrySupplier<Item> TOBACCO_SEEDS = registerItem("tobacco_seeds", BlockRegistry.TOBACCO_CROP, CreativeModeTabs.INGREDIENTS);

	public static final RegistrySupplier<Item> SNAKE_OIL = registerItem("snake_oil", item -> new TonicItem(item, 1), CreativeModeTabs.FOOD_AND_DRINKS);
	public static final RegistrySupplier<Item> POTENT_SNAKE_OIL = registerItem("potent_snake_oil", item -> new TonicItem(item, 2), CreativeModeTabs.FOOD_AND_DRINKS);
	public static final RegistrySupplier<Item> SPECIAL_SNAKE_OIL = registerItem("special_snake_oil", item -> new TonicItem(item, 3), CreativeModeTabs.FOOD_AND_DRINKS);

	public static void register() {
		ITEMS.register();
	}

	@FunctionalInterface
	public interface ItemFactory<T extends Item> {
		T create(Item.Properties settings);
	}
	public static RegistrySupplier<Item> registerItem(String name) {
		return registerItem(name, Item::new);
	}
	public static RegistrySupplier<Item> registerItem(String name, ItemFactory<Item> factory, ResourceKey<CreativeModeTab> tab) {
		return ITEMS.register(name, () -> factory.create(new Item.Properties().arch$tab(tab)));
	}
	public static RegistrySupplier<Item> registerItem(String name, RegistrySupplier<? extends Block> block, ResourceKey<CreativeModeTab> tab) {
		return ITEMS.register(name, () -> new ItemNameBlockItem(block.get(), new Item.Properties().arch$tab(tab)));
	}
	public static <T extends Item> RegistrySupplier<T> registerItem(String name, ItemFactory<T> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, name));
		return ITEMS.register(name, () -> factory.create(new Item.Properties()));
	}
}
