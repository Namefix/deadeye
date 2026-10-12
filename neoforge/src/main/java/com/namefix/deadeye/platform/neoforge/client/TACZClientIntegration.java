package com.namefix.deadeye.platform.neoforge.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TACZClientIntegration {
	private TACZClientIntegration() {}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!(player instanceof LocalPlayer localPlayer)) return;
		localPlayer.connection.send(new ServerboundMovePlayerPacket.Rot(localPlayer.getYRot(), localPlayer.getXRot(), localPlayer.onGround()));
		IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(localPlayer);
		IGun gun = IGun.getIGunOrNull(item);
		if(gun != null) {
			TimelessAPI.getCommonGunIndex(gun.getGunId(item)).ifPresent(index -> {
				ChargeData chargeData = index.getGunData().getChargeData(gun.getFireMode(item));
				if(chargeData != null) {
					operator.getDataHolder().chargeProgress = chargeData.getMaxCharge();
				}
			});
		}
		operator.shoot();
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

