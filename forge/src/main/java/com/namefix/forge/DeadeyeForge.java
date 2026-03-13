package com.namefix.forge;

import com.namefix.DeadeyeMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(DeadeyeMod.MOD_ID)
public final class DeadeyeForge {
    public DeadeyeForge() {
        EventBuses.registerModEventBus(DeadeyeMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        DeadeyeMod.init();
    }
}
