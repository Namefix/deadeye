package com.namefix.mixin.tickmanager;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	@Accessor("nextTickTime")
	long deadeye$getNextTickTime();

	@Accessor("nextTickTime")
	void deadeye$setNextTickTime(long nextTickTime);
}