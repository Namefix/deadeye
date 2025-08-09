package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.server.DeadeyeServer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class DeadeyeNetwork {
	public static final CustomPacketPayload.Type<RequestDeadeyePayload> REQUEST_DEADEYE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "request_deadeye"));

	public static void initialize() {

	}

	public static void initializeClient() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_DEADEYE, RequestDeadeyePayload.CODEC, DeadeyeServer::handleDeadeyeRequest);
	}
}
