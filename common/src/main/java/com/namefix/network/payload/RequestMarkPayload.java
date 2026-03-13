package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;

public record RequestMarkPayload(Vector3f markPos, int entityId, DeadeyeNetwork.PacketType<RequestMarkPayload> packetType) {
	public RequestMarkPayload(FriendlyByteBuf buffer) {
		this(buffer.readVector3f(), buffer.readInt(), DeadeyeNetwork.REQUEST_MARK_C2S);
	}

	// C2S packets
	public RequestMarkPayload(Vector3f markPos, int entityId) {
		this(markPos, entityId, DeadeyeNetwork.REQUEST_MARK_C2S);
	}

	// Factory for S2C packets
	public static RequestMarkPayload forServerToClient(Vector3f markPos, int entityId) {
		return new RequestMarkPayload(markPos, entityId, DeadeyeNetwork.REQUEST_MARK_S2C);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVector3f(markPos);
		buffer.writeInt(entityId);
	}
}
