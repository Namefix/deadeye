package com.namefix.forge.mixin.integration.jeg;

import com.namefix.client.DeadeyeClient;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ttv.migami.jeg.util.GunEnchantmentHelper;

@Mixin(value = GunEnchantmentHelper.class, remap = false)
public class JEGGunEnchantmentHelperMixin {
	@Inject(method = "getModifiedDrawTick", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void deadeye$forceNoDrawTick(ItemStack stack, int drawTick, CallbackInfoReturnable<Integer> cir) {
		if(Platform.getEnvironment() == Env.CLIENT && DeadeyeClient.DEADEYE_ENABLED) {
			cir.setReturnValue(0);
		}
	}
}
