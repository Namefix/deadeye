package com.namefix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.namefix.config.DeadeyeConfig;
import com.namefix.server.DeadeyeServer;
import com.namefix.util.TickManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
public class BowItemMixin {
	@ModifyArg(
			method = "releaseUsing",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"),
			index = 0
	)
	private int deadeye$adjustDrawTime(int drawTime, @Local(argsOnly = true) LivingEntity entity) {
		if(!(entity instanceof Player player) || player.level().isClientSide) return drawTime;

		if(DeadeyeServer.DeadeyeStates.containsKey(player) && DeadeyeConfig.Server.bowPullCompensation) {
			float curTickRate = TickManager.getTickRate(player.level());
			float prevTickRate = DeadeyeServer.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeServer.PREVIOUS_TICK_RATE;

			return (int) (drawTime * (prevTickRate / curTickRate));
		}
		return drawTime;
	}
}

