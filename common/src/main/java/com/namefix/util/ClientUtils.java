package com.namefix.util;

import com.namefix.config.DeadeyeConfig;
import com.namefix.data.DeadeyeTargetData;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2i;

import static com.namefix.client.DeadeyeClient.DEADEYE_STATE;

public class ClientUtils {
	public static DeadeyeTargetData getNextValidTarget() {
		while(!DEADEYE_STATE.targets.isEmpty()) {
			DeadeyeTargetData candidate = DEADEYE_STATE.targets.getFirst();
			if(candidate != null && !candidate.isInvalid()) return candidate;
			DEADEYE_STATE.targets.removeFirst();
		}
		return null;
	}

	public static Vector2i getHudCoordinates(GuiGraphics ctx, DeadeyeConfig.HUD.HudPosition pos) {
		switch(pos) {
			case TOP_LEFT -> {
				return new Vector2i(4, 4);
			}
			case TOP_RIGHT -> {
				return new Vector2i(ctx.guiWidth()-20, 4);
			}
			case BOTTOM_LEFT -> {
				return new Vector2i(4, ctx.guiHeight()-20);
			}
			case BOTTOM_RIGHT -> {
				return new Vector2i(ctx.guiWidth()-20, ctx.guiHeight()-20);
			}
			case NEAR_HOTBAR -> {
				return new Vector2i((ctx.guiWidth()/4)-20, ctx.guiHeight()-20);
			}
			case CUSTOM -> {
				return new Vector2i(DeadeyeConfig.HUD.hudCustomX, DeadeyeConfig.HUD.hudCustomY);
			}
		}
		return new Vector2i(0, 0);
	}
}
