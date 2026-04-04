package com.namefix.deadeye.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record LevelDataPayload(int deadeyeSkill, int deadeyeLevel, float deadeyeXp) {
	public LevelDataPayload(FriendlyByteBuf buffer) {
		this(buffer.readInt(), buffer.readInt(), buffer.readFloat());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(deadeyeSkill);
		buffer.writeInt(deadeyeLevel);
		buffer.writeFloat(deadeyeXp);
	}
}
