package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestDeadeyePayload() implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestDeadeyePayload> CODEC = StreamCodec.unit(new RequestDeadeyePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.REQUEST_DEADEYE;
	}
}
