package com.namefix.client;

import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.registry.KeybindRegistry;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class DeadeyeClient {
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
			NetworkManager.sendToServer(new RequestDeadeyePayload());
			return EventResult.interruptDefault();
		}
		return EventResult.pass();

	}
}
