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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PointBlankIntegrationImpl {
	private static final Map<UUID, AtomicInteger> PENDING_INSTANT_RELOAD = new ConcurrentHashMap<>();

	private PointBlankIntegrationImpl() {}

	public static boolean isLoaded() {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded("pointblank");
	}

	public static boolean isGun(ItemStack item) {
		if (!isLoaded()) return false;
		return item != null && !item.isEmpty() && item.getItem() instanceof GunItem;
	}

	private static GunItem.OperableGunContext resolveGunContext(Player player, ItemStack item) {
		if (player == null || item == null) return null;
		return (item == player.getOffhandItem())
			? GunItem.resolveOffhandGunContext(player)
			: GunItem.resolveMainHandGunContext(player);
	}

	private static GunClientState resolveClientState(Player player, ItemStack item) {
		if (player == null || item == null) return null;
		GunClientState state = (item == player.getOffhandItem())
			? GunClientState.getOffhandState(player)
			: GunClientState.getMainHandState(player);
		if (state != null) return state;

		GunItem.OperableGunContext ctx = GunItem.resolveContextForStack(player, item);
		int slot = ctx != null ? ctx.slotIndex() : (item == player.getOffhandItem() ? 40 : player.getInventory().selected);
		boolean offhand = ctx != null ? ctx.offhand() : (item == player.getOffhandItem());
		return GunClientState.getState(player, item, slot, offhand);
	}

	public static int getGunAmmo(ItemStack item) {
		return getGunAmmo(item, null);
	}

	public static int getGunAmmo(ItemStack item, Player player) {
		if (!isLoaded()) return 0;
		if (item == null || !(item.getItem() instanceof GunItem)) return 0;
		if (player != null && player.level().isClientSide) {
			GunClientState state = resolveClientState(player, item);
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

		GunItem.OperableGunContext ctx = resolveGunContext(player, item);
		boolean requested = ctx != null
			? gun.requestReloadFromServer(player, ctx)
			: gun.requestReloadFromServer(player, item);

		if(!requested) {
			AtomicInteger counter = PENDING_INSTANT_RELOAD.get(playerId);
			if (counter != null && counter.decrementAndGet() <= 0) {
				PENDING_INSTANT_RELOAD.remove(playerId);
			}
			return;
		}

		if(fireMode != null) {
			int maxAmmo = gun.getMaxAmmoCapacity(item, fireMode);
			GunItem.setAmmo(item, fireMode, maxAmmo);
			GunClientState state = resolveClientState(player, item);
			if(state != null) {
				setClientAmmoCount(state, fireMode, maxAmmo);
			}
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
		if(!(player instanceof LocalPlayer localPlayer)) return;
		if(!(item.getItem() instanceof GunItem gun)) return;
		GunItem.OperableGunContext ctx = resolveGunContext(player, item);
		if (ctx != null) {
			gun.tryFire(localPlayer, ctx, target);
		} else {
			gun.tryFire(localPlayer, item, target);
		}
	}

	public static FireMode getGunFiremode(ItemStack item) {
		if(!isLoaded() || item == null) return null;
		if(!(item.getItem() instanceof GunItem)) return null;
		return GunItem.getFireModeInstance(item).getType();
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(player == null || item == null || !(item.getItem() instanceof GunItem)) return false;
		if(!player.level().isClientSide) return false;

		GunClientState state = resolveClientState(player, item);
		if(state == null) return false;

		FireMode mode = getGunFiremode(item);
		if(mode == FireMode.AUTOMATIC && state.isIdle()) return true;
		else if(mode == FireMode.AUTOMATIC) return state.isFiring();
		else return state.isIdle();
	}

	public static boolean isGunReloading(ItemStack item, Player player) {
		if (!isLoaded() || player == null || item == null || !(item.getItem() instanceof GunItem)) return false;
		if (!player.level().isClientSide) return false;

		GunClientState state = resolveClientState(player, item);
		if (state == null) return false;
		return state.isReloading() || state.isPreparingReload();
	}

	public static boolean isDualWielding(Player player) {
		if (!isLoaded() || player == null) return false;
		return GunItem.hasGunsInMainAndAltSlots(player);
	}

	public static int getTotalGunAmmo(Player player) {
		if (!isLoaded() || player == null) return 0;
		if (GunItem.hasGunsInMainAndAltSlots(player)) {
			return getGunAmmo(player.getMainHandItem(), player) + getGunAmmo(player.getOffhandItem(), player);
		}
		if (isGun(player.getMainHandItem())) {
			return getGunAmmo(player.getMainHandItem(), player);
		}
		if (isGun(player.getOffhandItem())) {
			return getGunAmmo(player.getOffhandItem(), player);
		}
		GunItem.OperableGunContext ctx = GunItem.resolveOperableGunContext(player);
		return ctx != null ? getGunAmmo(ctx.itemStack(), player) : 0;
	}

	public static void refillAllGuns(Player player) {
		if (!isLoaded() || player == null) return;
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		if (isGun(main)) {
			refillAmmo(player, main);
		}
		if (isGun(off)) {
			refillAmmo(player, off);
		}
	}

	public static ItemStack getOperableGun(Player player) {
		if (!isLoaded() || player == null) return ItemStack.EMPTY;
		GunItem.OperableGunContext ctx = GunItem.resolveOperableGunContext(player);
		return ctx != null ? ctx.itemStack() : ItemStack.EMPTY;
	}
}