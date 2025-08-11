package com.namefix;

import com.namefix.client.DeadeyeClient;
import com.namefix.network.DeadeyeNetwork;
import com.namefix.registry.KeybindRegistry;
import com.namefix.server.DeadeyeServer;
import com.namefix.shader.ShaderManager;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeadeyeMod {
    public static final String MOD_ID = "deadeye";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

        ShaderManager.initialize();
    }
}
