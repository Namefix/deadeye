package com.namefix.fabric.client;

import com.namefix.DeadeyeMod;
import net.fabricmc.api.ClientModInitializer;

public final class DeadeyeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DeadeyeMod.initClient();
    }
}
