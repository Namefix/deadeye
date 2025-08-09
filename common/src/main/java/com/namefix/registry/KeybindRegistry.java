package com.namefix.registry;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;

public class KeybindRegistry {
	public static final KeyMapping DEADEYE_TOGGLE = new KeyMapping("key.deadeye.toggle_deadeye", InputConstants.KEY_CAPSLOCK, "key.categories.deadeye");

	public static void register() {
		KeyMappingRegistry.register(DEADEYE_TOGGLE);
	}
}
