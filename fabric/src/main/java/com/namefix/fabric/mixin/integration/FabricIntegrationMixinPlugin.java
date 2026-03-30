package com.namefix.fabric.mixin.integration;

import com.namefix.mixin.integration.IntegrationMixinPluginBase;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricIntegrationMixinPlugin extends IntegrationMixinPluginBase {
	@Override
	protected boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	protected void registerMixins() {
		registerIntegrationMixin("com.namefix.fabric.mixin.integration.pointblank.PointBlankGunMixin", "pointblank");
		registerIntegrationMixin("com.namefix.fabric.mixin.integration.pointblank.PointBlankFireModeFeatureMixin", "pointblank");
		registerIntegrationMixin("com.namefix.fabric.mixin.integration.pointblank.PointBlankGunClientMixin", "pointblank");
		registerIntegrationMixin("com.namefix.fabric.mixin.integration.pointblank.PointBlankGunClientStateAccessor", "pointblank");
	}
}
