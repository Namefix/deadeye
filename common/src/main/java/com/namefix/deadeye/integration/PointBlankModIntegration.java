package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.PointBlankDeadeyeInteraction;
import com.namefix.deadeye.platform.PointBlankIntegration;
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
