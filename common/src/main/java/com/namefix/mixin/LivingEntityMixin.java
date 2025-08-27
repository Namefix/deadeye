package com.namefix.mixin;

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
		if(!(entity instanceof Player player)) return;

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
}
