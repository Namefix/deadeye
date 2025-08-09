package com.namefix.neoforge.client;

import com.namefix.DeadeyeMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = DeadeyeMod.MOD_ID, value = Dist.CLIENT)
public class DeadeyeNeoForgeClient {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		DeadeyeMod.initClient();
	}
}

