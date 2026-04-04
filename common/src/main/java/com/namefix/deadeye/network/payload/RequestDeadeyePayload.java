package com.namefix.deadeye.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record RequestDeadeyePayload() {
	public RequestDeadeyePayload(FriendlyByteBuf buffer) {
		this();
	}

	public void write(FriendlyByteBuf buffer) {
	}
}
