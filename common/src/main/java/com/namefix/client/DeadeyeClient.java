package com.namefix.client;

import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.registry.KeybindRegistry;
import com.namefix.shader.ShaderManager;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.minecraft.client.Minecraft;
import com.namefix.data.PlayerDeadeyeState.State;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class DeadeyeClient {
	public static boolean DEADEYE_ENABLED = false;
	public static State DEADEYE_STATE = State.IDLE;
	public static float PREVIOUS_TICK_RATE = -1.0f;

	public static void initialize() {
		modifyBowAnimations();
	}

	public static void onQuit(LocalPlayer localPlayer) {
		DEADEYE_ENABLED = false;
	}

	public static EventResult onKeyPressed(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if(
			minecraft.level == null ||
			minecraft.player == null ||
			minecraft.player.isSpectator() ||
			!minecraft.player.isAlive() ||
			minecraft.screen != null
		) {
			return EventResult.pass();
		}

		if (KeybindRegistry.DEADEYE_TOGGLE.matches(keyCode, scanCode) && action == 1) {
			requestDeadeye();
			return EventResult.interruptDefault();
		}

		return EventResult.pass();
	}

	private static void setDeadeyeState(boolean enabled) {
		DEADEYE_ENABLED = enabled;

		if(enabled) {
			ShaderManager.activateShader("rdr2_deadeye");
		} else {
			ShaderManager.deactivateShader("rdr2_deadeye");
		}
	}

	// Request Dead Eye toggle from the server
	public static void requestDeadeye() {
		NetworkManager.sendToServer(new RequestDeadeyePayload());
	}

	// Handle server Dead Eye state
	public static void handleDeadeyeState(DeadeyeStatePayload payload, NetworkManager.PacketContext packetContext) {
		setDeadeyeState(payload.state());
		PREVIOUS_TICK_RATE = payload.previousTickrate();
	}

	// Modify bow pulling animations
	public static void modifyBowAnimations() {
		ItemPropertiesRegistry.register(Items.BOW, ResourceLocation.parse("pull"), (itemStack, world, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			}
			if (entity.isUsingItem() && entity.getUseItem() == itemStack) {
				int useTicks = entity.getTicksUsingItem();

				return Math.min(useTicks / 20.0f, 1.0f);
			}
			return 0.0F;
		});

		ItemPropertiesRegistry.register(Items.CROSSBOW, ResourceLocation.parse("pull"), (itemStack, world, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			}
			if (entity.isUsingItem() && entity.getUseItem() == itemStack) {
				int useTicks = entity.getTicksUsingItem();

				return Math.min(useTicks / 20.0f, 1.0f);
			}
			return 0.0F;
		});
	}
}
