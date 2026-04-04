package com.namefix.deadeye.mixin;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.server.DeadeyeServer;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
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
	private static void deadeye$modifyChargeDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
		boolean serverSide = !DeadeyeServer.DeadeyeStates.isEmpty();
		boolean clientSide = Platform.getEnvironment() == Env.CLIENT && DeadeyeClient.DEADEYE_ENABLED;
		if(!serverSide && !clientSide) return;

		float curTickRate = DeadeyeConfig.Server.deadeyeTickRate;
		if(curTickRate <= 0.0f) return;

		float prevTickRate;
		if(serverSide) {
			prevTickRate = DeadeyeServer.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeServer.PREVIOUS_TICK_RATE;
		} else {
			prevTickRate = DeadeyeClient.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeClient.PREVIOUS_TICK_RATE;
		}

		if(prevTickRate <= 0.0f) return;
		int adjusted = Math.max(1, (int) (cir.getReturnValue() / (prevTickRate / curTickRate)));
		cir.setReturnValue(adjusted);
	}
}
