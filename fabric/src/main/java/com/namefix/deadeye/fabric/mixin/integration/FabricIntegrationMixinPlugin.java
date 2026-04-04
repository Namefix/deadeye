package com.namefix.deadeye.fabric.mixin.integration;

import com.namefix.deadeye.mixin.integration.IntegrationMixinPluginBase;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricIntegrationMixinPlugin extends IntegrationMixinPluginBase {
	@Override
	protected boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	protected void registerMixins() {
		registerIntegrationMixin("com.namefix.deadeye.fabric.integration.pointblank.PointBlankGunMixin", "pointblank");
		registerIntegrationMixin("com.namefix.deadeye.fabric.integration.pointblank.PointBlankFireModeFeatureMixin", "pointblank");
		registerIntegrationMixin("com.namefix.deadeye.fabric.integration.pointblank.PointBlankGunClientMixin", "pointblank");
		registerIntegrationMixin("com.namefix.deadeye.fabric.integration.pointblank.PointBlankGunClientStateAccessor", "pointblank");
	}
}
