package com.namefix.integration;

import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IntegrationRegistry {
	private static final List<DeadeyeIntegration> INTEGRATIONS = new ArrayList<>();
	private static final Set<String> REGISTERED_IDS = new HashSet<>();
	private static boolean initialized;

	private IntegrationRegistry() {}

	static {
		register(PointBlankModIntegration.INSTANCE);
		register(TACZModIntegration.INSTANCE);
		register(SAGModIntegration.INSTANCE);
		register(JEGModIntegration.INSTANCE);
	}

	public static void register(DeadeyeIntegration integration) {
		if (!REGISTERED_IDS.add(integration.id())) return;
		INTEGRATIONS.add(integration);
		if (initialized) {
			integration.initialize();
		}
	}

	public static void initialize() {
		if (initialized) return;
		initialized = true;
		for (DeadeyeIntegration integration : INTEGRATIONS) {
			integration.initialize();
		}
	}

	public static AbstractDeadeyeInteraction resolveInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		if (!initialized) initialize();
		for (DeadeyeIntegration integration : INTEGRATIONS) {
			if (!integration.isReady()) continue;
			if (!integration.matches(itemStack)) continue;
			AbstractDeadeyeInteraction interaction = integration.createInteraction(state, player, itemStack);
			if (interaction != null) return interaction;
		}
		return null;
	}
}
