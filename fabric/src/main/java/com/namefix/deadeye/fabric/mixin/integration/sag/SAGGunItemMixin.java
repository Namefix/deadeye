package com.namefix.deadeye.fabric.mixin.integration.sag;


import com.llamalad7.mixinextras.sugar.Local;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.util.TickManager;
import net.elidhan.anim_guns.item.GunItem;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GunItem.class)
public class SAGGunItemMixin {
	@ModifyArg(
			method = "shoot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"
			)
	)
	private int deadeye$modifyCooldown(int cooldown, @Local(argsOnly = true) ServerPlayer player) {
		float curTickRate = TickManager.getTickRate(player.level());
		float prevTickRate = DeadeyeServer.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeServer.PREVIOUS_TICK_RATE;
		return (int) (cooldown * (curTickRate / prevTickRate));
	}

	@ModifyArg(
			method = "shoot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/elidhan/anim_guns/util/BulletUtil;horiSpread(Lnet/minecraft/world/entity/player/Player;D)Lnet/minecraft/world/phys/Vec3;"
			),
			index = 1
	)
	private double deadeye$zeroSpreadX(double spreadX, @Local(argsOnly = true) ServerPlayer player) {
		if (DeadeyeServer.DeadeyeStates.containsKey(player)) return 0D;
		return spreadX;
	}

	@ModifyArg(
			method = "shoot",
			at = @At(
					value = "INVOKE",
					target = "Lnet/elidhan/anim_guns/util/BulletUtil;vertiSpread(Lnet/minecraft/world/entity/player/Player;D)Lnet/minecraft/world/phys/Vec3;"
			),
			index = 1
	)
	private double deadeye$zeroSpreadY(double spreadY, @Local(argsOnly = true) ServerPlayer player) {
		if (DeadeyeServer.DeadeyeStates.containsKey(player)) return 0D;
		return spreadY;
	}
}
