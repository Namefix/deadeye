package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.client.DeadeyeClient;
import com.namefix.config.SyncedConfigCache;
import com.namefix.network.payload.*;
import com.namefix.server.DeadeyeServer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class DeadeyeNetwork {
	public static final CustomPacketPayload.Type<RequestDeadeyePayload> REQUEST_DEADEYE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "request_deadeye"));
	public static final CustomPacketPayload.Type<DeadeyeStatePayload> DEADEYE_STATE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "deadeye_state"));
	public static final CustomPacketPayload.Type<ConfigSyncPayload> CONFIG_SYNC = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "config_sync"));
	public static final CustomPacketPayload.Type<RequestMarkPayload> REQUEST_MARK_C2S = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "request_mark_c2s"));
	public static final CustomPacketPayload.Type<RequestMarkPayload> REQUEST_MARK_S2C = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "request_mark_s2c"));
	public static final CustomPacketPayload.Type<InformShotPayload> INFORM_SHOT = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "inform_shot"));
	public static final CustomPacketPayload.Type<InformShootingPhasePayload> INFORM_SHOOTING_PHASE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "inform_shooting_phase"));
	public static final CustomPacketPayload.Type<LevelDataPayload> LEVEL_DATA = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "skill_data"));
	public static final CustomPacketPayload.Type<MeterDataPayload> METER_DATA = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "meter_data"));

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, DEADEYE_STATE, DeadeyeStatePayload.CODEC, DeadeyeClient::handleDeadeyeState);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, CONFIG_SYNC, ConfigSyncPayload.CODEC, SyncedConfigCache::receiveConfigData);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, REQUEST_MARK_S2C, RequestMarkPayload.CODEC, DeadeyeClient::handleDeadeyeMark);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, LEVEL_DATA, LevelDataPayload.CODEC, DeadeyeClient::handleLevelData);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, METER_DATA, MeterDataPayload.CODEC, DeadeyeClient::handleMeterData);
	}

	public static void initializeClient() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_DEADEYE, RequestDeadeyePayload.CODEC, DeadeyeServer::handleDeadeyeRequest);
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_MARK_C2S, RequestMarkPayload.CODEC, DeadeyeServer::handleMarkRequest);
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, INFORM_SHOT, InformShotPayload.CODEC, DeadeyeServer::handleShotInfo);
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, INFORM_SHOOTING_PHASE, InformShootingPhasePayload.CODEC, DeadeyeServer::handleShootingPhase);
	}
}
