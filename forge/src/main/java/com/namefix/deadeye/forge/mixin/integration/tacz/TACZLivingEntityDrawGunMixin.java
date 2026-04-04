package com.namefix.deadeye.forge.mixin.integration.tacz;

import com.namefix.deadeye.client.DeadeyeClient;
import com.tacz.guns.entity.shooter.LivingEntityDrawGun;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityDrawGun.class)
public class TACZLivingEntityDrawGunMixin {
	@Inject(method = "getDrawCoolDown", at = @At("HEAD"), cancellable = true, remap = false)
	private void deadeye$modifyDrawCooldown(CallbackInfoReturnable<Long> cir) {
		if(Platform.getEnvironment() == Env.CLIENT && DeadeyeClient.DEADEYE_ENABLED) cir.setReturnValue(0L);
	}
}
