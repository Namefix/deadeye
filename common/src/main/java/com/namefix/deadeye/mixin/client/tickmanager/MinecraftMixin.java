package com.namefix.deadeye.mixin.client.tickmanager;

import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.util.TickManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow @Final private Timer timer;
	@Unique
	private static final float DEADEYE_CLIENT_TICKRATE_STEP = 6.0f;

	@Unique
	private float deadeye$appliedTickRate = 20.0f;

	@Inject(method = "runTick", at = @At("HEAD"))
	private void deadeye$applyClientTickRate(boolean renderLevel, CallbackInfo ci) {
		Minecraft minecraft = (Minecraft) (Object) this;
		TickManager.beginClientFrame(minecraft);

		float targetTickRate = DeadeyeClient.DEADEYE_ENABLED
			? DeadeyeClient.getEffectiveCurrentTickRate()
			: 20.0f;

		if(!Float.isFinite(targetTickRate) || targetTickRate <= 0.0f) {
			targetTickRate = 20.0f;
		}

		if(Math.abs(targetTickRate - deadeye$appliedTickRate) > 0.01f) {
			if(targetTickRate > deadeye$appliedTickRate) {
				deadeye$appliedTickRate = Math.min(targetTickRate, deadeye$appliedTickRate + DEADEYE_CLIENT_TICKRATE_STEP);
			} else {
				deadeye$appliedTickRate = Math.max(targetTickRate, deadeye$appliedTickRate - DEADEYE_CLIENT_TICKRATE_STEP);
			}
		} else {
			deadeye$appliedTickRate = targetTickRate;
		}

		float msPerTick = 1000.0f / Math.max(0.1f, deadeye$appliedTickRate);
		((TimerAccessor) timer).deadeye$setMsPerTick(msPerTick);
	}

	@Inject(method = "runTick", at = @At("TAIL"))
	private void deadeye$finishClientFrame(boolean renderLevel, CallbackInfo ci) {
		TickManager.endClientFrame((Minecraft) (Object) this);
	}
}
