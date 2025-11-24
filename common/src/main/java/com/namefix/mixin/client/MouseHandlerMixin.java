package com.namefix.mixin.client;

import com.namefix.client.DeadeyeClient;
import com.namefix.data.PlayerDeadeyeState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
	private void deadeye$cancelMouseMovement(CallbackInfo ci) {
		if(DeadeyeClient.DEADEYE_ENABLED && DeadeyeClient.DEADEYE_STATE.phase == PlayerDeadeyeState.Phase.SHOOTING) {
			ci.cancel();
		}
	}
}
