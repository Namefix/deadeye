package com.namefix.neoforge.mixin.integration;

import com.namefix.mixin.integration.IntegrationMixinPluginBase;
import net.neoforged.fml.ModList;

public final class NeoForgeIntegrationMixinPlugin extends IntegrationMixinPluginBase {
	@Override
	protected boolean isModLoaded(String modId) {
		ModList modList = ModList.get();
		if (modList != null) {
			return modList.isLoaded(modId);
		}

		try {
			Class<?> fmlLoaderClass = Class.forName("net.neoforged.fml.loading.FMLLoader");
			Object loadingModList = fmlLoaderClass.getMethod("getLoadingModList").invoke(null);
			if (loadingModList != null) {
				Object modFile = loadingModList.getClass().getMethod("getModFileById", String.class).invoke(loadingModList, modId);
				return modFile != null;
			}
		} catch (Throwable ignored) {
		}

		return true;
	}

	@Override
	protected void registerMixins() {
		registerIntegrationMixin("com.namefix.neoforge.mixin.integration.pointblank.PointBlankGunMixin", "pointblank");
		registerIntegrationMixin("com.namefix.neoforge.mixin.integration.pointblank.PointBlankFireModeFeatureMixin", "pointblank");
	}
}
