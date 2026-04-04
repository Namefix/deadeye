package com.namefix.deadeye;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.client.DeadeyeHud;
import com.namefix.deadeye.client.DeadeyeSound;
import com.namefix.deadeye.command.DeadeyeCommandRegistry;
import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.integration.IntegrationRegistry;
import com.namefix.deadeye.network.DeadeyeNetwork;
import com.namefix.deadeye.platform.RenderLayers;
import com.namefix.deadeye.registry.BlockRegistry;
import com.namefix.deadeye.registry.FeatureRegistry;
import com.namefix.deadeye.registry.ItemRegistry;
import com.namefix.deadeye.registry.KeybindRegistry;
import com.namefix.deadeye.registry.SoundEventRegistry;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.shader.ShaderManager;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
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
        SoundEventRegistry.register();
        BlockRegistry.register();
        ItemRegistry.register();
        FeatureRegistry.register();

        DeadeyeNetwork.initialize();

        PlayerEvent.PLAYER_JOIN.register(DeadeyeServer::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(DeadeyeServer::onPlayerQuit);
        EntityEvent.LIVING_DEATH.register(DeadeyeServer::onPlayerDeath);
        TickEvent.SERVER_LEVEL_PRE.register(DeadeyeServer::onTick);
        EntityEvent.LIVING_DEATH.register(DeadeyeServer::onEntityDeath);

        IntegrationRegistry.initialize();
    }

    // CLIENT INIT
    public static void initClient() {
        RenderLayers.registerCutout(BlockRegistry.TOBACCO_CROP.get(), BlockRegistry.WILD_TOBACCO.get());
        KeybindRegistry.register();
        DeadeyeNetwork.initializeClient();
        DeadeyeClient.initialize();
        DeadeyeSound.initialize();

        ClientRawInputEvent.KEY_PRESSED.register(DeadeyeClient::onKeyPressed);
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register(DeadeyeClient::onMouseClicked);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(DeadeyeClient::onQuit);
        ClientGuiEvent.RENDER_HUD.register(DeadeyeHud::render);

        ShaderManager.initialize();
    }
}
