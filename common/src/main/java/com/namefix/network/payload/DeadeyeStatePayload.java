package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.FriendlyByteBuf;

public record DeadeyeStatePayload(boolean state, float previousTickrate, int phase) {
	public DeadeyeStatePayload(FriendlyByteBuf buffer) {
		this(buffer.readBoolean(), buffer.readFloat(), buffer.readInt());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(state);
		buffer.writeFloat(previousTickrate);
		buffer.writeInt(phase);
	}
}
