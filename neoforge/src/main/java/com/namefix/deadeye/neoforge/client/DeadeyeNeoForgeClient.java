package com.namefix.deadeye.neoforge.client;

import com.namefix.deadeye.DeadeyeMod;
import com.namefix.deadeye.registry.KeybindRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class DeadeyeNeoForgeClient {
	public static void register(IEventBus modEventBus) {
		modEventBus.addListener(DeadeyeNeoForgeClient::onRegisterKeyMappings);
		modEventBus.addListener(DeadeyeNeoForgeClient::onClientSetup);
	}

	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(KeybindRegistry.DEADEYE_TOGGLE);
		event.register(KeybindRegistry.DEADEYE_MARK);
		event.register(KeybindRegistry.DEADEYE_SHOOT_TARGETS);
		event.register(KeybindRegistry.DEADEYE_INFO_SHOW);
	}

	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(DeadeyeMod::initClient);
	}
}

