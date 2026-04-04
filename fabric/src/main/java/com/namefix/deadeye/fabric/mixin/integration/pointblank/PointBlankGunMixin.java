package com.namefix.deadeye.fabric.mixin.integration.pointblank;

import com.namefix.deadeye.server.DeadeyeServer;
import net.minecraft.server.level.ServerPlayer;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.item.GunItem", remap = false)
public class PointBlankGunMixin {
	private static boolean deadeye$isServerDeadeye(Player player) {
		return DeadeyeServer.DeadeyeStates.containsKey(player)
			|| DeadeyeServer.DeadeyeStates.keySet().stream().anyMatch(p -> p.getUUID().equals(player.getUUID()));
	}

	@Shadow
	private double adjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming) {
		return 0.0;
	}

	@Inject(method = "adjustInaccuracy", at = @At("HEAD"), cancellable = true)
	private void deadeye$modifyAdjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming, CallbackInfoReturnable<Double> cir) {
		if(!player.level().isClientSide) {
			if(deadeye$isServerDeadeye(player) || DeadeyeServer.hasPendingPointBlankShotMark(player)) {
				cir.setReturnValue(0.0);
			}
		}
	}

	@Redirect(
		method = "handleClientHitScanFireRequest",
		remap = true,
		at = @At(
			value = "INVOKE",
			target = "Lcom/vicmatskiv/pointblank/item/GunItem;adjustInaccuracy(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Z)D"
		)
	)
	private double deadeye$forceZeroInaccuracyServerHitScan(GunItem instance, Player player, ItemStack itemStack, boolean isAiming) {
		if(deadeye$isServerDeadeye(player) || DeadeyeServer.hasPendingPointBlankShotMark(player)) return 0.0;
		return adjustInaccuracy(player, itemStack, isAiming);
	}

	@Redirect(
		method = "handleClientHitScanFireRequest",
		remap = true,
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
		)
	)
	private Vec3 deadeye$useMarkedVectorForServerHitScan(ServerPlayer player, float partialTick) {
		Vec3 markPos = DeadeyeServer.consumePointBlankShotMark(player);
		if(markPos == null) {
			return player.getViewVector(partialTick);
		}
		Vec3 eye = player.getEyePosition();
		Vec3 desired = markPos.subtract(eye);
		if(desired.lengthSqr() < 1.0E-7) {
			return player.getViewVector(partialTick);
		}
		return desired.normalize();
	}

	@Redirect(
		method = "handleClientProjectileFireRequest",
		remap = true,
		at = @At(
			value = "INVOKE",
			target = "Lcom/vicmatskiv/pointblank/item/GunItem;adjustInaccuracy(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Z)D"
		)
	)
	private double deadeye$forceZeroInaccuracyServerProjectile(GunItem instance, Player player, ItemStack itemStack, boolean isAiming) {
		if(deadeye$isServerDeadeye(player) || DeadeyeServer.hasPendingPointBlankShotMark(player)) return 0.0;
		return adjustInaccuracy(player, itemStack, isAiming);
	}

}
