package com.namefix;

import com.namefix.client.DeadeyeClient;
import com.namefix.client.DeadeyeHud;
import com.namefix.command.DeadeyeCommandRegistry;
import com.namefix.config.DeadeyeConfig;
import com.namefix.network.DeadeyeNetwork;
import com.namefix.registry.KeybindRegistry;
import com.namefix.server.DeadeyeServer;
import com.namefix.shader.ShaderManager;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeadeyeMod {
    public static final String MOD_ID = "deadeye";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Configurator CONFIGURATOR = new Configurator(MOD_ID);

    // COMMON INIT
    public static void init() {
        DeadeyeMod.LOGGER.info("Initializing Dead Eye. People don't forget, nothing gets forgiven.");
        CONFIGURATOR.register(DeadeyeConfig.class);
        DeadeyeCommandRegistry.initialize();

        DeadeyeNetwork.initialize();

        PlayerEvent.PLAYER_JOIN.register(DeadeyeServer::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(DeadeyeServer::onPlayerQuit);
        EntityEvent.LIVING_DEATH.register(DeadeyeServer::onPlayerDeath);
    }

    // CLIENT INIT
    public static void initClient() {
        KeybindRegistry.register();
        DeadeyeNetwork.initializeClient();
        DeadeyeClient.initialize();

        ClientRawInputEvent.KEY_PRESSED.register(DeadeyeClient::onKeyPressed);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(DeadeyeClient::onQuit);
        ClientGuiEvent.RENDER_HUD.register(DeadeyeHud::render);

        ShaderManager.initialize();
    }
}
