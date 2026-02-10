package com.namefix.fabric.datagen;

import com.namefix.registry.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.data.recipes.ShapedRecipeBuilder.shaped;
import static net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.smelting;

public class DeadeyeRecipeProvider extends FabricRecipeProvider {
	public DeadeyeRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		shapeless(RecipeCategory.BREWING, ItemRegistry.TOBACCO_SEEDS.get())
				.requires(ItemRegistry.TOBACCO.get())
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		shapeless(RecipeCategory.BREWING, ItemRegistry.TOBACCO_WATER.get())
				.requires(Items.POTION)
				.requires(ItemRegistry.TOBACCO.get())
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		shapeless(RecipeCategory.BREWING, ItemRegistry.POTENT_TOBACCO_WATER.get())
				.requires(Items.POTION)
				.requires(ItemRegistry.TOBACCO.get())
				.requires(ItemRegistry.TOBACCO.get())
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		shapeless(RecipeCategory.BREWING, ItemRegistry.SPECIAL_TOBACCO_WATER.get())
				.requires(Items.POTION)
				.requires(ItemRegistry.TOBACCO.get())
				.requires(ItemRegistry.TOBACCO.get())
				.requires(ItemRegistry.TOBACCO.get())
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		shapeless(RecipeCategory.FOOD, ItemRegistry.CIGARETTE.get())
				.requires(Items.PAPER)
				.requires(ItemRegistry.TOBACCO.get())
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		shapeless(RecipeCategory.FOOD, ItemRegistry.CIGAR.get())
				.requires(Items.PAPER)
				.requires(ItemRegistry.TOBACCO.get())
				.requires(ItemRegistry.TOBACCO.get())
				.requires(ItemRegistry.TOBACCO.get())
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		// smelting

		smelting(Ingredient.of(ItemRegistry.TOBACCO.get()), RecipeCategory.BREWING, ItemRegistry.CHEWING_TOBACCO.get(), 0f, 1000)
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		smelting(Ingredient.of(ItemRegistry.TOBACCO_WATER.get()), RecipeCategory.BREWING, ItemRegistry.SNAKE_OIL.get(), 0.5f, 200)
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		smelting(Ingredient.of(ItemRegistry.POTENT_TOBACCO_WATER.get()), RecipeCategory.BREWING, ItemRegistry.POTENT_SNAKE_OIL.get(), 1.0f, 300)
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);

		smelting(Ingredient.of(ItemRegistry.SPECIAL_TOBACCO_WATER.get()), RecipeCategory.BREWING, ItemRegistry.SPECIAL_SNAKE_OIL.get(), 1.5f, 400)
				.unlockedBy(RecipeProvider.getHasName(ItemRegistry.TOBACCO.get()), has(ItemRegistry.TOBACCO.get()))
				.save(recipeOutput);
	}
}
