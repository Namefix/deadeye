package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.JEGDeadeyeInteraction;
import com.namefix.deadeye.platform.JEGIntegration;
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
