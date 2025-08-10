package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.client.DeadeyeClient;
import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.server.DeadeyeServer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class DeadeyeNetwork {
	public static final CustomPacketPayload.Type<RequestDeadeyePayload> REQUEST_DEADEYE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "request_deadeye"));
	public static final CustomPacketPayload.Type<DeadeyeStatePayload> DEADEYE_STATE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "deadeye_state"));

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, DEADEYE_STATE, DeadeyeStatePayload.CODEC, DeadeyeClient::handleDeadeyeState);
	}

	public static void initializeClient() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_DEADEYE, RequestDeadeyePayload.CODEC, DeadeyeServer::handleDeadeyeRequest);
	}
}
