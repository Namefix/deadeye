package com.namefix.deadeye.interactions;

import com.namefix.deadeye.data.DeadeyeTargetData;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.platform.SAGIntegration;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.util.Utils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class SAGDeadeyeInteraction extends AbstractDeadeyeInteraction {
	public SAGDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		super(state, player, itemStack);
		clientSideShoot = false;
		isGun = true;
	}

	@Override
	public boolean preMark() {
		return SAGIntegration.getGunAmmo(itemStack) > state.targets.size();
	}

	@Override
	public void postMark() {
		if(player.level().isClientSide) return;
		if(SAGIntegration.getGunAmmo(itemStack) <= state.targets.size()) {
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
		}
	}

	@Override
	public boolean preShot() {
		ItemStack held = player.getMainHandItem();
		if(held.isEmpty() || held.getItem() != itemStack.getItem()) return false;
		if(!SAGIntegration.isGun(held)) return false;
		if(!SAGIntegration.isGunReady(held, player)) return false;
		return true;
	}

	@Override
	public void shoot() {
		if(state.targets.isEmpty()) return;

		DeadeyeTargetData targetData = state.targets.get(0);
		if(targetData == null) return;
		var markPos = targetData.getMarkPosition(0.0f);

		Vec2 heading = Utils.getHeadingFromTarget(player, EntityAnchorArgument.Anchor.EYES, markPos);
		player.setXRot(heading.x);
		player.setYRot(heading.y);
		player.setYHeadRot(heading.y);
		player.setYBodyRot(heading.y);

		SAGIntegration.fireGun(player.getMainHandItem(), player, targetData.target);
	}

	@Override
	public void postShot(boolean hasMoreTargets) {

	}
}
