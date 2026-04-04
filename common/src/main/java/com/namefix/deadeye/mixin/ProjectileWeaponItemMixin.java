package com.namefix.deadeye.mixin;

import com.namefix.deadeye.server.DeadeyeServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
	@ModifyArgs(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;shootProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V"))
	private void deadeye$modifyBowAccuracy(Args args) {
		LivingEntity owner = args.get(0);
		if(!(owner instanceof Player player)) return;

		if(DeadeyeServer.DeadeyeStates.containsKey(player)) {
			args.set(4, 0f);
			args.set(5, 0f);
		}
	}
}
