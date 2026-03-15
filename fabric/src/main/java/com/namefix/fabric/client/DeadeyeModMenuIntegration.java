package com.namefix.fabric.client;

import com.namefix.DeadeyeMod;
import com.namefix.config.DeadeyeConfig;
import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import com.teamresourceful.resourcefulconfig.common.config.ResourcefulConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

public final class DeadeyeModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ResourcefulConfig config = DeadeyeMod.CONFIGURATOR.getConfig(DeadeyeConfig.class);
			if(config == null) return parent;
			return new ConfigScreen((Screen) parent, null, config);
		};
	}
}
