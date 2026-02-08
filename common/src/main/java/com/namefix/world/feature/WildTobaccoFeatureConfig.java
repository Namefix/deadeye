package com.namefix.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record WildTobaccoFeatureConfig(
        int tries,
        int xzSpread,
        int ySpread,
        Holder<PlacedFeature> primaryFeature,
        Holder<PlacedFeature> secondaryFeature,
        Holder<PlacedFeature> floorFeature
) implements FeatureConfiguration {

    public static final Codec<WildTobaccoFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(WildTobaccoFeatureConfig::tries),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(4).forGetter(WildTobaccoFeatureConfig::xzSpread),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(WildTobaccoFeatureConfig::ySpread),
            PlacedFeature.CODEC.fieldOf("primary_feature").forGetter(WildTobaccoFeatureConfig::primaryFeature),
            PlacedFeature.CODEC.fieldOf("secondary_feature").forGetter(WildTobaccoFeatureConfig::secondaryFeature),
            PlacedFeature.CODEC.optionalFieldOf("floor_feature").forGetter(config -> Optional.ofNullable(config.floorFeature))
    ).apply(instance, (tries, xzSpread, ySpread, primary, secondary, floor) ->
            floor.map(floorFeature -> new WildTobaccoFeatureConfig(tries, xzSpread, ySpread, primary, secondary, floorFeature))
                    .orElseGet(() -> new WildTobaccoFeatureConfig(tries, xzSpread, ySpread, primary, secondary, null))
    ));
}
