package com.namefix.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.namefix.DeadeyeMod;
import com.namefix.config.DeadeyeConfig;
import com.namefix.data.PlayerSavedData;
import com.namefix.util.ClientUtils;
import com.namefix.util.Utils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.IntStream;

public class DeadeyeHud {

	// SPRITES
	private static final ResourceLocation DEADEYE_MARK_SPRITE = ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, "textures/cross.png");

	private static final List<ResourceLocation> DEADEYE_LIGHTLEAK_SPRITES = IntStream.rangeClosed(1,15)
			.mapToObj(i -> ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, String.format("textures/lightleak/lightleak%02d.png", i)))
			.toList();

	private static final List<ResourceLocation> DEADEYE_CORE_SPRITES = IntStream.rangeClosed(1, 16)
			.mapToObj(i -> ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, String.format("textures/core/core%02d.png", i)))
			.toList();

	// Lightleak effect
	private static int LIGHTLEAK_FRAME = 15;
	private static float LIGHTLEAK_TIME = 0;
	private static boolean LIGHTLEAK_DIRECTION = false;

	// DEADEYE HUD
	private static final Tesselator HUD_TESSELATOR = new Tesselator();
	private static float LAST_DEADEYE_CORE = 0f;
	private static float DEADEYE_CORE_BLINK = 0f;
	private static float DEADEYE_CORE_SIZE_EFFECT = 0f;
	private static float LAST_DEADEYE_METER = 0f;
	private static float DEADEYE_METER_BLINK = 0f;

	public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if(!DeadeyeConfig.HUD.hudPosition.equals(DeadeyeConfig.HUD.HudPosition.DISABLED)) renderDeadeyeHUD(guiGraphics, deltaTracker);
		if(DeadeyeClient.DEADEYE_ENABLED) {
			renderTargetMarks(guiGraphics, deltaTracker);
			if(LIGHTLEAK_FRAME < 15) renderLightLeak(guiGraphics, deltaTracker);
		}
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
			RenderSystem.setShaderTexture(0, DEADEYE_MARK_SPRITE);
			GlStateManager._texParameter(3553, 10241, 9729); // GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR
			GlStateManager._texParameter(3553, 10240, 9729); // GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(drawX, drawY, 0d);
			float textureScale = scaledSize / 64f;
			guiGraphics.pose().scale(textureScale, textureScale, 1f);
			guiGraphics.blit(DEADEYE_MARK_SPRITE, 0, 0, 64, 64, 0, 0, 64, 64, 64, 64);
			guiGraphics.pose().popPose();

			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
		});
		guiGraphics.setColor(1f, 1f, 1f, 1f);
	}

	public static void renderLightLeak(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float) width / 2, (float) height / 2, 0f);
		guiGraphics.pose().scale(1f, 1f, 1f);
		guiGraphics.pose().translate((float) -width / 2, (float) -height / 2, 0f);
		guiGraphics.setColor(1f, 1f, 1f, 1f);
		guiGraphics.blit(
				DEADEYE_LIGHTLEAK_SPRITES.get(LIGHTLEAK_FRAME), 0, 0, -90, 0, 0, width, height, LIGHTLEAK_DIRECTION ? -width : width, height
		);
		guiGraphics.pose().popPose();

		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();

		LIGHTLEAK_TIME += deltaTracker.getRealtimeDeltaTicks();
		LIGHTLEAK_FRAME = Mth.floor(LIGHTLEAK_TIME / 0.6f);
	}

	public static void playLightLeak() {
		LIGHTLEAK_TIME = 0;
		LIGHTLEAK_FRAME = 0;
		LIGHTLEAK_DIRECTION = Minecraft.getInstance().player.getRandom().nextBoolean();
	}

	public static void renderDeadeyeHUD(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		//renderDeadeyeBackground(guiGraphics);
		renderDeadeyeCore(guiGraphics, deltaTracker);
	}

	public static void renderDeadeyeCore(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		float currentCore = DeadeyeClient.DEADEYE_DATA.deadeyeCore;

		if (LAST_DEADEYE_CORE > 20f) {
			if (
					(LAST_DEADEYE_CORE >= 60f && currentCore <= 60f) ||
					(LAST_DEADEYE_CORE >= 40f && currentCore <= 40f) ||
					(currentCore <= 20f)
			) {
				DEADEYE_CORE_BLINK = 1f;
			}
		}

		LAST_DEADEYE_CORE = currentCore;

		if (DEADEYE_CORE_BLINK > 0f) {
			DEADEYE_CORE_BLINK = Mth.clamp(DEADEYE_CORE_BLINK - deltaTracker.getRealtimeDeltaTicks() / 16f, 0f, 1f);
			int phase = (int)(DEADEYE_CORE_BLINK * 4f);
			if((phase & 1) == 1) return;
		}

		int coreSpriteIndex = Mth.clamp(Math.round(currentCore), 0, 15);
		if (coreSpriteIndex < 4) guiGraphics.setColor(0.8f, 0.075f, 0.024f, 1.0f);
		else {
			Vector3f color = PlayerSavedData.getCoreColor(currentCore);
			guiGraphics.setColor(color.x, color.y, color.z, 1.0f);
		}

		if (DeadeyeClient.DEADEYE_ENABLED && PlayerSavedData.usingDeadeyeCore(DeadeyeClient.DEADEYE_DATA)) {
			DEADEYE_CORE_SIZE_EFFECT = Mth.frac(DEADEYE_CORE_SIZE_EFFECT + deltaTracker.getRealtimeDeltaTicks() / 16f);
		} else {
			DEADEYE_CORE_SIZE_EFFECT = 0.5f;
		}

		float triangle =
			DEADEYE_CORE_SIZE_EFFECT < 0.20f ? 1f - (DEADEYE_CORE_SIZE_EFFECT / 0.20f) :
			DEADEYE_CORE_SIZE_EFFECT < 0.40f ? (DEADEYE_CORE_SIZE_EFFECT - 0.20f) / 0.20f : 1f;

		float effectScale = 0.75f + 0.25f * triangle;

		Vector2i hudPosition = ClientUtils.getHudCoordinates(guiGraphics, DeadeyeConfig.HUD.hudPosition);
		int hudScale = Math.round(16f * DeadeyeConfig.HUD.hudScale);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(hudPosition.x + hudScale / 2.0f, hudPosition.y + hudScale /2.0f, 0f);
		guiGraphics.pose().scale(effectScale, effectScale, 1f);
		guiGraphics.pose().translate(-hudScale / 2.0f, -hudScale / 2.0f, 0f);

		guiGraphics.blit(DEADEYE_CORE_SPRITES.get(coreSpriteIndex), 0, 0, -90, 0, 0, hudScale, hudScale, hudScale, hudScale);
		guiGraphics.pose().popPose();

		guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}
}
