package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.client.DeadeyeClient;
import com.namefix.config.SyncedConfigCache;
import com.namefix.network.payload.*;
import com.namefix.server.DeadeyeServer;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class DeadeyeNetwork {
	public record PacketType<T>(ResourceLocation id) {
	}

	public static final PacketType<RequestDeadeyePayload> REQUEST_DEADEYE = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "request_deadeye"));
	public static final PacketType<DeadeyeStatePayload> DEADEYE_STATE = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "deadeye_state"));
	public static final PacketType<ConfigSyncPayload> CONFIG_SYNC = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "config_sync"));
	public static final PacketType<RequestMarkPayload> REQUEST_MARK_C2S = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "request_mark_c2s"));
	public static final PacketType<RequestMarkPayload> REQUEST_MARK_S2C = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "request_mark_s2c"));
	public static final PacketType<InformShotPayload> INFORM_SHOT = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "inform_shot"));
	public static final PacketType<InformShootingPhasePayload> INFORM_SHOOTING_PHASE = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "inform_shooting_phase"));
	public static final PacketType<LevelDataPayload> LEVEL_DATA = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "skill_data"));
	public static final PacketType<MeterDataPayload> METER_DATA = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "meter_data"));
	public static final PacketType<DeadeyeThresholdPayload> DEADEYE_THRESHOLD = new PacketType<>(new ResourceLocation(DeadeyeMod.MOD_ID, "deadeye_threshold"));

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_DEADEYE.id(), (buffer, context) -> DeadeyeServer.handleDeadeyeRequest(new RequestDeadeyePayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_MARK_C2S.id(), (buffer, context) -> DeadeyeServer.handleMarkRequest(new RequestMarkPayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, INFORM_SHOT.id(), (buffer, context) -> DeadeyeServer.handleShotInfo(new InformShotPayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, INFORM_SHOOTING_PHASE.id(), (buffer, context) -> DeadeyeServer.handleShootingPhase(new InformShootingPhasePayload(buffer), context));
	}

	public static void initializeClient() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, DEADEYE_STATE.id(), (buffer, context) -> DeadeyeClient.handleDeadeyeState(new DeadeyeStatePayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, CONFIG_SYNC.id(), (buffer, context) -> SyncedConfigCache.receiveConfigData(new ConfigSyncPayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, REQUEST_MARK_S2C.id(), (buffer, context) -> DeadeyeClient.handleDeadeyeMark(new RequestMarkPayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, LEVEL_DATA.id(), (buffer, context) -> DeadeyeClient.handleLevelData(new LevelDataPayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, METER_DATA.id(), (buffer, context) -> DeadeyeClient.handleMeterData(new MeterDataPayload(buffer), context));
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, DEADEYE_THRESHOLD.id(), (buffer, context) -> DeadeyeClient.handleThresholdToast(new DeadeyeThresholdPayload(buffer), context));
	}

	public static FriendlyByteBuf createBuffer() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}

	public static void sendToServer(PacketType<?> packetType, FriendlyByteBuf buffer) {
		NetworkManager.sendToServer(packetType.id(), buffer);
	}

	public static void sendToPlayer(ServerPlayer player, PacketType<?> packetType, FriendlyByteBuf buffer) {
		NetworkManager.sendToPlayer(player, packetType.id(), buffer);
	}
}
