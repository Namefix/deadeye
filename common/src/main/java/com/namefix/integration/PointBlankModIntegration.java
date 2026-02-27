package com.namefix.integration;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.PointBlankDeadeyeInteraction;
import com.namefix.platform.PointBlankIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PointBlankModIntegration extends BaseModIntegration {
	public static final PointBlankModIntegration INSTANCE = new PointBlankModIntegration();

	private PointBlankModIntegration() {
		super("pointblank");
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		return PointBlankIntegration.isGun(itemStack);
	}

	@Override
	public AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		return new PointBlankDeadeyeInteraction(state, player, itemStack);
	}
}
