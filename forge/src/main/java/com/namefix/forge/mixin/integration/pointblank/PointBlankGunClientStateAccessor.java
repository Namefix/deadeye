package com.namefix.forge.mixin.integration.pointblank;

import com.vicmatskiv.pointblank.item.AmmoCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.client.GunClientState", remap = false)
public interface PointBlankGunClientStateAccessor {
	@Accessor("ammoCount")
	AmmoCount deadeye$getAmmoCount();
}
