package com.namefix.deadeye.neoforge;

import com.namefix.deadeye.neoforge.client.DeadeyeNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;

import com.namefix.deadeye.DeadeyeMod;

@Mod(DeadeyeMod.MOD_ID)
public final class DeadeyeNeoForge {
    public DeadeyeNeoForge() {
        DeadeyeMod.init();

		if (FMLEnvironment.dist == Dist.CLIENT) {
			DeadeyeNeoForgeClient.register(ModLoadingContext.get().getActiveContainer().getEventBus());
		}
    }
}
