package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record InformShootingPhasePayload() implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, InformShootingPhasePayload> CODEC = StreamCodec.unit(new InformShootingPhasePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.INFORM_SHOOTING_PHASE;
	}
}
