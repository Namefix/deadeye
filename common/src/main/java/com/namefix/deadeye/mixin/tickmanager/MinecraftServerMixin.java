package com.namefix.deadeye.mixin.tickmanager;

import com.namefix.deadeye.util.TickManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Unique
	private long deadeye$lastTickStartNs = -1L;

	@Inject(method = "tickServer", at = @At("HEAD"))
	private void deadeye$captureTickStart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		deadeye$lastTickStartNs = System.nanoTime();
	}

	@Inject(method = "tickServer", at = @At("TAIL"))
	private void deadeye$paceServerTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		if(deadeye$lastTickStartNs <= 0L) return;
		MinecraftServer server = (MinecraftServer) (Object) this;
		long targetMspt = TickManager.getMillisecondsPerTick(server);
		if(targetMspt <= MinecraftServer.MS_PER_TICK) return;

		long extraMs = targetMspt - MinecraftServer.MS_PER_TICK;
		if(extraMs <= 0L) return;

		MinecraftServerAccessor accessor = (MinecraftServerAccessor) this;
		accessor.deadeye$setNextTickTime(accessor.deadeye$getNextTickTime() + extraMs);
	}
}
