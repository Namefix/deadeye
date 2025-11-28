package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MeterDataPayload(float deadeyeMeter, float deadeyeCore) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, MeterDataPayload> CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, MeterDataPayload::deadeyeMeter, ByteBufCodecs.FLOAT, MeterDataPayload::deadeyeCore, MeterDataPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.METER_DATA;
	}
}
