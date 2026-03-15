package com.namefix.forge.client;

import com.namefix.DeadeyeMod;
import com.namefix.config.DeadeyeConfig;
import com.namefix.forge.DeadeyeForge;
import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import com.teamresourceful.resourcefulconfig.common.config.ResourcefulConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DeadeyeMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DeadeyeForgeClient {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		if(DeadeyeForge.MOD_LOADING_CONTEXT != null) {
			DeadeyeForge.MOD_LOADING_CONTEXT.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> {
					ResourcefulConfig config = DeadeyeMod.CONFIGURATOR.getConfig(DeadeyeConfig.class);
					if(config == null) return parent;
					return new ConfigScreen(parent, null, config);
				})
			);
		}
		DeadeyeMod.initClient();
	}
}

