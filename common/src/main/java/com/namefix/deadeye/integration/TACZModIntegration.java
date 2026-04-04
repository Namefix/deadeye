package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.TACZDeadeyeInteraction;
import com.namefix.deadeye.platform.TACZIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TACZModIntegration extends BaseModIntegration {
	public static final TACZModIntegration INSTANCE = new TACZModIntegration();

	private TACZModIntegration() {super("tacz");}

	@Override
	public boolean matches(ItemStack itemStack) {
		return TACZIntegration.isGun(itemStack);
	}

	@Override
	public AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		return new TACZDeadeyeInteraction(state, player, itemStack);
	}
}
