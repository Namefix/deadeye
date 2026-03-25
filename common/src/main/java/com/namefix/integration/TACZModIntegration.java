package com.namefix.integration;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.TACZDeadeyeInteraction;
import com.namefix.platform.TACZIntegration;
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
