package com.namefix.deadeye.mixin;

import com.namefix.deadeye.data.PlayerSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(
			method = "isSleepingLongEnough",
			at = @At("HEAD")
	)
	private void deadeye$sleepCoreReward(CallbackInfoReturnable<Boolean> cir) {
		Player player = (Player) (Object) this;
		if(player.getSleepTimer() >= 100f) {
			if (player.level().isClientSide) return;
			PlayerSavedData.addDeadeyeCore((ServerPlayer) player, 20f, true);
		}
	}
}
