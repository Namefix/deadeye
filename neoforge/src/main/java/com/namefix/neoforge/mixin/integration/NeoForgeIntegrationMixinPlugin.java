package com.namefix.neoforge.mixin.integration;

import com.namefix.mixin.integration.IntegrationMixinPluginBase;
import net.neoforged.fml.ModList;

public final class NeoForgeIntegrationMixinPlugin extends IntegrationMixinPluginBase {
	@Override
	protected boolean isModLoaded(String modId) {
		ModList modList = ModList.get();
		return modList != null && modList.isLoaded(modId);
	}

	@Override
	protected void registerMixins() {
		registerIntegrationMixin("com.namefix.neoforge.mixin.integration.pointblank.PointBlankGunMixin", "pointblank");
	}
}
