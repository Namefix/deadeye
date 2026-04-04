package com.namefix.deadeye.forge.mixin.integration.jeg;

import com.namefix.deadeye.client.DeadeyeClient;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
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
			target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V",
			remap = true
		),
		remap = false,
		require = 0
	)
	private void deadeye$compensateClientCooldown(ItemCooldowns cooldowns, Item item, int cooldown) {
		if(Platform.getEnvironment() != Env.CLIENT || !DeadeyeClient.DEADEYE_ENABLED) {
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
