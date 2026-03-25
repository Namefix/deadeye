package com.namefix.forge.mixin.integration.tacz;

import com.namefix.server.DeadeyeServer;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InaccuracyType.class)
public class TACZInaccuracyTypeMixin {
	@Inject(method = "getInaccuracyType", at = @At("HEAD"), cancellable = true, remap = false)
	private static void deadeye$modifyInaccuracyType(LivingEntity livingEntity, CallbackInfoReturnable<InaccuracyType> cir) {
		if(DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(livingEntity.getUUID()))) cir.setReturnValue(InaccuracyType.AIM);
	}
}
