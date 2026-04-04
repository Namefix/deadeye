package com.namefix.deadeye.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DeadeyeDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(DeadeyeBlockLootProvider::new);
        pack.addProvider(DeadeyeModelProvider::new);
        pack.addProvider(DeadeyeRecipeProvider::new);
        pack.addProvider(DeadeyeItemTagProvider::new);
        pack.addProvider(DeadeyeBlockTagProvider::new);
    }
}
