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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.util.GunModifierHelper;

@Mixin(value = AnimatedGunItem.class, remap = false)
public class SGAnimatedGunItemMixin {
	@Redirect(
		method = "handleDrawingState(Lnet/minecraft/nbt/CompoundTag;Lsoftware/bernie/geckolib/core/animation/AnimationController;Lnet/minecraft/world/item/ItemStack;)V",
		at = @At(
			value = "INVOKE",
			target = "Ltop/ribs/scguns/util/GunModifierHelper;getModifiedDrawSpeed(Lnet/minecraft/world/item/ItemStack;D)D"
		),
		remap = false
	)
	private double deadeye$skipDrawCooldown(ItemStack itemStack, double drawSpeed, CompoundTag tag, AnimationController<?> controller, ItemStack stack) {
		if(DeadeyeClient.DEADEYE_ENABLED) {
			return 0D;
		}
		return GunModifierHelper.getModifiedDrawSpeed(itemStack, drawSpeed);
	}

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
