package com.namefix.mixin.client;

import com.namefix.client.DeadeyeClient;
import com.namefix.data.PlayerDeadeyeState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "getAttackStrengthScale", at = @At("RETURN"), cancellable = true)
	private void deadeye$keepAttackStrengthWhileShooting(float partialTicks, CallbackInfoReturnable<Float> cir) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) return;
		if(mc.player != (Object) this) return;
		if(!DeadeyeClient.DEADEYE_ENABLED) return;
		if(DeadeyeClient.DEADEYE_STATE.phase != PlayerDeadeyeState.Phase.SHOOTING) return;
		cir.setReturnValue(1.0f);
	}
}
