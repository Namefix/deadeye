package com.namefix.deadeye.mixin;

import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.util.TickManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BowItem.class)
public class BowItemMixin {
	@Redirect(
			method = "releaseUsing",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F")
	)
	private float deadeye$adjustPowerForTime(int drawTime, ItemStack itemStack, Level level, LivingEntity entity, int timeLeft) {
		if(!(entity instanceof Player player) || player.level().isClientSide) return BowItem.getPowerForTime(drawTime);

		if(DeadeyeServer.DeadeyeStates.containsKey(player) && DeadeyeConfig.Server.bowPullCompensation) {
			float curTickRate = TickManager.getTickRate(player.level());
			float prevTickRate = DeadeyeServer.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeServer.PREVIOUS_TICK_RATE;
			int adjustedDrawTime = (int) (drawTime * (prevTickRate / curTickRate));
			return BowItem.getPowerForTime(adjustedDrawTime);
		}
		return BowItem.getPowerForTime(drawTime);
	}
}

