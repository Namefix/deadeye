package com.namefix.deadeye.mixin.client;

import com.namefix.deadeye.DeadeyeMod;
import com.namefix.deadeye.client.DeadeyeClient;
import com.namefix.deadeye.config.DeadeyeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Inject(method = "calculatePitch", at = @At("RETURN"), cancellable = true)
	private void deadeye$modifyPitch(SoundInstance instance, CallbackInfoReturnable<Float> cir) {
		Minecraft mc = Minecraft.getInstance();
		if(!DeadeyeConfig.Client.pitchShift || mc.player == null || mc.level == null || mc.isPaused()) return;
		float originalPitch = cir.getReturnValue();

		if(DeadeyeClient.DEADEYE_ENABLED) {
			if(instance.getLocation().getNamespace().equals(DeadeyeMod.MOD_ID)) return;
			if(instance.getLocation().toString().equals("pointblank:hit_light") || instance.getLocation().toString().equals("pointblank:hit_heavy") || instance.getLocation().toString().equals("pointblank:hit_headshot")) return;
			float curTickRate = DeadeyeClient.getEffectiveCurrentTickRate();
			float prevTickRate = DeadeyeClient.getEffectivePreviousTickRate();
			if(curTickRate <= 0.0f || prevTickRate <= 0.0f) return;

			float pitchScale = prevTickRate / curTickRate;
			if(!Float.isFinite(pitchScale) || pitchScale <= 0.0f) return;

			cir.setReturnValue(originalPitch / pitchScale);
		}
	}
}
