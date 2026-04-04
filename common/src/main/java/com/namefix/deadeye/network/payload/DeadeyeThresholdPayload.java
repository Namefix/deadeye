package com.namefix.deadeye.network.payload;

import com.namefix.deadeye.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DeadeyeThresholdPayload(int threshold) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, DeadeyeThresholdPayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, DeadeyeThresholdPayload::threshold, DeadeyeThresholdPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.DEADEYE_THRESHOLD;
	}
}
