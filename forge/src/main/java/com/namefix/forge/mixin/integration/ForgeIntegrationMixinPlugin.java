package com.namefix.forge.mixin.integration;

import com.namefix.mixin.integration.IntegrationMixinPluginBase;
import net.minecraftforge.fml.ModList;

public final class ForgeIntegrationMixinPlugin extends IntegrationMixinPluginBase {
	@Override
	protected boolean isModLoaded(String modId) {
		try {
			Class<?> fmlLoaderClass = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
			Object loadingModList = fmlLoaderClass.getMethod("getLoadingModList").invoke(null);
			if (loadingModList != null) {
				Object modFile = loadingModList.getClass().getMethod("getModFileById", String.class).invoke(loadingModList, modId);
				return modFile != null;
			}
		} catch (Throwable ignored) {
		}

		ModList modList = ModList.get();
		if (modList != null) {
			return modList.isLoaded(modId);
		}

		return true;
	}

	@Override
	protected void registerMixins() {
		//pointblank
		registerIntegrationMixin("com.namefix.forge.mixin.integration.pointblank.PointBlankGunMixin", "pointblank");
		registerIntegrationMixin("com.namefix.forge.mixin.integration.pointblank.PointBlankFireModeFeatureMixin", "pointblank");

		//jeg
		registerIntegrationMixin("com.namefix.forge.mixin.integration.jeg.JEGShootingHandlerCooldownMixin", "jeg");
		registerIntegrationMixin("com.namefix.forge.mixin.integration.jeg.JEGAnimatedGunItemMixin", "jeg");
		registerIntegrationMixin("com.namefix.forge.mixin.integration.jeg.JEGProjectileEntityMixin", "jeg");

		//tacz
		registerIntegrationMixin("com.namefix.forg.emixin.integration.tacz.TACZInaccuracyTypeMixin", "tacz");
		registerIntegrationMixin("com.namefix.forg.emixin.integration.tacz.TACZLivingEntityDrawGunMixin", "tacz");
		registerIntegrationMixin("com.namefix.forg.emixin.integration.tacz.TACZModernKineticGunItemMixin", "tacz");
	}
}
