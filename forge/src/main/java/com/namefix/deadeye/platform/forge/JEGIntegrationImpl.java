package com.namefix.deadeye.platform.forge;

import com.namefix.deadeye.config.DeadeyeConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import ttv.migami.jeg.common.AmmoContext;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.ReloadType;
import ttv.migami.jeg.client.handler.ShootingHandler;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.util.GunModifierHelper;

public class JEGIntegrationImpl {
	private JEGIntegrationImpl() {}

	public static boolean isLoaded() {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded("jeg");
	}

	public static boolean isGun(ItemStack item) {
		if (!isLoaded()) return false;
		return item.getItem() instanceof GunItem;
	}

	public static int getGunAmmo(ItemStack item) {
		if(!(item.getItem() instanceof GunItem)) return -1;

		if(item.getOrCreateTag().contains("AmmoCount"))
			return item.getOrCreateTag().getInt("AmmoCount");
		return -1;
	}

	public static void refillAmmo(Player player, ItemStack item) {
		if(!isLoaded()) return;
		if(!(item.getItem() instanceof GunItem gunItem)) return;
		if(player.level().isClientSide) return;
		if(!DeadeyeConfig.Server.instantGunReload) return;

		Gun gun = gunItem.getModifiedGun(item);
		if(gun == null) return;

		CompoundTag tag = item.getOrCreateTag();
		int currentAmmo = Math.max(tag.getInt("AmmoCount"), 0);
		int maxAmmo = Math.max(GunModifierHelper.getModifiedAmmoCapacity(item, gun), 0);
		if(maxAmmo <= 0) {
			maxAmmo = Math.max(gun.getReloads().getMaxAmmo(), 0);
		}

		int ammoNeeded = Math.max(maxAmmo - currentAmmo, 0);
		if(ammoNeeded <= 0) return;

		ReloadType reloadType = gun.getReloads().getReloadType();

		if(player.getAbilities().instabuild || item.getEnchantmentLevel(ModEnchantments.INFINITY.get()) > 0) {
			tag.putInt("AmmoCount", maxAmmo);
			return;
		}

		if(reloadType == ReloadType.SINGLE_ITEM) {
			ResourceLocation reloadItem = gun.getReloads().getReloadItem();
			if(reloadItem == null) return;

			AmmoContext ammoContext = Gun.findAmmo(player, reloadItem);
			ItemStack reloadStack = ammoContext.stack();
			if(reloadStack.isEmpty()) return;

			tag.putInt("AmmoCount", maxAmmo);
			reloadStack.shrink(1);
			if(ammoContext.container() != null) {
				ammoContext.container().setChanged();
			}
			return;
		}

		ResourceLocation ammoItem = gun.getProjectile().getItem();
		if(ammoItem == null) return;

		ItemStack[] availableStacks = Gun.findAmmoStack(player, ammoItem);
		int availableAmmo = Math.max(Gun.getTotalAmmoCount(availableStacks), 0);
		int ammoToRefill = Math.min(ammoNeeded, availableAmmo);
		if(ammoToRefill <= 0) return;

		int consumedAmmo = consumeAmmo(availableStacks, ammoToRefill);
		if(consumedAmmo <= 0) return;

		tag.putInt("AmmoCount", currentAmmo + consumedAmmo);
	}

	private static int consumeAmmo(ItemStack[] stacks, int amountNeeded) {
		int remaining = amountNeeded;

		for(ItemStack stack : stacks) {
			if(remaining <= 0) break;
			if(stack.isEmpty()) continue;

			int toShrink = Math.min(remaining, stack.getCount());
			stack.shrink(toShrink);
			remaining -= toShrink;
		}
		return amountNeeded - remaining;
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!(item.getItem() instanceof GunItem)) return;

		ShootingHandler.get().fire(player, item);
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(!(item.getItem() instanceof GunItem)) return false;

		return !player.getCooldowns().isOnCooldown(item.getItem()) && getGunAmmo(item) > 0;
	}
}
