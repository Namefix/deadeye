package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3f;

public record InformShotPayload(Vector3f targetPos) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, InformShotPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VECTOR3F, InformShotPayload::targetPos, InformShotPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return DeadeyeNetwork.INFORM_SHOT;
	}
}
