package com.namefix.deadeye.interactions;

import com.namefix.deadeye.config.SyncedConfigCache;
import com.namefix.deadeye.data.DeadeyeTargetData;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.integration.pointblank.PointBlankPendingShotAim;
import com.namefix.deadeye.platform.PointBlankIntegration;
import com.namefix.deadeye.server.DeadeyeServer;
import com.namefix.deadeye.util.Utils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class PointBlankDeadeyeInteraction extends AbstractDeadeyeInteraction {
	private boolean fireOffhandNext = false;

	public PointBlankDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		super(state, player, itemStack);
		clientSideShoot = true;
		isGun = true;
	}

	@Override
	public void onEnterDeadeye() {
		PointBlankIntegration.clearPendingInstantReload(player);
		if(SyncedConfigCache.isInstantGunReload(player)) {
			PointBlankIntegration.refillAllGuns(player);
		}
	}

	@Override
	public void onExitDeadeye() {
		PointBlankIntegration.clearPendingInstantReload(player);
	}

	@Override
	public boolean supportsOffhand() {
		if(player == null) return false;
		return PointBlankIntegration.isDualWielding(player) || (PointBlankIntegration.isGun(player.getOffhandItem()) && PointBlankIntegration.getOperableGun(player) == player.getOffhandItem());
	}

	@Override
	public boolean isHoldingWeapon() {
		if(player == null) return false;
		if(PointBlankIntegration.isDualWielding(player)) return true;
		if(itemStack != null && !itemStack.isEmpty()) {
			if(player.getMainHandItem().getItem().equals(itemStack.getItem())) return true;
			if(supportsOffhand() && player.getOffhandItem().getItem().equals(itemStack.getItem())) return true;
		}
		ItemStack operable = PointBlankIntegration.getOperableGun(player);
		return !operable.isEmpty();
	}

	private boolean canGunShoot(ItemStack gun) {
		if(gun == null || gun.isEmpty() || !PointBlankIntegration.isGun(gun)) return false;
		if(PointBlankIntegration.isGunReloading(gun, player)) return false;
		return PointBlankIntegration.getGunAmmo(gun, player) > 0;
	}

	private ItemStack resolveNextGunToFire() {
		if(player == null) return ItemStack.EMPTY;
		if(PointBlankIntegration.isDualWielding(player)) {
			ItemStack mainStack = player.getMainHandItem();
			ItemStack offStack = player.getOffhandItem();
			boolean mainCanShoot = canGunShoot(mainStack);
			boolean offCanShoot = canGunShoot(offStack);

			if(mainCanShoot && offCanShoot) {
				return fireOffhandNext ? offStack : mainStack;
			} else if(mainCanShoot) {
				return mainStack;
			} else if(offCanShoot) {
				return offStack;
			}
			return ItemStack.EMPTY;
		}

		ItemStack operable = PointBlankIntegration.getOperableGun(player);
		if(!operable.isEmpty() && canGunShoot(operable)) {
			return operable;
		}
		if(canGunShoot(player.getMainHandItem())) {
			return player.getMainHandItem();
		}
		if(canGunShoot(player.getOffhandItem())) {
			return player.getOffhandItem();
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean hasAmmo() {
		return !resolveNextGunToFire().isEmpty();
	}

	private int getTotalAvailableAmmo() {
		return PointBlankIntegration.getTotalGunAmmo(player);
	}

	@Override
	public boolean preMark() {
		return getTotalAvailableAmmo() > state.targets.size();
	}

	@Override
	public void postMark() {
		if(player.level().isClientSide) return;
		if(getTotalAvailableAmmo() <= state.targets.size()) {
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
		}
	}

	@Override
	public boolean preShot() {
		ItemStack gunToFire = resolveNextGunToFire();
		if(gunToFire.isEmpty()) return false;
		return PointBlankIntegration.isGunReady(gunToFire, player);
	}

	@Override
	public void shoot() {
		PointBlankIntegration.clearPendingInstantReload(player);
		if(state.targets.isEmpty()) return;

		DeadeyeTargetData targetData = state.targets.get(0);
		if(targetData == null) return;
		var markPos = targetData.getMarkPosition(0.0f);

		Vec2 heading = Utils.getHeadingFromTarget(player, EntityAnchorArgument.Anchor.EYES, markPos);
		player.setXRot(heading.x);
		player.setYRot(heading.y);
		player.setYHeadRot(heading.y);
		player.setYBodyRot(heading.y);
		PointBlankPendingShotAim.put(player, heading.x, heading.y);

		ItemStack gunToFire = resolveNextGunToFire();
		if(gunToFire.isEmpty()) {
			return;
		}

		PointBlankIntegration.fireGun(gunToFire, player, targetData.target);

		if(PointBlankIntegration.isDualWielding(player)) {
			fireOffhandNext = (gunToFire != player.getOffhandItem());
		}
	}

	@Override
	public void postShot(boolean hasMoreTargets) {
	}
}
