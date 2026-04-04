package com.namefix.deadeye.interactions;

import com.namefix.deadeye.data.DeadeyeTargetData;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

public class ProjectileDeadeyeInteraction extends AbstractDeadeyeInteraction {
	private record ShotConfig(double speed, double gravity, float pitchOffset) {}

	public ProjectileDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		super(state, player, itemStack);
	}

	@Override
	public boolean preMark() {
		return player.isCreative() || itemStack.getCount() > state.targets.size();
	}

	@Override
	public void postMark() {
		if(player.level().isClientSide) return;
		if(!player.isCreative() && itemStack.getCount() <= state.targets.size())
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
	}

	@Override
	public boolean preShot() {
		return true;
	}

	@Override
	public void shoot() {
		if(player.level().isClientSide) return;
		if(state.targets.isEmpty()) return;

		DeadeyeTargetData targetData = state.targets.get(0);
		if(targetData == null) return;
		Entity target = targetData.target;
		if(target == null || target.isRemoved()) return;

		ItemStack held = player.getMainHandItem();
		if(held.isEmpty() || held.getItem() != itemStack.getItem()) return;
		Item item = held.getItem();
		if(!Utils.isProjectileItem(held)) return;

		ShotConfig config = getShotConfig(item);
		alignPlayerForShot(targetData, target, config.speed, config.gravity);
		if(config.pitchOffset != 0.0f) {
			player.setXRot(player.getXRot() - config.pitchOffset);
		}

		if(item instanceof TridentItem tridentItem) {
			int useDuration = tridentItem.getUseDuration(held);
			int timeLeft = Mth.clamp(useDuration - TridentItem.THROW_THRESHOLD_TIME, 0, useDuration);
			player.startUsingItem(InteractionHand.MAIN_HAND);
			tridentItem.releaseUsing(held, player.level(), player, timeLeft);
			player.stopUsingItem();
			return;
		}

		item.use(player.level(), player, InteractionHand.MAIN_HAND);
	}

	@Override
	public void postShot(boolean hasMoreTargets) {

	}

	private ShotConfig getShotConfig(Item item) {
		if(item instanceof TridentItem) return new ShotConfig(2.5d, 0.05d, 0.0f);
		if(item instanceof ExperienceBottleItem) return new ShotConfig(0.7d, 0.07d, -20.0f);
		if(item instanceof ThrowablePotionItem) return new ShotConfig(0.5d, 0.05d, -20.0f);
		if(item instanceof SnowballItem || item instanceof EggItem) return new ShotConfig(1.5d, 0.03d, 0.0f);
		return new ShotConfig(1.1d, 0.03d, 0.0f);
	}
}
