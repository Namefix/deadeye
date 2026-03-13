package com.namefix.registry;

import com.namefix.DeadeyeMod;
import com.namefix.world.feature.WildTobaccoFeature;
import com.namefix.world.feature.WildTobaccoFeatureConfig;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureRegistry {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(DeadeyeMod.MOD_ID, Registries.FEATURE);

    public static final RegistrySupplier<Feature<WildTobaccoFeatureConfig>> WILD_TOBACCO_FEATURE =
            FEATURES.register("wild_tobacco", () -> new WildTobaccoFeature(WildTobaccoFeatureConfig.CODEC));

    public static final ResourceKey<PlacedFeature> WILD_TOBACCO_PLACED_KEY = ResourceKey.create(
            Registries.PLACED_FEATURE,
            new ResourceLocation(DeadeyeMod.MOD_ID, "wild_tobacco")
    );

    public static void register() {
        FEATURES.register();
        BiomeModifications.addProperties(
                context -> context.hasTag(BiomeTags.IS_OVERWORLD),
                (context, properties) -> properties.getGenerationProperties().addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WILD_TOBACCO_PLACED_KEY
                )
        );
    }
}
