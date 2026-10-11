package com.namefix.deadeye.neoforge.mixin.integration.ironsartifice;

import com.namefix.deadeye.client.DeadeyeClient;
import io.redspace.irons_artifice.client.RecoilManager;
import io.redspace.irons_artifice.gun.ShotProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecoilManager.class)
public class IronsArtificeRecoilManagerMixin {
	@Inject(method = "applyRecoil", at = @At("HEAD"), cancellable = true)
	private static void deadeye$modifyApplyRecoil(ShotProfile shotProfile, CallbackInfo ci) {
		if(DeadeyeClient.DEADEYE_ENABLED) ci.cancel();
	}
}
