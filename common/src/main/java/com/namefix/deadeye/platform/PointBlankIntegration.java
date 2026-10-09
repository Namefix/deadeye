package com.namefix.deadeye.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PointBlankIntegration {
	private PointBlankIntegration() {}

	@ExpectPlatform
	public static boolean isLoaded() {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isGun(ItemStack item) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static int getGunAmmo(ItemStack item) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static int getGunAmmo(ItemStack item, Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void refillAmmo(Player player, ItemStack item) {throw new AssertionError();}

	@ExpectPlatform
	public static void fireGun(ItemStack item, Player player, Entity target) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isGunReady(ItemStack item, Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isDualWielding(Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static int getTotalGunAmmo(Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void refillAllGuns(Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static ItemStack getOperableGun(Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isGunReloading(ItemStack item, Player player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void clearPendingInstantReload(Player player) {
		throw new AssertionError();
	}
}
