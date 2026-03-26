package com.namefix.integration;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.SAGDeadeyeInteraction;
import com.namefix.platform.SAGIntegration;
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
