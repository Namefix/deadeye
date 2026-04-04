package com.namefix.deadeye.mixin.client;

import com.namefix.deadeye.client.DeadeyeBowVisuals;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void deadeye$correctForcedBowFov(CallbackInfoReturnable<Float> cir) {
		AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
		if(!DeadeyeBowVisuals.shouldForceVisualItemUse(player)) return;

		ItemStack useItem = player.getUseItem();
		if(useItem.isEmpty() || !useItem.is(Items.BOW)) return;

		int ticks = player.getTicksUsingItem();
		float drawProgress = Math.min((float) ticks / 20.0f, 1.0f);
		float drawPower = drawProgress * drawProgress;
		float bowFactor = 1.0f - drawPower * 0.15f;
		if(bowFactor <= 0.0f) {
			cir.setReturnValue(1.0f);
			return;
		}

		float original = cir.getReturnValue();
		float restored = original / bowFactor;
		cir.setReturnValue(Mth.clamp(restored, 0.1f, 1.5f));
	}
}
