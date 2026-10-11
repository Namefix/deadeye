package com.namefix.deadeye.interactions;

import com.namefix.deadeye.data.DeadeyeTargetData;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.platform.IronsArtificeIntegration;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.util.Utils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class IronsArtificeDeadeyeInteraction extends AbstractDeadeyeInteraction {
	public IronsArtificeDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		super(state, player, itemStack);
		clientSideShoot = true;
		isGun = true;
	}

	@Override
	public void onEnterDeadeye() {
		IronsArtificeIntegration.refillAmmo(player, itemStack);
	}

	@Override
	public boolean preMark() {
		return IronsArtificeIntegration.getGunAmmo(itemStack) > state.targets.size();
	}

	@Override
	public void postMark() {
		if(player.level().isClientSide) return;
		if(IronsArtificeIntegration.getGunAmmo(itemStack) <= state.targets.size()) {
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
		}
	}

	@Override
	public boolean hasAmmo() {
		if(player == null) return false;
		return IronsArtificeIntegration.getGunAmmo(player.getMainHandItem()) > 0;
	}

	@Override
	public boolean preShot() {
		if(player == null) return false;
		ItemStack held = player.getMainHandItem();
		if(held.isEmpty() || held.getItem() != itemStack.getItem()) return false;
		if(!IronsArtificeIntegration.isGun(held)) return false;
		return IronsArtificeIntegration.isGunReady(held, player);
	}

	@Override
	public void shoot() {
		if(player == null || state.targets.isEmpty()) return;

		DeadeyeTargetData targetData = state.targets.get(0);
		if(targetData == null) return;
		var markPos = targetData.getMarkPosition(0.0f);

		Vec2 heading = Utils.getHeadingFromTarget(player, EntityAnchorArgument.Anchor.EYES, markPos);
		player.setXRot(heading.x);
		player.setYRot(heading.y);
		player.setYHeadRot(heading.y);
		player.setYBodyRot(heading.y);

		IronsArtificeIntegration.fireGun(player.getMainHandItem(), player, targetData.target);
	}

	@Override
	public void postShot(boolean hasMoreTargets) {

	}
}
