package com.namefix.integration.pointblank;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PointBlankPendingShotAim {
	private static final Map<UUID, Aim> PENDING_AIM = new ConcurrentHashMap<>();

	private PointBlankPendingShotAim() {}

	public static void put(Player player, float xRot, float yRot) {
		if(player == null) return;
		PENDING_AIM.put(player.getUUID(), new Aim(xRot, yRot));
	}

	public static Aim consume(Player player) {
		if(player == null) return null;
		return PENDING_AIM.remove(player.getUUID());
	}

	public record Aim(float xRot, float yRot) {}
}
