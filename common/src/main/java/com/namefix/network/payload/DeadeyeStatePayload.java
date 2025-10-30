package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DeadeyeStatePayload(boolean state, float previousTickrate, int phase) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, DeadeyeStatePayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, DeadeyeStatePayload::state, ByteBufCodecs.FLOAT, DeadeyeStatePayload::previousTickrate, ByteBufCodecs.INT, DeadeyeStatePayload::phase, DeadeyeStatePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.DEADEYE_STATE;
	}
}
