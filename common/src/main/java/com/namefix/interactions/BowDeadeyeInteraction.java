package com.namefix.interactions;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.server.DeadeyeServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class BowDeadeyeInteraction extends AbstractDeadeyeInteraction {

	public BowDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		super(state, player, itemStack);
	}

	@Override
	public boolean preMark() {
		return player.isCreative() || player.getProjectile(itemStack).getCount() > state.targets.size();
	}

	@Override
	public void postMark() {
		if(player.level().isClientSide) return;
		if(!player.isCreative() && player.getProjectile(itemStack).getCount() <= state.targets.size()) {
			state.markItem = itemStack;
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
		}
	}

	@Override
	public boolean preShot() {
		return true;
	}

	@Override
	public void shoot() {
		if(player.level().isClientSide) return;
		ProjectileWeaponItem bow = (ProjectileWeaponItem) itemStack.getItem();
		bow.shoot();
	}

	@Override
	public void postShot() {

	}
}
