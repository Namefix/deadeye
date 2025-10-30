package com.namefix.interactions;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.server.DeadeyeServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
		if(!player.isCreative() && player.getProjectile(itemStack).getCount() <= state.targets.size())
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
	}

	@Override
	public boolean preShoot() {
		return true;
	}

	@Override
	public void postShoot() {

	}
}
