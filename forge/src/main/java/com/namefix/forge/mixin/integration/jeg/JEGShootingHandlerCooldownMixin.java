package com.namefix.forge.mixin.integration.jeg;

import com.namefix.client.DeadeyeClient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ttv.migami.jeg.client.handler.ShootingHandler;

@Mixin(value = ShootingHandler.class, remap = false)
public class JEGShootingHandlerCooldownMixin {
	@Redirect(
		method = "fire",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"
		),
		remap = false
	)
	private void deadeye$compensateClientCooldown(ItemCooldowns cooldowns, Item item, int cooldown, Player player, ItemStack itemStack) {
		if(!DeadeyeClient.DEADEYE_ENABLED) {
			cooldowns.addCooldown(item, cooldown);
			return;
		}

		float currentTickRate = DeadeyeClient.getEffectiveCurrentTickRate();
		float previousTickRate = DeadeyeClient.getEffectivePreviousTickRate();
		if(previousTickRate <= 0f) {
			cooldowns.addCooldown(item, cooldown);
			return;
		}

		int compensated = (int) Math.round(cooldown * (currentTickRate / previousTickRate));
		cooldowns.addCooldown(item, Math.max(compensated, 0));
	}
}
