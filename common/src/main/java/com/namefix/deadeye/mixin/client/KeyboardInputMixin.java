package com.namefix.deadeye.mixin.client;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.config.DeadeyeConfig;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
	@Inject(
			method = "tick",
			at = @At("TAIL")
	)
	private void deadeye$cancelPlayerInput(boolean bl, float f, CallbackInfo ci) {
		if(DeadeyeClient.DEADEYE_ENABLED && DeadeyeConfig.Client.preventMovement) {
			KeyboardInput input = (KeyboardInput) (Object) this;
			
			input.up = false;
			input.down = false;
			input.left = false;
			input.right = false;
			input.jumping = false;
			input.shiftKeyDown = false;
			input.forwardImpulse = 0.0f;
			input.leftImpulse = 0.0f;
		}
	}
}
