package com.namefix.fabric.mixin.integration.pointblank;

import com.namefix.client.DeadeyeClient;
import com.namefix.platform.fabric.PointBlankIntegrationImpl;
import com.namefix.server.DeadeyeServer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.item.GunItem", remap = false)
public class PointBlankGunMixin {
	@Inject(method = "adjustInaccuracy", at = @At("HEAD"), cancellable = true)
	private void deadeye$modifyAdjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming, CallbackInfoReturnable<Double> cir) {
		if(!player.level().isClientSide) {
			if(DeadeyeServer.DeadeyeStates.containsKey(player)) {
				cir.setReturnValue(0.0);
			}
		} else {
			if(DeadeyeClient.DEADEYE_ENABLED) {
				cir.setReturnValue(0.0);
			}
		}
	}

	@Inject(method = "getDrawCooldownDuration", at = @At("HEAD"), cancellable = true)
	private void deadeye$modifyDrawCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Long> cir) {
		if(DeadeyeClient.DEADEYE_ENABLED) {
			cir.setReturnValue(0L);
		}
	}

	@Inject(method = "getReloadingCooldownTime", at = @At("HEAD"), cancellable = true)
	private void deadeye$modifyReloadCooldownDuration(GunItem.ReloadPhase phase, LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Long> cir) {
		if(DeadeyeClient.DEADEYE_ENABLED && player instanceof Player localPlayer && PointBlankIntegrationImpl.consumePendingInstantReload(localPlayer)) {
			cir.setReturnValue(0L);
		}
	}

	@Inject(method = "processServerReloadResponse", at = @At("TAIL"))
	private void deadeye$syncHudAmmoNow(int correlationId, boolean success, ItemStack itemStack, GunClientState state, int ammo, FireModeInstance fireModeInstance, CallbackInfo ci) {
		if(!success || state == null || fireModeInstance == null) return;
		((PointBlankGunClientStateAccessor) state).deadeye$getAmmoCount().setAmmoCount(fireModeInstance, ammo);
	}
}
