package com.namefix.integration;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.SGDeadeyeInteraction;
import com.namefix.platform.SGIntegration;
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
