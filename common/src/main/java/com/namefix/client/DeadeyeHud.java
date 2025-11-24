package com.namefix.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import com.namefix.config.DeadeyeConfig;
import com.namefix.util.Utils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class DeadeyeHud {

	// TEXTURES
	private static final ResourceLocation DEADEYE_MARK = ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "textures/cross.png");

	public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if(DeadeyeClient.DEADEYE_ENABLED) renderTargetMarks(guiGraphics, deltaTracker);
	}

	public static void renderTargetMarks(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		final float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
		DeadeyeClient.DEADEYE_STATE.targets.forEach((mark) -> {
			mark.incrementRenderTicks();
			float deltaRenderTicks = mark.getRenderTicks() * deltaTracker.getRealtimeDeltaTicks()*2f;

			float markSize = 5f*DeadeyeConfig.Client.targetMarkSize;
			Vec2 markPos = Utils.worldToScreen(mark.getMarkPosition(partialTick), partialTick);
			if(!Utils.isOnScreen(markPos)) return;

			float t = Math.min(deltaRenderTicks / 10f, 1f);
			guiGraphics.setColor(1f - 0.22f * t, 1f - 0.91f * t, 1f - 0.91f * t, 1.0f);

			float sizeModifier = 1;
			if(deltaRenderTicks < 5f) sizeModifier = 1.5f-(deltaRenderTicks / 10f);
			float scaledSize = markSize * sizeModifier;
			float drawX = markPos.x - scaledSize / 2f;
			float drawY = markPos.y - scaledSize / 2f;

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableDepthTest();

			// linear filtering
			RenderSystem.setShaderTexture(0, DEADEYE_MARK);
			GlStateManager._texParameter(3553, 10241, 9729); // GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR
			GlStateManager._texParameter(3553, 10240, 9729); // GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(drawX, drawY, 0d);
			float textureScale = scaledSize / 64f;
			guiGraphics.pose().scale(textureScale, textureScale, 1f);
			guiGraphics.blit(DEADEYE_MARK, 0, 0, 64, 64, 0, 0, 64, 64, 64, 64);
			guiGraphics.pose().popPose();

			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
		});
		guiGraphics.setColor(1f, 1f, 1f, 1f);
	}
}
