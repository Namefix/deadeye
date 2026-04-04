package com.namefix.deadeye.integration;

import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface DeadeyeIntegration {
	String id();
	void initialize();
	boolean isReady();
	boolean matches(ItemStack itemStack);
	AbstractDeadeyeInteraction createInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack);
}
