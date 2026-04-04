package com.namefix.deadeye.registry;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;

public class KeybindRegistry {
	public static final KeyMapping DEADEYE_TOGGLE = new KeyMapping("key.deadeye.toggle_deadeye", InputConstants.KEY_CAPSLOCK, "key.categories.deadeye");
	public static final KeyMapping DEADEYE_MARK = new KeyMapping("key.deadeye.mark", InputConstants.KEY_GRAVE, "key.categories.deadeye");
	public static final KeyMapping DEADEYE_SHOOT_TARGETS = new KeyMapping("key.deadeye.shoot_targets", InputConstants.KEY_G, "key.categories.deadeye");
	public static final KeyMapping DEADEYE_INFO_SHOW = new KeyMapping("key.deadeye.info_show", InputConstants.KEY_LALT, "key.categories.deadeye");

	public static void register() {
		KeyMappingRegistry.register(DEADEYE_TOGGLE);
		KeyMappingRegistry.register(DEADEYE_MARK);
		KeyMappingRegistry.register(DEADEYE_SHOOT_TARGETS);
		KeyMappingRegistry.register(DEADEYE_INFO_SHOW);
	}
}
