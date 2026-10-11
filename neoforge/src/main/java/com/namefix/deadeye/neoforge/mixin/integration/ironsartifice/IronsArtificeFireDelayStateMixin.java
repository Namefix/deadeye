package com.namefix.deadeye.neoforge.mixin.integration.ironsartifice;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.server.DeadeyeServer;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.redspace.irons_artifice.item.FireDelayState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireDelayState.class)
public class IronsArtificeFireDelayStateMixin {
	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private static void deadeye$modifyIsActive(LivingEntity living, ItemStack heldGun, CallbackInfoReturnable<Boolean> cir) {
		if(!(living instanceof Player player)) return;
		if(player.level().isClientSide) {
			if(Platform.getEnvironment() == Env.CLIENT && DeadeyeClient.DEADEYE_ENABLED) {
				cir.setReturnValue(false);
			}
		} else {
			if(DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(player.getUUID()))
				|| DeadeyeServer.hasPendingIronsArtificeShotMark(player)) {
				cir.setReturnValue(false);
			}
		}
	}
}
