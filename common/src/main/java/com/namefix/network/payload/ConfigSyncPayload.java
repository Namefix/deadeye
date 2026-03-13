package com.namefix.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record ConfigSyncPayload(boolean bowPullCompensation, boolean instantGunReload) {
	public ConfigSyncPayload(FriendlyByteBuf buffer) {
		this(buffer.readBoolean(), buffer.readBoolean());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(bowPullCompensation);
		buffer.writeBoolean(instantGunReload);
	}
}
