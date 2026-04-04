package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.SGDeadeyeInteraction;
import com.namefix.deadeye.platform.SGIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SGModIntegration extends BaseModIntegration {
	public static final SGModIntegration INSTANCE = new SGModIntegration();

	private SGModIntegration() {
		super("scguns");
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		return SGIntegration.isGun(itemStack);
	}

	@Override
	public AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		return new SGDeadeyeInteraction(state, player, itemStack);
	}
}
