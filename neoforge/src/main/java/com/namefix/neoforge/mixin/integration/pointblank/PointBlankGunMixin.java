package com.namefix.neoforge.mixin.integration.pointblank;

import com.namefix.client.DeadeyeClient;
import com.namefix.integration.pointblank.PointBlankPendingShotAim;
import com.namefix.platform.neoforge.PointBlankIntegrationImpl;
import com.namefix.server.DeadeyeServer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

	@Inject(method = "requestFireFromServer", at = @At("HEAD"))
	private void deadeye$syncRotationBeforeFire(GunClientState state, Player player, ItemStack itemStack, Entity target, CallbackInfo ci) {
		if(!DeadeyeClient.DEADEYE_ENABLED) return;
		PointBlankPendingShotAim.Aim pendingAim = PointBlankPendingShotAim.consume(player);
		if(pendingAim != null) {
			player.setXRot(pendingAim.xRot());
			player.setYRot(pendingAim.yRot());
			player.setYHeadRot(pendingAim.yRot());
			player.setYBodyRot(pendingAim.yRot());
		}
		if(player instanceof LocalPlayer localPlayer) {
			localPlayer.connection.send(new ServerboundMovePlayerPacket.Rot(localPlayer.getYRot(), localPlayer.getXRot(), localPlayer.onGround()));
		}
	}

	@Redirect(
		method = "requestFireFromServer",
		remap = true,
		at = @At(
			value = "INVOKE",
			target = "Lcom/vicmatskiv/pointblank/item/GunItem;adjustInaccuracy(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Z)D"
		)
	)
	private double deadeye$forceZeroInaccuracyClient(GunItem instance, Player player, ItemStack itemStack, boolean isAiming) {
		if(DeadeyeClient.DEADEYE_ENABLED) return 0.0;
		return adjustInaccuracy(player, itemStack, isAiming);
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
	private Vec3 deadeye$resolveServerHitScanVector(ServerPlayer player, float partialTick) {
		Vec3 baseView = player.getViewVector(partialTick);
		Vec3 markPos = DeadeyeServer.consumePointBlankShotMark(player);
		if(markPos == null) return baseView;
		Vec3 eye = player.getEyePosition();
		Vec3 desired = markPos.subtract(eye);
		if(desired.lengthSqr() < 1.0E-7) return baseView;
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

	@Inject(method = "processServerReloadResponse", at = @At("TAIL"))
	private void deadeye$syncHudAmmoNow(int correlationId, boolean success, ItemStack itemStack, GunClientState state, int ammo, FireModeInstance fireModeInstance, CallbackInfo ci) {
		if(!success || state == null || fireModeInstance == null) return;
		((PointBlankGunClientStateAccessor) state).deadeye$getAmmoCount().setAmmoCount(fireModeInstance, ammo);
	}
}