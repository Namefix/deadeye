package com.namefix.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record DeadeyeThresholdPayload(int threshold) {
	public DeadeyeThresholdPayload(FriendlyByteBuf buffer) {
		this(buffer.readInt());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(threshold);
	}
}
