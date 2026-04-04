package com.namefix.deadeye.fabric.client;

import com.namefix.deadeye.DeadeyeMod;
import net.fabricmc.api.ClientModInitializer;

public final class DeadeyeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DeadeyeMod.initClient();
    }
}
