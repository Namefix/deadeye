package com.namefix.mixin;

import com.namefix.client.DeadeyeBowVisuals;
import com.namefix.client.DeadeyeClient;
import com.namefix.config.SyncedConfigCache;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "getTicksUsingItem", at = @At("RETURN"), cancellable = true)
	private void deadeye$getTicksUsingItem(CallbackInfoReturnable<Integer> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if(!(entity instanceof Player)) return;
		if(!entity.level().isClientSide) return;

		if(DeadeyeClient.DEADEYE_ENABLED && SyncedConfigCache.bowPullCompensation) {
			ItemStack useItem = entity.getUseItem();
			if(useItem.getItem() instanceof BowItem || useItem.getItem() instanceof CrossbowItem) {
				int originalTicks = cir.getReturnValue();
				float curTickRate = entity.level().tickRateManager().tickrate();
				float prevTickRate = DeadeyeClient.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeClient.PREVIOUS_TICK_RATE;
				cir.setReturnValue((int) (originalTicks * (prevTickRate / curTickRate)));
			}
		}
	}

	@Inject(method = "getUseItem", at = @At("RETURN"), cancellable = true)
	private void deadeye$forceUseItem(CallbackInfoReturnable<ItemStack> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if(!(entity instanceof Player player)) return;
		if(!entity.level().isClientSide) return;
		if(!DeadeyeBowVisuals.shouldForceVisualItemUse(player)) return;
		ItemStack forced = DeadeyeBowVisuals.getForcedUseItem(player);
		if(!forced.isEmpty()) cir.setReturnValue(forced);
	}

	@Inject(method = "getUseItemRemainingTicks", at = @At("RETURN"), cancellable = true)
	private void deadeye$forceRemainingTicks(CallbackInfoReturnable<Integer> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if(!(entity instanceof Player player)) return;
		if(!entity.level().isClientSide) return;
		int forced = DeadeyeBowVisuals.getForcedUseRemainingTicks(player);
		if(forced >= 0) cir.setReturnValue(forced);
	}
}
