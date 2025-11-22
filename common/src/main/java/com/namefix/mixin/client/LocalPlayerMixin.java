package com.namefix.mixin.client;

import com.namefix.client.DeadeyeBowVisuals;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Inject(method = "isUsingItem", at = @At("RETURN"), cancellable = true)
	private void deadeye$forceIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if(cir.getReturnValue()) return;
		if(DeadeyeBowVisuals.shouldForceVisualItemUse(player)) cir.setReturnValue(true);
	}

	@Inject(method = "getUsedItemHand", at = @At("RETURN"), cancellable = true)
	private void deadeye$forceHand(CallbackInfoReturnable<InteractionHand> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		InteractionHand hand = DeadeyeBowVisuals.getForcedUseHand(player);
		if(hand != null) cir.setReturnValue(hand);
	}
}
