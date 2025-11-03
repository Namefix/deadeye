package com.namefix.interactions;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.server.DeadeyeServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ProjectileDeadeyeInteraction extends AbstractDeadeyeInteraction {
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

	}

	@Override
	public void postShot() {

	}
}
