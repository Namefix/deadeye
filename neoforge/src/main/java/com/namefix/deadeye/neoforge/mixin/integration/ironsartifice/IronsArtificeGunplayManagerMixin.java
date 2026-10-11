package com.namefix.deadeye.neoforge.mixin.integration.ironsartifice;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.server.DeadeyeServer;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.redspace.irons_artifice.item.FireOutcome;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GunplayManager.class)
public class IronsArtificeGunplayManagerMixin {
	private static boolean deadeye$isServerDeadeye(Player player) {
		return DeadeyeServer.DeadeyeStates.containsKey(player) || DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(player.getUUID())) || DeadeyeServer.hasPendingIronsArtificeShotMark(player);
	}

	@Inject(method = "getSpreadForEntity", at = @At("HEAD"), cancellable = true)
	private static void deadeye$modifySpreadForEntity(ShotProfile shotProfile, Entity entity, CallbackInfoReturnable<Float> cir) {
		if(!(entity instanceof Player player)) return;
		if(player.level().isClientSide) {
			if(Platform.getEnvironment() == Env.CLIENT && DeadeyeClient.DEADEYE_ENABLED) {
				cir.setReturnValue(0.0f);
			}
		} else {
			if(deadeye$isServerDeadeye(player)) cir.setReturnValue(0.0f);
		}
	}

	@ModifyVariable(method = "tryFire", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static Vec3 deadeye$redirectTryFireDirection(Vec3 direction, LivingEntity living) {
		if(living instanceof Player player && !player.level().isClientSide) {
			Vec3 markPos = DeadeyeServer.peekIronsArtificeShotMark(player);
			if(markPos != null) {
				Vec3 eye = player.getEyePosition();
				Vec3 desired = markPos.subtract(eye);
				if(desired.lengthSqr() > 1.0E-7) {
					return desired.normalize();
				}
			}
		}
		return direction;
	}

	@Inject(method = "tryFire", at = @At("RETURN"))
	private static void deadeye$consumePendingShotOnReturn(LivingEntity living, Vec3 direction, CallbackInfoReturnable<FireOutcome> cir) {
		if(living instanceof Player player && !player.level().isClientSide) {
			DeadeyeServer.consumeIronsArtificeShotMark(player);
		}
	}
}
