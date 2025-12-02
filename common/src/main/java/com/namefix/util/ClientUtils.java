package com.namefix.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.namefix.config.DeadeyeConfig;
import com.namefix.data.DeadeyeTargetData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import static com.namefix.client.DeadeyeClient.DEADEYE_STATE;

public class ClientUtils {
	private static final Tesselator HUD_TESSELATOR = new Tesselator();

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

	public static void drawDeadeyeCoreBackground(GuiGraphics guiGraphics, Vector2i hudPosition, int hudScale) {
		if (hudScale <= 0) return;

		float radius = hudScale * 0.425f;
		if (radius <= 0f) return;

		float centerX = hudPosition.x + hudScale / 2.0f + 0.25f;
		float centerY = hudPosition.y + hudScale / 2.0f;
		guiGraphics.flush();
		drawHudCircle(guiGraphics, centerX, centerY, radius, 12, 8, 14, Mth.clamp(Math.round(0.75f * 255f), 0, 255));
	}

	public static void drawDeadeyeCorePulse(GuiGraphics guiGraphics, Vector2i hudPosition, int hudScale, float scale) {
		if (hudScale <= 0 || scale <= 0f) return;

		float radius = hudScale * 0.425f * scale;
		if (radius <= 0f) return;

		float centerX = hudPosition.x + hudScale / 2.0f + 0.25f;
		float centerY = hudPosition.y + hudScale / 2.0f;
		guiGraphics.flush();
		drawHudCircle(guiGraphics, centerX, centerY, radius, 156, 9, 13, 242);
	}

	public static void drawDeadeyeCoreFadePulse(GuiGraphics guiGraphics, Vector2i hudPosition, int hudScale, float alphaFactor) {
		if (hudScale <= 0 || alphaFactor <= 0f) return;

		float radius = hudScale * 0.425f;
		if (radius <= 0f) return;

		float centerX = hudPosition.x + hudScale / 2.0f + 0.25f;
		float centerY = hudPosition.y + hudScale / 2.0f;
		int alpha = Mth.clamp(Math.round(alphaFactor * 255f), 0, 255);
		if (alpha <= 0) return;
		guiGraphics.flush();
		drawHudCircle(guiGraphics, centerX, centerY, radius, 156, 9, 13, alpha);
	}

	private static void drawHudCircle(GuiGraphics guiGraphics, float centerX, float centerY, float radius, int red, int green, int blue, int alpha) {
		if (radius <= 0f) return;
		float zLevel = -90f;
		Matrix4f poseMatrix = guiGraphics.pose().last().pose();
		BufferBuilder bufferBuilder = HUD_TESSELATOR.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		int segmentCount = Math.max(48, Mth.ceil(radius * 1.5f));
		bufferBuilder.addVertex(poseMatrix, centerX, centerY, zLevel).setColor(red, green, blue, alpha);
		for (int i = 0; i <= segmentCount; i++) {
			float angle = (float)(Math.PI * 2f * i / (float)segmentCount);
			float x = centerX + Mth.cos(angle) * radius;
			float y = centerY + Mth.sin(angle) * radius;
			bufferBuilder.addVertex(poseMatrix, x, y, zLevel).setColor(red, green, blue, alpha);
		}
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		RenderSystem.enableCull();
	}
}