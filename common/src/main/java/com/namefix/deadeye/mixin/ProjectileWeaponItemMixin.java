package com.namefix.deadeye.mixin;

import com.namefix.deadeye.server.DeadeyeServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Projectile.class)
public class ProjectileWeaponItemMixin {
	@ModifyVariable(
			method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"),
			ordinal = 4,
			argsOnly = true
	)
	private float deadeye$modifyProjectileInaccuracy(float inaccuracy, Entity owner) {
		if(!(owner instanceof Player player)) return inaccuracy;
		if(!DeadeyeServer.DeadeyeStates.containsKey(player)) return inaccuracy;

		Projectile projectile = (Projectile) (Object) this;
		if(projectile instanceof AbstractArrow || projectile instanceof FireworkRocketEntity) {
			return 0.0f;
		}

		return inaccuracy;
	}
}
