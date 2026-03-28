package com.namefix.forge.mixin.integration.sg;

import com.namefix.client.DeadeyeClient;
import com.namefix.server.DeadeyeServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.item.animated.AnimatedGunItem;

@Mixin(value = AnimatedGunItem.class, remap = false)
public class SGAnimatedGunItemMixin {
	@Inject(method = "inventoryTick", at = @At("HEAD"), remap = false)
	private void deadeye$clearDrawState(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
		if(!(entity instanceof Player player)) return;
		if(!deadeye$isServerDeadeye(player)) return;

		CompoundTag tag = itemStack.getOrCreateTag();
		tag.putBoolean("IsDrawing", false);
		tag.putInt("DrawnTick", Integer.MAX_VALUE);
	}

	private static boolean deadeye$isServerDeadeye(Player player) {
		if(player.level().isClientSide) {
			return DeadeyeClient.DEADEYE_ENABLED;
		}

		return DeadeyeServer.DeadeyeStates.containsKey(player)
			|| DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(player.getUUID()));
	}
}
