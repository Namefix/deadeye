package com.namefix.deadeye.mixin.client.tickmanager;

import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface TimerAccessor {
	@Mutable
	@Accessor("msPerTick")
	void deadeye$setMsPerTick(float msPerTick);
}