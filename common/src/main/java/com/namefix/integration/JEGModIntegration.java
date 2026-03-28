package com.namefix.integration;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.JEGDeadeyeInteraction;
import com.namefix.platform.JEGIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class JEGModIntegration extends BaseModIntegration {
	public static final JEGModIntegration INSTANCE = new JEGModIntegration();

	protected JEGModIntegration() {
		super("jeg");
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		return JEGIntegration.isGun(itemStack);
	}

	@Override
	public AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		return new JEGDeadeyeInteraction(state, player, itemStack);
	}
}
