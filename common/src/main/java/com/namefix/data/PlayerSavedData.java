package com.namefix.data;

import com.google.common.collect.Lists;
import com.namefix.server.DeadeyeServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.List;

public class PlayerSavedData {
	public int deadeyeSkill = 3;
	public int deadeyeLevel = 3;
	public float deadeyeXp = 0;
	public float deadeyeMeter = 30.0f;
	public float deadeyeCore = 20.0f;

	// Will be used server side only
	public float deadeyeConsumeRate = 0.25f;
	public float deadeyeKillReward = 3f;

	private static final List<Vector3f> HUD_FORTIFICATION_COLORS = Lists.newArrayList(
			new Vector3f(1f, 0.969f, 0.776f),
			new Vector3f(1f, 0.969f, 0.659f),
			new Vector3f(0.976f, 0.925f, 0.412f)
	);

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

	public static void setDeadeyeXP(ServerPlayer player, float xp) {
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

	public static void setDeadeyeConsumeRate(ServerPlayer player, float rate) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeConsumeRate = rate;
	}

	public static void setDeadeyeKillReward(ServerPlayer player, float reward) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeKillReward = reward;
	}

	public static void addDeadeyeLevel(ServerPlayer player, int amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		data.deadeyeLevel = Mth.clamp(data.deadeyeLevel+amount, 0, 10);
		data.deadeyeXp = 0;
		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void addDeadeyeXP(ServerPlayer player, float amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		if(data.deadeyeLevel >= 10) return;
		data.deadeyeXp += amount;

		boolean leveledUp = false;
		float levelup = requiredXPToLevelUp(data.deadeyeLevel);
		while(data.deadeyeXp >= levelup) {
			data.deadeyeXp -= levelup;
			data.deadeyeLevel++;
			levelup = requiredXPToLevelUp(data.deadeyeLevel);
			leveledUp = true;
		}
		if(leveledUp) //TODO: Remove this message when the level up hud is added
			player.sendSystemMessage(Component.literal("[DeadEye] Your Dead Eye leveled up! " + data.deadeyeLevel));

		DeadeyeServer.updatePlayerLevelData(player, data);
	}

	public static void addDeadeyeMeter(ServerPlayer player, float amount, boolean meterCap) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		if(meterCap && data.deadeyeMeter >= data.deadeyeLevel*10) return;
		if(meterCap && data.deadeyeMeter <= data.deadeyeLevel*10 && data.deadeyeMeter + amount > data.deadeyeLevel*10f) data.deadeyeMeter = data.deadeyeLevel*10f;
		else data.deadeyeMeter = Mth.clamp(data.deadeyeMeter + amount, 0f, getMaxMeter(data, 3));
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	public static void addDeadeyeCore(ServerPlayer player, float amount, boolean coreCap) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		if(coreCap && data.deadeyeCore >= 20f) return;
		if(coreCap && data.deadeyeCore <= 20f && data.deadeyeCore + amount > 20f) data.deadeyeCore = 20f;
		else data.deadeyeCore = Mth.clamp(data.deadeyeCore + amount, 0f, 80f);
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	public static void addTotalMeter(ServerPlayer player, float amount, boolean meterCap, boolean coreCap) {
		PlayerSavedData data = StateManager.getPlayerState(player);
		if(data.deadeyeMeter < getMaxMeter(data, meterCap ? 0 : 3)) addDeadeyeMeter(player, amount, meterCap);
		if(data.deadeyeCore < (coreCap ? 20f : 80f)) addDeadeyeCore(player, amount, coreCap);
	}

	// Subtracts meter first and core second until depletion.
	public static void subDeadeyeTotal(ServerPlayer player, float amount) {
		PlayerSavedData data = StateManager.getPlayerState(player);

		if(data.deadeyeMeter > 0f) data.deadeyeMeter = Mth.clamp(data.deadeyeMeter-amount, 0f, getMaxMeter(data, 3));
		else data.deadeyeCore = Mth.clamp(data.deadeyeCore-amount, 0f, 80f);
		DeadeyeServer.updatePlayerMeterData(player, data);
	}

	public static float requiredXPToLevelUp(int currentLevel) {
		return currentLevel * 100f;
	}

	// Maximum Dead Eye level achievable with the given level. Adds max meter value to max core value.
	public static float getMaxTotalDeadeye(int level) {
		return (level*10f)+60f;
	}

	public static float getMaxMeter(PlayerSavedData data, int tonicLevel) {
		return (data.deadeyeLevel*10)+(tonicLevel*20);
	}
	public static float getMaxMeter(PlayerSavedData data) {
		return getMaxMeter(data, 0);
	}

	// Player uses dead eye core without fortification
	public static boolean usingDeadeyeCore(PlayerSavedData data) {
		return data.deadeyeMeter <= 0f && data.deadeyeCore > 0f && data.deadeyeCore <= 20f;
	}

	// Player uses dead eye meter without fortification
	public static boolean usingDeadeyeMeter(PlayerSavedData data) {
		return data.deadeyeMeter > 0f && data.deadeyeMeter <= data.deadeyeLevel*10f;
	}

	public static Vector3f getMeterColor(PlayerSavedData data) {
		if(data.deadeyeMeter > getMaxMeter(data, 2)) return HUD_FORTIFICATION_COLORS.get(2);
		else if(data.deadeyeMeter > getMaxMeter(data, 1)) return HUD_FORTIFICATION_COLORS.get(1);
		else if(data.deadeyeMeter > getMaxMeter(data, 0)) return HUD_FORTIFICATION_COLORS.get(0);
		else return new Vector3f(1.0f, 1.0f, 1.0f);
	}

	public static Vector3f getCoreColor(float core) {
		if(core> 60) return HUD_FORTIFICATION_COLORS.get(2);
		else if(core > 40) return HUD_FORTIFICATION_COLORS.get(1);
		else if(core > 20) return HUD_FORTIFICATION_COLORS.get(0);
		else return new Vector3f(1.0f, 1.0f, 1.0f);
	}
}
