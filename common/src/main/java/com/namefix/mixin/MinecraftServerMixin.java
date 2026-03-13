package com.namefix.mixin;

import com.namefix.util.TickManager;
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

		long elapsedNs = System.nanoTime() - deadeye$lastTickStartNs;
		long targetNs = targetMspt * 1_000_000L;
		long sleepNs = targetNs - elapsedNs;
		if(sleepNs <= 0L) return;

		long sleepMs = sleepNs / 1_000_000L;
		int sleepExtraNs = (int) (sleepNs % 1_000_000L);
		try {
			Thread.sleep(sleepMs, sleepExtraNs);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}
}
