package com.namefix.client;

import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.registry.KeybindRegistry;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import com.namefix.data.PlayerDeadeyeState.State;
import net.minecraft.client.player.LocalPlayer;

public class DeadeyeClient {
	public static boolean DEADEYE_ENABLED = false;
	public static State DEADEYE_STATE = State.IDLE;

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

	// Request Dead Eye toggle from the server
	public static void requestDeadeye() {
		NetworkManager.sendToServer(new RequestDeadeyePayload());
	}

	// Handle server Dead Eye state
	public static void handleDeadeyeState(DeadeyeStatePayload payload, NetworkManager.PacketContext packetContext) {
		DEADEYE_ENABLED = payload.state();
	}
}
