package com.namefix.forge;

import com.namefix.DeadeyeMod;
import net.minecraftforge.fml.common.Mod;

@Mod(DeadeyeMod.MOD_ID)
public final class DeadeyeForge {
    public DeadeyeForge() {
        DeadeyeMod.init();
    }
}
