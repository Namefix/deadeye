package com.namefix;

import com.namefix.client.DeadeyeClient;
import com.namefix.network.DeadeyeNetwork;
import com.namefix.registry.KeybindRegistry;
import com.namefix.server.DeadeyeServer;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;

public final class DeadeyeMod {
    public static final String MOD_ID = "deadeye";

    // COMMON INIT
    public static void init() {
        DeadeyeNetwork.initialize();

        PlayerEvent.PLAYER_QUIT.register(DeadeyeServer::onPlayerQuit);
        EntityEvent.LIVING_DEATH.register(DeadeyeServer::onPlayerDeath);
    }

    // CLIENT INIT
    public static void initClient() {
        KeybindRegistry.register();
        DeadeyeNetwork.initializeClient();

        ClientRawInputEvent.KEY_PRESSED.register(DeadeyeClient::onKeyPressed);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(DeadeyeClient::onQuit);
    }
}
