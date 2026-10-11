package com.namefix.deadeye.platform.neoforge;

import com.namefix.deadeye.config.SyncedConfigCache;
import io.redspace.irons_artifice.item.*;
import io.redspace.irons_artifice.network.packets.ServerboundFireGunPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

public class IronsArtificeIntegrationImpl {
	private IronsArtificeIntegrationImpl() {}

	public static boolean isLoaded() {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded("irons_artifice");
	}

	public static boolean isGun(ItemStack item) {
		return isLoaded() && item != null && !item.isEmpty() && item.getItem() instanceof GunItem;
	}

	public static int getGunAmmo(ItemStack item) {
		if(!isGun(item)) return -1;
		return GunItem.getMagazine(item).count();
	}

	public static void refillAmmo(Player player, ItemStack item) {
		if(!isLoaded() || player == null || player.level().isClientSide || !isGun(item)) return;

		if(ReloadState.has(item)) {
			ReloadState.remove(item);
			GunplayManager.cancelGunAnimation(player, item);
		}
		FireDelayState.clear(player);

		if(SyncedConfigCache.isInstantGunReload(player)) {
			GunplayManager.attemptFinishReload(player, item, 0);
		}
	}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!(player instanceof LocalPlayer localPlayer)) return;

		localPlayer.connection.send(new ServerboundMovePlayerPacket.Rot(localPlayer.getYRot(), localPlayer.getXRot(), localPlayer.onGround()));

		Vec3 direction = Vec3.directionFromRotation(localPlayer.getXRot(), localPlayer.getYRot());
		FireOutcome outcome = GunplayManager.tryFire(localPlayer, direction);
		if(outcome.fired()) {
			PacketDistributor.sendToServer(new ServerboundFireGunPacket(direction));
		}
	}

	public static boolean isGunReady(ItemStack item, Player player) {
		if(!isGun(item) || player == null) return false;
		return !GunItem.isReloading(item) && GunItem.getMagazine(item).count() > 0 && !FireDelayState.isActive(player, item);
	}
}
