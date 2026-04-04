package com.namefix.deadeye.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record MeterDataPayload(float deadeyeMeter, float deadeyeCore) {
	public MeterDataPayload(FriendlyByteBuf buffer) {
		this(buffer.readFloat(), buffer.readFloat());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeFloat(deadeyeMeter);
		buffer.writeFloat(deadeyeCore);
	}
}
