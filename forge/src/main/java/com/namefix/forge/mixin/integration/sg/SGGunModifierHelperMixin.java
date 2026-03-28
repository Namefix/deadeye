package com.namefix.forge.mixin.integration.sg;

import com.namefix.client.DeadeyeClient;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.util.GunModifierHelper;

@Mixin(value = GunModifierHelper.class, remap = false)
public class SGGunModifierHelperMixin {
	@Inject(method = "getModifiedDrawSpeed", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void deadeye$forceNoDrawSpeed(ItemStack stack, double drawSpeed, CallbackInfoReturnable<Double> cir) {
		if(DeadeyeClient.DEADEYE_ENABLED) {
			cir.setReturnValue(0D);
		}
	}
}
