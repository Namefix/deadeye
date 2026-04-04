package com.namefix.deadeye.platform.forge;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SAGIntegrationImpl {
	private SAGIntegrationImpl() {}

	public static boolean isLoaded() {
		return false;
	}

	public static boolean isGun(ItemStack item) {
		return false;
	}

	public static int getGunAmmo(ItemStack item) {
		return 0;
	}

	public static void refillAmmo(Player player, ItemStack item) {
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		return false;
	}
}
