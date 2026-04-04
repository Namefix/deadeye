package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.SAGDeadeyeInteraction;
import com.namefix.deadeye.platform.SAGIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SAGModIntegration extends BaseModIntegration {
	public static final SAGModIntegration INSTANCE = new SAGModIntegration();

	private SAGModIntegration() {super("anim_guns");}

	@Override
	public boolean matches(ItemStack itemStack) {
		return SAGIntegration.isGun(itemStack);
	}

	@Override
	public AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		return new SAGDeadeyeInteraction(state, player, itemStack);
	}
}
