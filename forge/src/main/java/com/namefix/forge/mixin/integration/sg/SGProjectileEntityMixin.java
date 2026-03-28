package com.namefix.forge.mixin.integration.sg;

import com.namefix.server.DeadeyeServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.item.GunItem;

@Mixin(value = ProjectileEntity.class, remap = false)
public class SGProjectileEntityMixin {
	@Inject(method = "getDirection", at = @At("HEAD"), cancellable = true, remap = false)
	private void deadeye$forceZeroSpread(LivingEntity shooter, ItemStack itemStack, GunItem gunItem, Gun gun, CallbackInfoReturnable<Vec3> cir) {
		if(!(shooter instanceof Player player)) return;
		if(!deadeye$isServerDeadeye(player)) return;
		cir.setReturnValue(Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot()));
	}

	private static boolean deadeye$isServerDeadeye(Player player) {
		return DeadeyeServer.DeadeyeStates.containsKey(player)
			|| DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(player.getUUID()));
	}
}
