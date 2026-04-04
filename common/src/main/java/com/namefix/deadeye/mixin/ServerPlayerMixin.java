package com.namefix.deadeye.mixin;

import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.server.DeadeyeServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(
		method = "hurt",
		at = @At("HEAD"),
		cancellable = true
	)
	private void deadeye$cancelPlayerDamage(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;

		if(DeadeyeServer.DeadeyeStates.containsKey(player) && DeadeyeConfig.Server.deadeyeInvulnerability) {
			// Cancel event if the damage is not environmental
			if (!deadeye$isEnvironmentalDamage(damageSource)) {
				cir.setReturnValue(false);
			}
		}
	}

	@Unique
	private static boolean deadeye$isEnvironmentalDamage(DamageSource damageSource) {
		return damageSource.is(DamageTypes.IN_FIRE)
				|| damageSource.is(DamageTypes.CAMPFIRE)
				|| damageSource.is(DamageTypes.LIGHTNING_BOLT)
				|| damageSource.is(DamageTypes.ON_FIRE)
				|| damageSource.is(DamageTypes.LAVA)
				|| damageSource.is(DamageTypes.HOT_FLOOR)
				|| damageSource.is(DamageTypes.IN_WALL)
				|| damageSource.is(DamageTypes.CRAMMING)
				|| damageSource.is(DamageTypes.DROWN)
				|| damageSource.is(DamageTypes.STARVE)
				|| damageSource.is(DamageTypes.FALL)
				|| damageSource.is(DamageTypes.FLY_INTO_WALL)
				|| damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)
				|| damageSource.is(DamageTypes.FREEZE)
				|| damageSource.is(DamageTypes.STALAGMITE)
				|| damageSource.is(DamageTypes.FALLING_BLOCK)
				|| damageSource.is(DamageTypes.FALLING_ANVIL)
				|| damageSource.is(DamageTypes.FALLING_STALACTITE)
				|| damageSource.is(DamageTypes.FIREWORKS)
				|| damageSource.is(DamageTypes.OUTSIDE_BORDER)
				|| damageSource.is(DamageTypes.BAD_RESPAWN_POINT)
				|| damageSource.is(DamageTypes.EXPLOSION);
	}
}
