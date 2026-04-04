package com.namefix.deadeye.mixin;

import com.namefix.deadeye.server.DeadeyeServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {
	// prevents invulnerability ticks when the entity gets hit by arrows in Dead Eye
	@Inject(
			method = "onHitEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;doKnockback(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)V"
			)
	)
	private void deadeye$preventInvTime(EntityHitResult entityHitResult, CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;

		if(arrow.getOwner() instanceof Player player) {
			if(DeadeyeServer.DeadeyeStates.containsKey(player)) entityHitResult.getEntity().invulnerableTime = 0;
		}
	}
}
