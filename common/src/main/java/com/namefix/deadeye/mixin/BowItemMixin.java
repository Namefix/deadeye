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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public class BowItemMixin {
	@ModifyVariable(
			method = "releaseUsing",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0
	)
	private int deadeye$adjustTimeLeft(int timeLeft, ItemStack itemStack, Level level, LivingEntity entity) {
		if (!(entity instanceof Player player) || level.isClientSide) return timeLeft;

		if (DeadeyeServer.isPlayerInDeadeye(player) && DeadeyeConfig.Server.bowPullCompensation) {
			float curTickRate = TickManager.getTickRate(level);
			float prevTickRate = DeadeyeServer.PREVIOUS_TICK_RATE == -1f ? curTickRate : DeadeyeServer.PREVIOUS_TICK_RATE;
			int duration = ((BowItem) (Object) this).getUseDuration(itemStack);
			int drawTime = duration - timeLeft;
			int adjustedDrawTime = (int) (drawTime * (prevTickRate / curTickRate));
			return duration - adjustedDrawTime;
		}
		return timeLeft;
	}
}

