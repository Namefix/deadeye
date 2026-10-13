package com.namefix.deadeye.platform.forge.client;

import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PointBlankClientIntegration {
	private PointBlankClientIntegration() {}

	public static void fireGun(GunItem gun, ItemStack item, Player player, Entity target, GunItem.OperableGunContext ctx) {
		if(!(player instanceof LocalPlayer localPlayer)) return;
		if (ctx != null) {
			gun.tryFire(localPlayer, ctx, target);
		} else {
			gun.tryFire(localPlayer, item, target);
		}
	}
}

