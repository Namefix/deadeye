package com.namefix.forge;

import com.namefix.DeadeyeMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(DeadeyeMod.MOD_ID)
public final class DeadeyeForge {
    public static FMLJavaModLoadingContext MOD_LOADING_CONTEXT;

    public DeadeyeForge(FMLJavaModLoadingContext modLoadingContext) {
        MOD_LOADING_CONTEXT = modLoadingContext;
        EventBuses.registerModEventBus(DeadeyeMod.MOD_ID, modLoadingContext.getModEventBus());
        DeadeyeMod.init();
    }
}
