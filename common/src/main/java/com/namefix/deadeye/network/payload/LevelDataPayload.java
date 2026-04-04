package com.namefix.deadeye.network.payload;

import com.namefix.deadeye.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LevelDataPayload(int deadeyeSkill, int deadeyeLevel, float deadeyeXp) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, LevelDataPayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, LevelDataPayload::deadeyeSkill, ByteBufCodecs.INT, LevelDataPayload::deadeyeLevel, ByteBufCodecs.FLOAT, LevelDataPayload::deadeyeXp, LevelDataPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.LEVEL_DATA;
	}
}
