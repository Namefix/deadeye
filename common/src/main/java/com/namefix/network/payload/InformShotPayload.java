package com.namefix.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;

public record InformShotPayload(Vector3f targetPos) {
	public InformShotPayload(FriendlyByteBuf buffer) {
		this(buffer.readVector3f());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVector3f(targetPos);
	}
}
