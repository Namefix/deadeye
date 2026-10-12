package com.namefix.deadeye.platform.neoforge.client;

import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.network.packets.ServerboundFireGunPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class IronsArtificeClientIntegration {
	private IronsArtificeClientIntegration() {}

	public static void fireGun(ItemStack item, Player player, Entity target) {
		if(!(player instanceof LocalPlayer localPlayer)) return;

		localPlayer.connection.send(new ServerboundMovePlayerPacket.Rot(localPlayer.getYRot(), localPlayer.getXRot(), localPlayer.onGround()));

		Vec3 direction = Vec3.directionFromRotation(localPlayer.getXRot(), localPlayer.getYRot());
		FireOutcome outcome = GunplayManager.tryFire(localPlayer, direction);
		if(outcome.fired()) {
			PacketDistributor.sendToServer(new ServerboundFireGunPacket(direction));
		}
	}
}

