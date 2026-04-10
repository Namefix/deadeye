package com.namefix.deadeye.fabric.client;

import com.namefix.deadeye.DeadeyeMod;
import com.namefix.deadeye.registry.KeybindRegistry;
import net.fabricmc.api.ClientModInitializer;

public final class DeadeyeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeybindRegistry.register();
        DeadeyeMod.initClient();
    }
}
