package com.namefix.deadeye.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record InformShootingPhasePayload() {
	public InformShootingPhasePayload(FriendlyByteBuf buffer) {
		this();
	}

	public void write(FriendlyByteBuf buffer) {
	}
}
