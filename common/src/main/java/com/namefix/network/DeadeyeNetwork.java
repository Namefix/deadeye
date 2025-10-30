package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.client.DeadeyeClient;
import com.namefix.config.SyncedConfigCache;
import com.namefix.network.payload.ConfigSyncPayload;
import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.network.payload.RequestMarkPayload;
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

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, DEADEYE_STATE, DeadeyeStatePayload.CODEC, DeadeyeClient::handleDeadeyeState);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, CONFIG_SYNC, ConfigSyncPayload.CODEC, SyncedConfigCache::receiveConfigData);
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, REQUEST_MARK_S2C, RequestMarkPayload.CODEC, DeadeyeClient::handleDeadeyeMark);
	}

	public static void initializeClient() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_DEADEYE, RequestDeadeyePayload.CODEC, DeadeyeServer::handleDeadeyeRequest);
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_MARK_C2S, RequestMarkPayload.CODEC, DeadeyeServer::handleMarkRequest);
	}
}
