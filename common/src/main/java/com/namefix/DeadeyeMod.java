package com.namefix;

import com.namefix.client.DeadeyeClient;
import com.namefix.network.DeadeyeNetwork;
import com.namefix.registry.KeybindRegistry;
import dev.architectury.event.events.client.ClientRawInputEvent;

public final class DeadeyeMod {
    public static final String MOD_ID = "deadeye";

    // COMMON INIT
    public static void init() {
        DeadeyeNetwork.initialize();
    }

    // CLIENT INIT
    public static void initClient() {
        KeybindRegistry.register();
        DeadeyeNetwork.initializeClient();

        ClientRawInputEvent.KEY_PRESSED.register(DeadeyeClient::onKeyPressed);
    }
}
