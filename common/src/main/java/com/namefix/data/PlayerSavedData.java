package com.namefix.data;

import com.namefix.server.DeadeyeServer;
import com.namefix.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class PlayerSavedData {
	public int deadeyeSkill = 3;
	public int deadeyeLevel = 3;
	public int deadeyeXp = 0;
	public float deadeyeMeter = 30.0f;
	public float deadeyeCore = 20.0f;

	public static void setDeadeyeSkill(ServerPlayer player, int skill) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeSkill = skill;
		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void setDeadeyeLevel(ServerPlayer player, int level) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeLevel = level;
		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void setDeadeyeXP(ServerPlayer player, int xp) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeXp = xp;
		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void setDeadeyeMeter(ServerPlayer player, float meter) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeMeter = meter;
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	public static void setDeadeyeCore(ServerPlayer player, float core) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeCore = core;
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	public static void addDeadeyeLevel(ServerPlayer player, int amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeLevel = Mth.clamp(data.deadeyeLevel+amount, 0, 10);
		data.deadeyeXp = 0;
		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void addDeadeyeXP(ServerPlayer player, int amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		if(data.deadeyeLevel >= 10) return;
		data.deadeyeXp += amount;

		boolean leveledUp = false;
		while(data.deadeyeXp >= requiredXPToLevelUp(data.deadeyeLevel)) {
			data.deadeyeXp -= requiredXPToLevelUp(data.deadeyeLevel);
			data.deadeyeLevel++;
			leveledUp = true;
		}
		if(leveledUp) //TODO: Remove this message when the level up hud is added
			player.sendSystemMessage(Component.literal("[DeadEye] Your Dead Eye leveled up! " + data.deadeyeLevel));

		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void addDeadeyeMeter(ServerPlayer player, float amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeMeter = Mth.clamp(data.deadeyeMeter + amount, 0f, data.deadeyeLevel*10f);
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	public static void addDeadeyeCore(ServerPlayer player, float amount, boolean meterCap) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		if(meterCap && data.deadeyeCore + amount > 20f) data.deadeyeCore = 20f;
		else data.deadeyeCore = Mth.clamp(data.deadeyeCore + amount, 0f, 80f);
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	// Subtracts meter first and core second until depletion.
	public static void subDeadeyeTotal(ServerPlayer player, float amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);

		if(data.deadeyeMeter > 0f) data.deadeyeMeter = Mth.clamp(data.deadeyeMeter-amount, 0f, data.deadeyeLevel*10f);
		else data.deadeyeCore = Mth.clamp(data.deadeyeCore-amount, 0f, 80f);
	}

	public static int requiredXPToLevelUp(int currentLevel) {
		return currentLevel * 10;
	}

	// Maximum Dead Eye level achievable with the given level. Adds max meter value to max core value.
	public static float getMaxTotalDeadeye(int level) {
		return (level*10f)+80f;
	}
}
