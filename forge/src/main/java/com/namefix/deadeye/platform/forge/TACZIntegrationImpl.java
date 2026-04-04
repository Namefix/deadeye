package com.namefix.deadeye.platform.forge;

import com.namefix.deadeye.config.DeadeyeConfig;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;

public class TACZIntegrationImpl {
	private TACZIntegrationImpl() {}

	public static boolean isLoaded() {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded("tacz");
	}

	public static boolean isGun(ItemStack item) {
		if (!isLoaded()) return false;
		return item.getItem() instanceof AbstractGunItem;
	}

	public static int getGunAmmo(ItemStack item) {
		IGun gun = IGun.getIGunOrNull(item);
		if(gun == null) return -1;

		ResourceLocation gunId = gun.getGunId(item);
		CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).get();
		boolean hasAmmoInBarrel = gun.hasBulletInBarrel(item) && gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT;

		return gun.getCurrentAmmoCount(item) + (hasAmmoInBarrel?1:0);
	}

	public static void refillAmmo(Player player, ItemStack item) {
		if(!isLoaded()) return;
		if(!(item.getItem() instanceof AbstractGunItem)) return;
		if(player.level().isClientSide) return;
		if(!DeadeyeConfig.Server.instantGunReload) return;

		IGun gun = IGun.getIGunOrNull(item);
		if(gun == null) return;
		ResourceLocation gunId = gun.getGunId(item);
		Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
		if(gunIndexOptional.isEmpty()) return;

		CommonGunIndex gunIndex = gunIndexOptional.get();
		int maxAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(item, gunIndex.getGunData());
		int currentAmmo = Math.max(gun.getCurrentAmmoCount(item), 0);
		int ammoNeeded = Math.max(maxAmmo - currentAmmo, 0);
		if(ammoNeeded <= 0) return;

		int availableAmmo = getAvailableAmmoForRefill(player, item, gun, ammoNeeded);
		int ammoToRefill = Math.min(ammoNeeded, Math.max(availableAmmo, 0));
		if(ammoToRefill <= 0) return;
		if(!consumeAmmoForRefill(player, item, gun, ammoToRefill)) return;

		int newAmmo = Math.min(currentAmmo + ammoToRefill, maxAmmo);
		gun.setCurrentAmmoCount(item, newAmmo);
	}

	private static int getAvailableAmmoForRefill(Player player, ItemStack gunStack, IGun gun, int ammoNeeded) {
		if(ammoNeeded <= 0) return 0;
		if(player.getAbilities().instabuild) return ammoNeeded;

		int availableAmmo = 0;

		if(gun.useDummyAmmo(gunStack)) {
			availableAmmo += Math.max(gun.getDummyAmmoAmount(gunStack), 0);
		}

		int inventoryAmmo = 0;
		IItemHandler itemHandler = player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
		if(itemHandler != null) {
			inventoryAmmo = countCompatibleAmmo(itemHandler, gunStack);
			availableAmmo += inventoryAmmo;
		}

		return availableAmmo;
	}

	private static boolean consumeAmmoForRefill(Player player, ItemStack gunStack, IGun gun, int ammoNeeded) {
		if(ammoNeeded <= 0) return true;
		if(player.getAbilities().instabuild) return true;

		int remaining = ammoNeeded;

		if(gun.useDummyAmmo(gunStack)) {
			int dummyAmmo = Math.max(gun.getDummyAmmoAmount(gunStack), 0);
			int consumeDummy = Math.min(dummyAmmo, remaining);
			if(consumeDummy > 0) {
				gun.addDummyAmmoAmount(gunStack, -consumeDummy);
				remaining -= consumeDummy;
			}
		}

		if(remaining > 0) {
			IItemHandler itemHandler = player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
			if(itemHandler != null) {
				remaining = extractCompatibleAmmo(itemHandler, gunStack, remaining);
			}
		}
		return remaining <= 0;
	}

	private static int countCompatibleAmmo(IItemHandler itemHandler, ItemStack gunStack) {
		int count = 0;
		for(int slot = 0; slot < itemHandler.getSlots(); slot++) {
			ItemStack stack = itemHandler.getStackInSlot(slot);
			if(stack.isEmpty()) continue;

			if(stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)) {
				count += stack.getCount();
				continue;
			}

			if(stack.getItem() instanceof IAmmoBox ammoBox && ammoBox.isAmmoBoxOfGun(gunStack, stack)) {
				count += Math.max(ammoBox.getAmmoCount(stack), 0);
			}
		}
		return count;
	}

	private static int extractCompatibleAmmo(IItemHandler itemHandler, ItemStack gunStack, int remainingNeeded) {
		int remaining = remainingNeeded;
		for(int slot = 0; slot < itemHandler.getSlots() && remaining > 0; slot++) {
			ItemStack stack = itemHandler.getStackInSlot(slot);
			if(stack.isEmpty()) continue;

			if(stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)) {
				ItemStack extracted = itemHandler.extractItem(slot, remaining, false);
				remaining -= extracted.getCount();
				continue;
			}

			if(stack.getItem() instanceof IAmmoBox ammoBox && ammoBox.isAmmoBoxOfGun(gunStack, stack)) {
				int boxAmmo = Math.max(ammoBox.getAmmoCount(stack), 0);
				int consume = Math.min(boxAmmo, remaining);
				if(consume > 0) {
					ammoBox.setAmmoCount(stack, boxAmmo - consume);
					remaining -= consume;
				}
			}
		}
		return remaining;
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!(player instanceof LocalPlayer localPlayer)) return;
		localPlayer.connection.send(new ServerboundMovePlayerPacket.Rot(localPlayer.getYRot(), localPlayer.getXRot(), localPlayer.onGround()));
		IClientPlayerGunOperator.fromLocalPlayer(localPlayer).shoot();
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(player == null || !player.level().isClientSide) return false;
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) return false;
		IGun gun = IGun.getIGunOrNull(item);
		if(gun == null) return false;
		if(IClientPlayerGunOperator.fromLocalPlayer(mc.player).getDataHolder().clientStateLock) return false;

		long shootCooldown = IClientPlayerGunOperator.fromLocalPlayer(mc.player).getClientShootCoolDown();
		if(shootCooldown < 0 || shootCooldown >= 50) return false;

		IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
		if(gunOperator.getSynReloadState().getStateType().isReloading()) return false;
		if(gunOperator.getSynDrawCoolDown() != 0) return false;
		if(gunOperator.getSynIsBolting()) return false;
		if(gunOperator.getSynMeleeCoolDown() != 0) return false;
		if(gunOperator.getSynSprintTime() > 0) return false;

		return true;
	}
}
