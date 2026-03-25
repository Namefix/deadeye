package com.namefix.forge.mixin.integration.tacz;

import com.namefix.server.DeadeyeServer;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModernKineticGunItem.class)
public class TACZModernKineticGunItemMixin {
	@ModifyVariable(method = "doBulletSpread", at = @At("HEAD"), ordinal = 1, argsOnly = true, remap = false)
	private float deadeye$modifySpread(float inaccuracy, ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
		if(DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(shooter.getUUID()))) return 0.0F;
		return inaccuracy;
	}
}
