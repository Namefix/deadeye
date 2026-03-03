package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigSyncPayload(boolean bowPullCompensation, boolean instantGunReload) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, ConfigSyncPayload::bowPullCompensation, ByteBufCodecs.BOOL, ConfigSyncPayload::instantGunReload, ConfigSyncPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() { return DeadeyeNetwork.CONFIG_SYNC; }
}
