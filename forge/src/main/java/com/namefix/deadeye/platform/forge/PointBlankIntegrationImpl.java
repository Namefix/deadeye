package com.namefix.deadeye.platform.forge;

import com.namefix.deadeye.config.SyncedConfigCache;
import com.namefix.deadeye.forge.mixin.integration.pointblank.PointBlankGunClientStateAccessor;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PointBlankIntegrationImpl {
	private static final Set<UUID> PENDING_INSTANT_RELOAD = ConcurrentHashMap.newKeySet();

	private PointBlankIntegrationImpl() {}

	public static boolean isLoaded() {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded("pointblank");
	}

	public static boolean isGun(ItemStack item) {
		if (!isLoaded()) return false;
		return item.getItem() instanceof GunItem;
	}

	public static int getGunAmmo(ItemStack item) {
		if (!isLoaded()) return 0;
		if (!(item.getItem() instanceof GunItem)) return 0;
		return GunItem.getAmmo(item, GunItem.getFireModeInstance(item));
	}

	public static void refillAmmo(Player player, ItemStack item) {
		if(!isLoaded()) return;
		if(!(item.getItem() instanceof GunItem gun)) return;
		if(!player.level().isClientSide) return;
		if(!SyncedConfigCache.instantGunReload) return;
		FireModeInstance fireMode = GunItem.getFireModeInstance(item);
		UUID playerId = player.getUUID();
		PENDING_INSTANT_RELOAD.add(playerId);
		if(!gun.requestReloadFromServer(player, item)) {
			PENDING_INSTANT_RELOAD.remove(playerId);
			return;
		}

		GunClientState state = GunClientState.getState(player, item, player.getInventory().selected, false);
		if(state != null && fireMode != null) {
			int maxAmmo = gun.getMaxAmmoCapacity(item, fireMode);
			setClientAmmoCount(state, fireMode, maxAmmo);
		}
	}

	public static void setClientAmmoCount(GunClientState state, FireModeInstance fireMode, int ammo) {
		if(state == null || fireMode == null) return;

		if(state instanceof PointBlankGunClientStateAccessor accessor) {
			accessor.deadeye$getAmmoCount().setAmmoCount(fireMode, ammo);
			return;
		}

		try {
			Field ammoCountField = state.getClass().getDeclaredField("ammoCount");
			ammoCountField.setAccessible(true);
			Object ammoCount = ammoCountField.get(state);
			if(ammoCount == null) return;

			Method setAmmoCount = ammoCount.getClass().getMethod("setAmmoCount", FireModeInstance.class, int.class);
			setAmmoCount.invoke(ammoCount, fireMode, ammo);
		} catch (Throwable ignored) {}
	}

	public static boolean consumePendingInstantReload(Player player) {
		if(player == null) return false;
		return PENDING_INSTANT_RELOAD.remove(player.getUUID());
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!isLoaded()) return;
		if(!(item.getItem() instanceof GunItem gun)) return;
		gun.tryFire((LocalPlayer) player, item, target);
	}

	public static FireMode getGunFiremode(ItemStack item) {
		if(!isLoaded()) return null;
		if(!(item.getItem() instanceof GunItem)) return null;
		return GunItem.getFireModeInstance(item).getType();
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(!(item.getItem() instanceof GunItem gun)) return false;
		if(!player.level().isClientSide) return false;
		GunClientState state = GunClientState.getState(player, item, player.getInventory().selected, false);

		FireMode mode = getGunFiremode(item);
		if(mode == FireMode.AUTOMATIC && state.isIdle()) return true;
		else if(mode == FireMode.AUTOMATIC) return state.isFiring();
		else return state.isIdle();
	}
}