package com.namefix.deadeye.data;

import com.namefix.deadeye.DeadeyeMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class StateManager extends SavedData {

	public HashMap<UUID, PlayerSavedData> players = new HashMap<>();

	@Override
	public @NotNull CompoundTag save(CompoundTag compoundTag) {
		CompoundTag playersTag = new CompoundTag();
		players.forEach(((uuid, playerSavedData) -> {
			CompoundTag playerTag = new CompoundTag();

			playerTag.putInt("deadeyeSkill", playerSavedData.deadeyeSkill);
			playerTag.putInt("deadeyeLevel", playerSavedData.deadeyeLevel);
			playerTag.putFloat("deadeyeXp", playerSavedData.deadeyeXp);
			playerTag.putFloat("deadeyeMeter", playerSavedData.deadeyeMeter);
			playerTag.putFloat("deadeyeCore", playerSavedData.deadeyeCore);

			playerTag.putFloat("deadeyeConsumeRate", playerSavedData.deadeyeConsumeRate);
			playerTag.putFloat("deadeyeKillReward", playerSavedData.deadeyeKillReward);

			playersTag.put(uuid.toString(), playerTag);
		}));
		compoundTag.put("players", playersTag);
		return compoundTag;
	}

	public static StateManager createFromNbt(CompoundTag compoundTag) {
		StateManager state = new StateManager();
		CompoundTag playersTag = compoundTag.getCompound("players");
		playersTag.getAllKeys().forEach(key -> {
			CompoundTag playerTag = playersTag.getCompound(key);
			PlayerSavedData playerData = new PlayerSavedData();

			playerData.deadeyeSkill = playerTag.getInt("deadeyeSkill");
			playerData.deadeyeLevel = playerTag.getInt("deadeyeLevel");
			playerData.deadeyeXp = playerTag.getFloat("deadeyeXp");
			playerData.deadeyeMeter = playerTag.getFloat("deadeyeMeter");
			playerData.deadeyeCore = playerTag.getFloat("deadeyeCore");

			playerData.deadeyeConsumeRate = playerTag.getFloat("deadeyeConsumeRate");
			playerData.deadeyeKillReward = playerTag.getFloat("deadeyeKillReward");

			UUID uuid = UUID.fromString(key);
			state.players.put(uuid, playerData);
		});
		return state;
	}

	public static StateManager createNew() {
		return new StateManager();
	}

	public static StateManager getServerState(MinecraftServer server) {
		ServerLevel world = server.getLevel(Level.OVERWORLD);
		assert world != null;

		StateManager state = world.getDataStorage().computeIfAbsent(StateManager::createFromNbt, StateManager::new, DeadeyeMod.MOD_ID);

		state.setDirty();
		return state;
	}

	public static PlayerSavedData getPlayerState(ServerPlayer player) {
		StateManager state = getServerState(player.getServer());
		return state.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerSavedData());
	}
}
