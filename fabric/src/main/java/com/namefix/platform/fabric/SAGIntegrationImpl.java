package com.namefix.platform.fabric;

import com.namefix.config.DeadeyeConfig;
import net.elidhan.anim_guns.item.GunItem;
import net.elidhan.anim_guns.util.InventoryUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SAGIntegrationImpl {
	private SAGIntegrationImpl() {}

	public static boolean isLoaded() {
		return FabricLoader.getInstance().isModLoaded("anim_guns");
	}

	public static boolean isGun(ItemStack item) {
		if (!isLoaded()) return false;
		return item.getItem() instanceof GunItem;
	}

	public static int getGunAmmo(ItemStack item) {
		if(!(item.getItem() instanceof GunItem)) return -1;

		if(item.getOrCreateTag().contains("ammo"))
			return item.getOrCreateTag().getInt("ammo");
		return -1;
	}

	public static void refillAmmo(Player player, ItemStack item) {
		if(!isLoaded()) return;
		if(!(item.getItem() instanceof GunItem gun)) return;
		if(player.level().isClientSide) return;
		if(!DeadeyeConfig.Server.instantGunReload) return;

		CompoundTag tag = item.getOrCreateTag();
		int currentAmmo = Math.max(tag.getInt("ammo"), 0);
		int maxAmmo = Math.max(gun.getMagSize(), 0);
		int ammoNeeded = Math.max(maxAmmo - currentAmmo, 0);
		if(ammoNeeded <= 0) return;

		if(player.getAbilities().instabuild) {
			tag.putInt("ammo", maxAmmo);
			return;
		}

		int availableAmmo = Math.max(InventoryUtil.itemCountInInventory(player, gun.getAmmoItem()), 0);
		int ammoToRefill = Math.min(ammoNeeded, availableAmmo);
		if(ammoToRefill <= 0) return;

		tag.putInt("ammo", currentAmmo + ammoToRefill);
		InventoryUtil.removeItemFromInventory(player, gun.getAmmoItem(), ammoToRefill);
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!(item.getItem() instanceof GunItem gun)) return;

		gun.shoot((ServerPlayer) player, item);
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(!(item.getItem() instanceof GunItem gun)) return false;

		return !player.getCooldowns().isOnCooldown(gun) && getGunAmmo(item) > 0; // if gun is loaded
	}
}
