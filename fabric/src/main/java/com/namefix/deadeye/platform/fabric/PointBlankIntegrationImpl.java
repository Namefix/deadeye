package com.namefix.deadeye.platform.fabric;

import com.namefix.deadeye.config.SyncedConfigCache;
import com.namefix.deadeye.fabric.mixin.integration.pointblank.PointBlankGunClientStateAccessor;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PointBlankIntegrationImpl {
	private static final Map<UUID, AtomicInteger> PENDING_INSTANT_RELOAD = new ConcurrentHashMap<>();

	private PointBlankIntegrationImpl() {}

	public static boolean isLoaded() {
		return FabricLoader.getInstance().isModLoaded("pointblank");
	}

	public static boolean isGun(ItemStack item) {
		if (!isLoaded()) return false;
		return item != null && !item.isEmpty() && item.getItem() instanceof GunItem;
	}

	public static int getGunAmmo(ItemStack item) {
		return getGunAmmo(item, null);
	}

	public static int getGunAmmo(ItemStack item, Player player) {
		if (!isLoaded()) return 0;
		if (item == null || !(item.getItem() instanceof GunItem)) return 0;
		if (player != null && player.level().isClientSide) {
			GunClientState state = GunClientState.getState(player, item, player.getInventory().selected, false);
			if (state != null) {
				FireModeInstance fireMode = GunItem.getFireModeInstance(item);
				if (fireMode != null) {
					return state.getAmmoCount(fireMode);
				}
			}
		}
		return GunItem.getAmmo(item, GunItem.getFireModeInstance(item));
	}

	public static void refillAmmo(Player player, ItemStack item) {
		if(!isLoaded() || player == null || item == null) return;
		if(!(item.getItem() instanceof GunItem gun)) return;
		if(!player.level().isClientSide) return;
		if(!SyncedConfigCache.isInstantGunReload(player)) return;
		FireModeInstance fireMode = GunItem.getFireModeInstance(item);
		UUID playerId = player.getUUID();
		PENDING_INSTANT_RELOAD.computeIfAbsent(playerId, id -> new AtomicInteger(0)).incrementAndGet();

		if(!gun.requestReloadFromServer(player, item)) {
			AtomicInteger counter = PENDING_INSTANT_RELOAD.get(playerId);
			if (counter != null && counter.decrementAndGet() <= 0) {
				PENDING_INSTANT_RELOAD.remove(playerId);
			}
			return;
		}

		if(fireMode != null) {
			int maxAmmo = gun.getMaxAmmoCapacity(item, fireMode);
			GunItem.setAmmo(item, fireMode, maxAmmo);
			GunClientState state = GunClientState.getState(player, item, player.getInventory().selected, false);
			if(state != null) {
				((PointBlankGunClientStateAccessor) state).deadeye$getAmmoCount().setAmmoCount(fireMode, maxAmmo);
			}
		}
	}

	public static boolean consumePendingInstantReload(Player player) {
		if(player == null) return false;
		AtomicInteger counter = PENDING_INSTANT_RELOAD.get(player.getUUID());
		if(counter == null) return false;
		if(counter.decrementAndGet() <= 0) {
			PENDING_INSTANT_RELOAD.remove(player.getUUID());
		}
		return true;
	}

	public static void clearPendingInstantReload(Player player) {
		if(player != null) {
			PENDING_INSTANT_RELOAD.remove(player.getUUID());
		}
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!isLoaded() || player == null || item == null) return;
		if(!(item.getItem() instanceof GunItem gun)) return;
		gun.tryFire(player, item, target);
	}

	public static FireMode getGunFiremode(ItemStack item) {
		if(!isLoaded() || item == null) return null;
		if(!(item.getItem() instanceof GunItem)) return null;
		return GunItem.getFireModeInstance(item).getType();
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(player == null || item == null || !(item.getItem() instanceof GunItem)) return false;
		if(!player.level().isClientSide) return false;

		GunClientState state = GunClientState.getState(player, item, player.getInventory().selected, false);
		if(state == null) return false;

		FireMode mode = getGunFiremode(item);
		if(mode == FireMode.AUTOMATIC && state.isIdle()) return true;
		else if(mode == FireMode.AUTOMATIC) return state.isFiring();
		else return state.isIdle();
	}

	public static boolean isGunReloading(ItemStack item, Player player) {
		if (!isLoaded() || player == null || item == null || !(item.getItem() instanceof GunItem)) return false;
		if (!player.level().isClientSide) return false;

		GunClientState state = GunClientState.getState(player, item, player.getInventory().selected, false);
		if (state == null) return false;
		return state.isReloading() || state.isPreparingReload();
	}

	public static boolean isDualWielding(Player player) {
		return false;
	}

	public static int getTotalGunAmmo(Player player) {
		if (!isLoaded() || player == null) return 0;
		if (isGun(player.getMainHandItem())) {
			return getGunAmmo(player.getMainHandItem(), player);
		}
		if (isGun(player.getOffhandItem())) {
			return getGunAmmo(player.getOffhandItem(), player);
		}
		return 0;
	}

	public static void refillAllGuns(Player player) {
		if (!isLoaded() || player == null) return;
		ItemStack main = player.getMainHandItem();
		if (isGun(main)) {
			refillAmmo(player, main);
		}
	}

	public static ItemStack getOperableGun(Player player) {
		if (!isLoaded() || player == null) return ItemStack.EMPTY;
		if (isGun(player.getMainHandItem())) return player.getMainHandItem();
		if (isGun(player.getOffhandItem())) return player.getOffhandItem();
		return ItemStack.EMPTY;
	}
}
