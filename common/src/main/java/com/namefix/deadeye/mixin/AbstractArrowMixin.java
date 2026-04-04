package com.namefix.deadeye.mixin;

import com.namefix.deadeye.server.DeadeyeServer;
import net.minecraft.world.entity.LivingEntity;
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
			at = @At("TAIL")
	)
	private void deadeye$preventInvTime(EntityHitResult entityHitResult, CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;

		if(arrow.getOwner() instanceof Player player) {
			boolean ownerInDeadeye = DeadeyeServer.DeadeyeStates.containsKey(player)
				|| DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(player.getUUID()));

			if(ownerInDeadeye && entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
				livingEntity.invulnerableTime = 0;
			}
		}
	}
}
