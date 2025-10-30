package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3f;

public record RequestMarkPayload(Vector3f markPos, int entityId, Type<RequestMarkPayload> packetType) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestMarkPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VECTOR3F, RequestMarkPayload::markPos,
			ByteBufCodecs.INT, RequestMarkPayload::entityId,
			(markPos, entityId) -> new RequestMarkPayload(markPos, entityId, DeadeyeNetwork.REQUEST_MARK_C2S)
	);

	// C2S packets
	public RequestMarkPayload(Vector3f markPos, int entityId) {
		this(markPos, entityId, DeadeyeNetwork.REQUEST_MARK_C2S);
	}

	// Factory for S2C packets
	public static RequestMarkPayload forServerToClient(Vector3f markPos, int entityId) {
		return new RequestMarkPayload(markPos, entityId, DeadeyeNetwork.REQUEST_MARK_S2C);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return packetType;
	}
}
