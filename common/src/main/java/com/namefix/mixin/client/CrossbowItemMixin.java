package com.namefix.mixin.client;

import com.namefix.client.DeadeyeClient;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@Inject(
		method = "getChargeDuration",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void deadeye$modifyChargeDurationClient(ItemStack itemStack, LivingEntity livingEntity, CallbackInfoReturnable<Integer> cir) {
		if(!(livingEntity instanceof Player player) || !player.level().isClientSide) return;
		if(!DeadeyeClient.DEADEYE_ENABLED) return;

		float curTickRate = player.level().tickRateManager().tickrate();
		float prevTickRate = DeadeyeClient.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeClient.PREVIOUS_TICK_RATE;
		cir.setReturnValue((int) (cir.getReturnValue() / (prevTickRate / curTickRate)));
	}
}
