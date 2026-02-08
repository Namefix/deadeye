package com.namefix.world.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WildTobaccoFeature extends Feature<WildTobaccoFeatureConfig> {
    public WildTobaccoFeature(Codec<WildTobaccoFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WildTobaccoFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        WildTobaccoFeatureConfig config = context.config();
        RandomSource random = context.random();

        int placed = 0;
        int tries = config.tries();
        int xzSpread = config.xzSpread() + 1;
        int ySpread = config.ySpread() + 1;

        Holder<PlacedFeature> floorFeature = config.floorFeature();
        if (floorFeature != null) {
            int floorTries = Math.max(2, tries / 2);
            int floorXzSpread = Math.max(1, xzSpread / 2);
            int floorYSpread = Math.max(1, ySpread / 2);
            for (int j = 0; j < floorTries; ++j) {
                BlockPos pos = origin.offset(
                        random.nextInt(floorXzSpread) - random.nextInt(floorXzSpread),
                        random.nextInt(floorYSpread) - random.nextInt(floorYSpread),
                        random.nextInt(floorXzSpread) - random.nextInt(floorXzSpread)
                );
                if (floorFeature.value().place(level, context.chunkGenerator(), random, pos)) {
                    ++placed;
                }
            }
        }

        int shorterXZ = Math.max(1, xzSpread - 2);
        placed += placeUnique(
            level,
            context,
            config.primaryFeature(),
            tries,
            shorterXZ,
            ySpread,
            origin,
            random
        );

        placed += placeUnique(
            level,
            context,
            config.secondaryFeature(),
            Math.max(1, tries / 2),
            xzSpread,
            ySpread,
            origin,
            random
        );

        return placed > 0;
    }

    private int placeUnique(
            WorldGenLevel level,
            FeaturePlaceContext<WildTobaccoFeatureConfig> context,
            Holder<PlacedFeature> feature,
            int count,
            int xzSpread,
            int ySpread,
            BlockPos origin,
            RandomSource random
    ) {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = count * 3;
        Set<BlockPos> used = new HashSet<>();

        while (placed < count && attempts < maxAttempts) {
            BlockPos pos = origin.offset(
                    random.nextInt(xzSpread) - random.nextInt(xzSpread),
                    random.nextInt(ySpread) - random.nextInt(ySpread),
                    random.nextInt(xzSpread) - random.nextInt(xzSpread)
            );
            attempts++;
            if (!used.add(pos)) {
                continue;
            }
            if (feature.value().place(level, context.chunkGenerator(), random, pos)) {
                placed++;
            }
        }

        return placed;
    }
}
