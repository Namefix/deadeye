package com.namefix.deadeye.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.deadeye.DeadeyeMod;
import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.data.PlayerSavedData;
import com.namefix.deadeye.util.ClientUtils;
import com.namefix.deadeye.util.Utils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

	private static final List<ResourceLocation> DEADEYE_METER_TRACK_SPRITES = IntStream.rangeClosed(1, 10)
			.mapToObj(i -> ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, String.format("textures/metertrack/track%02d.png", i)))
			.toList();

	private static final List<ResourceLocation> DEADEYE_METER_SPRITES = IntStream.rangeClosed(1, 100)
			.mapToObj(i -> ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MOD_ID, String.format("textures/meter/meter%02d.png", i)))
			.toList();

	// Lightleak effect
	private static int LIGHTLEAK_FRAME = 15;
	private static float LIGHTLEAK_TIME = 0;
	private static boolean LIGHTLEAK_DIRECTION = false;

	// DEADEYE HUD
	private static float LAST_DEADEYE_CORE = 0f;
	private static float DEADEYE_CORE_BLINK = 0f;
	private static float DEADEYE_CORE_SIZE_EFFECT = 0f;
	private static float LAST_DEADEYE_METER = 0f;
	private static float DEADEYE_METER_BLINK = 0f;
	private static float DEADEYE_CORE_PULSE_TIME = -1f;
	private static float LAST_PULSE_FRAME_DELTA = 0f;
	private static float DEADEYE_FADE_PULSE_TIME = -1f;
	private static float LAST_FADE_FRAME_DELTA = 0f;

	private static float INFO_COUNTER = -1f;

	private static float LEVEL_COUNTER = -1f;
	private static int LEVEL_PERCENT = 0;

	public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();

		if(!mc.options.hideGui && !mc.player.isSpectator()) {
			if (!DeadeyeConfig.HUD.hudPosition.equals(DeadeyeConfig.HUD.HudPosition.DISABLED) && DeadeyeClient.DEADEYE_DATA.deadeyeSkill > 0) {
				renderDeadeyeHUD(guiGraphics, deltaTracker);
			}

			if(DeadeyeConfig.HUD.enableInfoToast) {
				if (INFO_COUNTER != -1f) renderDeadeyeInfo(guiGraphics, deltaTracker);
				if (LEVEL_COUNTER != -1f) renderDeadeyeLevelUp(guiGraphics, deltaTracker);
			}
		}
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
		updateDeadeyeAnimations(deltaTracker);
		renderDeadeyeBackground(guiGraphics);
		renderDeadeyeCore(guiGraphics, deltaTracker);
		renderDeadeyeMeter(guiGraphics, deltaTracker);
	}

	public static void renderDeadeyeBackground(GuiGraphics guiGraphics) {
		Vector2i hudPosition = ClientUtils.getHudCoordinates(guiGraphics, DeadeyeConfig.HUD.hudPosition);
		int hudScale = Math.round(16f * DeadeyeConfig.HUD.hudScale);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		ClientUtils.drawDeadeyeCoreBackground(guiGraphics, hudPosition, hudScale);

		if (DeadeyeClient.DEADEYE_ENABLED && PlayerSavedData.usingDeadeyeMeter(DeadeyeClient.DEADEYE_DATA) && DEADEYE_FADE_PULSE_TIME >= 0f) {
			float fadeTime = Math.max(0f, DEADEYE_FADE_PULSE_TIME - LAST_FADE_FRAME_DELTA);
			if (fadeTime < 0.2f) {
				float alpha = 0.35f;
				float alphaFactor = alpha * (1f - (fadeTime / 0.2f));
				if (alphaFactor > 0f) {
					ClientUtils.drawDeadeyeCoreFadePulse(guiGraphics, hudPosition, hudScale, alphaFactor);
				}
			}
		}

		if (DEADEYE_CORE_PULSE_TIME >= 0f) {
			float visiblePulseTime = Math.max(0f, DEADEYE_CORE_PULSE_TIME - LAST_PULSE_FRAME_DELTA);
			float pulseScale = Mth.clamp(1f - (visiblePulseTime / 0.3f), 0f, 1f);
			if (pulseScale > 0f) {
				ClientUtils.drawDeadeyeCorePulse(guiGraphics, hudPosition, hudScale, pulseScale);
			}
		}

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void updateDeadeyeAnimations(DeltaTracker deltaTracker) {
		float realtimeDeltaTicks = deltaTracker.getRealtimeDeltaTicks();
		float deltaSeconds = realtimeDeltaTicks / 20f;
		boolean usingDeadeyeCore = DeadeyeClient.DEADEYE_ENABLED && PlayerSavedData.usingDeadeyeCore(DeadeyeClient.DEADEYE_DATA);
		boolean usingDeadeyeMeter = DeadeyeClient.DEADEYE_ENABLED && PlayerSavedData.usingDeadeyeMeter(DeadeyeClient.DEADEYE_DATA);

		if (usingDeadeyeCore) {
			float previousEffect = DEADEYE_CORE_SIZE_EFFECT;
			DEADEYE_CORE_SIZE_EFFECT = Mth.frac(DEADEYE_CORE_SIZE_EFFECT + realtimeDeltaTicks / 16f);
			if (DEADEYE_CORE_SIZE_EFFECT < previousEffect) {
				DEADEYE_CORE_PULSE_TIME = 0f;
				LAST_PULSE_FRAME_DELTA = 0f;
			}
		} else {
			DEADEYE_CORE_SIZE_EFFECT = 0.5f;
		}

		if (DEADEYE_CORE_PULSE_TIME >= 0f) {
			DEADEYE_CORE_PULSE_TIME += deltaSeconds;
			if (DEADEYE_CORE_PULSE_TIME >= 0.3f) {
				DEADEYE_CORE_PULSE_TIME = -1f;
			}
			LAST_PULSE_FRAME_DELTA = deltaSeconds;
		} else {
			LAST_PULSE_FRAME_DELTA = 0f;
		}

		if (usingDeadeyeMeter) {
			float cooldown = 0.25f;
			float cycleLength = 0.2f + cooldown;
			if (DEADEYE_FADE_PULSE_TIME < 0f) {
				DEADEYE_FADE_PULSE_TIME = 0f;
			}
			DEADEYE_FADE_PULSE_TIME += deltaSeconds;
			while (DEADEYE_FADE_PULSE_TIME >= cycleLength) {
				DEADEYE_FADE_PULSE_TIME -= cycleLength;
			}
			LAST_FADE_FRAME_DELTA = deltaSeconds;
		} else {
			DEADEYE_FADE_PULSE_TIME = -1f;
			LAST_FADE_FRAME_DELTA = 0f;
		}
	}

	public static void renderDeadeyeCore(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		float currentCore = DeadeyeClient.DEADEYE_DATA.deadeyeCore;

		if (LAST_DEADEYE_CORE > 20f) {
			if (
					(LAST_DEADEYE_CORE > 60f && currentCore <= 60f) ||
					(LAST_DEADEYE_CORE > 40f && currentCore <= 40f) ||
					(currentCore <= 20f)
			) {
				DEADEYE_CORE_BLINK = 1f;
			}
		}

		if(
				(LAST_DEADEYE_CORE <= 20f && currentCore > 20f) ||
				(LAST_DEADEYE_CORE <= 40f && currentCore > 40f) ||
				(LAST_DEADEYE_CORE <= 60f && currentCore > 60f)
		) {
			DEADEYE_CORE_BLINK = 1f;
		}

		LAST_DEADEYE_CORE = currentCore;

		boolean hideCoreThisFrame = false;
		if (DEADEYE_CORE_BLINK > 0f) {
			DEADEYE_CORE_BLINK = Mth.clamp(DEADEYE_CORE_BLINK - deltaTracker.getRealtimeDeltaTicks() / 16f, 0f, 1f);
			int phase = (int)(DEADEYE_CORE_BLINK * 4f);
			if((phase & 1) == 1) hideCoreThisFrame = true;
		}

		int coreSpriteIndex = Mth.clamp(Math.round(currentCore), 0, 15);
		if (coreSpriteIndex < 4) guiGraphics.setColor(0.8f, 0.075f, 0.024f, 1.0f);
		else {
			Vector3f color = PlayerSavedData.getCoreColor(currentCore);
			guiGraphics.setColor(color.x, color.y, color.z, 1.0f);
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

		if (hideCoreThisFrame) {
			guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
			return;
		}

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

	public static void renderDeadeyeMeter(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		float currentMeter = DeadeyeClient.DEADEYE_DATA.deadeyeMeter;
		PlayerSavedData data = DeadeyeClient.DEADEYE_DATA;

		boolean tonic = false;
		if(LAST_DEADEYE_METER > PlayerSavedData.getMaxMeter(data, 0)) {
			tonic = true;
			if(
				LAST_DEADEYE_METER > PlayerSavedData.getMaxMeter(data, 2) && currentMeter <= PlayerSavedData.getMaxMeter(data, 2) ||
				LAST_DEADEYE_METER > PlayerSavedData.getMaxMeter(data, 1) && currentMeter <= PlayerSavedData.getMaxMeter(data, 1) ||
				currentMeter <= 20f ||
				currentMeter > PlayerSavedData.getMaxMeter(data) && currentMeter > LAST_DEADEYE_METER
			) {
				DEADEYE_METER_BLINK = 1f;
			}
		}

		if(
				LAST_DEADEYE_METER <= PlayerSavedData.getMaxMeter(data, 2) && currentMeter > PlayerSavedData.getMaxMeter(data, 2) ||
				LAST_DEADEYE_METER <= PlayerSavedData.getMaxMeter(data, 1) && currentMeter > PlayerSavedData.getMaxMeter(data, 1) ||
				LAST_DEADEYE_METER <= PlayerSavedData.getMaxMeter(data) && currentMeter > PlayerSavedData.getMaxMeter(data)
		) {
			DEADEYE_METER_BLINK = 1f;
		}

		LAST_DEADEYE_METER = currentMeter;

		boolean hideMeterThisFrame = false;
		if (DEADEYE_METER_BLINK > 0f) {
			DEADEYE_METER_BLINK = Mth.clamp(DEADEYE_METER_BLINK - deltaTracker.getRealtimeDeltaTicks() / 16f, 0f, 1f);
			int phase = (int)(DEADEYE_METER_BLINK * 4f);
			if((phase & 1) == 1) hideMeterThisFrame = true;
		}

		if(hideMeterThisFrame) return;

		Vector2i hudPosition = ClientUtils.getHudCoordinates(guiGraphics, DeadeyeConfig.HUD.hudPosition);
		int hudScale = Math.round(16f * DeadeyeConfig.HUD.hudScale);
		Vector3f meterColor = PlayerSavedData.getMeterColor(data);

		int meterIndex = tonic ? 99 : Mth.clamp(Math.round(currentMeter), 0, 99);
		int trackIndex = Mth.clamp(data.deadeyeLevel-1, 0, 9);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();

		if(data.deadeyeLevel > 0) {
			guiGraphics.setColor(0.33f, 0.31f, 0.31f, 1.0f);
			guiGraphics.blit(DEADEYE_METER_TRACK_SPRITES.get(trackIndex), hudPosition.x, hudPosition.y, hudScale, hudScale, 0, 0, hudScale, hudScale, hudScale, hudScale);
		}

		if(Math.round(data.deadeyeMeter) > 0) {
			guiGraphics.setColor(meterColor.x, meterColor.y, meterColor.z, 1.0f);
			guiGraphics.blit(DEADEYE_METER_SPRITES.get(meterIndex), hudPosition.x, hudPosition.y, hudScale, hudScale, 0, 0, hudScale, hudScale, hudScale, hudScale);
		}

		guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	public static void renderDeadeyeInfo(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		INFO_COUNTER += deltaTracker.getRealtimeDeltaTicks() / 20f;
		float duration = 5.5f;
		float fadeDuration = 0.25f;
		float opacity;

		if(INFO_COUNTER >= duration) {
			INFO_COUNTER = -1f;
			return;
		}

		if(INFO_COUNTER < fadeDuration) {
			opacity = INFO_COUNTER / fadeDuration;
		} else if(INFO_COUNTER > duration - fadeDuration) {
			opacity = (duration - INFO_COUNTER) / fadeDuration;
 		} else {
			opacity = 1f;
		}

		Vector2i pos = new Vector2i(16, 16);
		int height = 16;
		int paddingX = 4;
		int rectColor = (Math.round(0x80 * opacity) << 24);
		int textColor = (Math.round(0xFF * opacity) << 24) | 0xFFFFFF;

		Font font = Minecraft.getInstance().font;
		String text = String.format("Dead Eye Level %d - %.1f / %.1f XP", DeadeyeClient.DEADEYE_DATA.deadeyeLevel, DeadeyeClient.DEADEYE_DATA.deadeyeXp, PlayerSavedData.requiredXPToLevelUp(DeadeyeClient.DEADEYE_DATA.deadeyeLevel));
		int textY = pos.y + (height - font.lineHeight) / 2 + 1;
		int textWidth = font.width(text);
		int width = textWidth + (paddingX * 2);

		guiGraphics.fill(pos.x, pos.y, pos.x + width, pos.y + height, rectColor);
		guiGraphics.drawString(font, text, pos.x + paddingX, textY, textColor);
	}

	public static void showDeadeyeInfo() {
		INFO_COUNTER = 0f;
	}

	public static boolean isDeadeyeInfoVisible() {
		return INFO_COUNTER >= 0;
	}

	public static void renderDeadeyeLevelUp(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		LEVEL_COUNTER += deltaTracker.getRealtimeDeltaTicks() / 20f;
		float duration = 5.5f;
		float fadeDuration = 0.25f;
		float opacity;

		if(LEVEL_COUNTER >= duration) {
			LEVEL_COUNTER = -1f;
			return;
		}

		if(LEVEL_COUNTER < fadeDuration) {
			opacity = LEVEL_COUNTER / fadeDuration;
		} else if(LEVEL_COUNTER > duration - fadeDuration) {
			opacity = (duration - LEVEL_COUNTER) / fadeDuration;
		} else {
			opacity = 1f;
		}

		Vector2i pos = new Vector2i(16, 16);
		if(INFO_COUNTER != -1f) pos.y += 16;
		int height = 16;
		int paddingX = 4;
		int rectColor = (Math.round(0x80 * opacity) << 24);
		int textColor = (Math.round(0xFF * opacity) << 24) | 0xFFFFFF;

		Font font = Minecraft.getInstance().font;
		String text = LEVEL_PERCENT < 100 ? String.format("Dead Eye %d%% to Level %d", LEVEL_PERCENT, DeadeyeClient.DEADEYE_DATA.deadeyeLevel + 1) : String.format("Dead Eye Level %d reached!", DeadeyeClient.DEADEYE_DATA.deadeyeLevel);
		int textY = pos.y + (height - font.lineHeight) / 2 + 1;
		int textWidth = font.width(text);
		int width = textWidth + (paddingX * 2);

		guiGraphics.fill(pos.x, pos.y, pos.x + width, pos.y + height, rectColor);
		guiGraphics.drawString(font, text, pos.x + paddingX, textY, textColor);
	}

	public static void showDeadeyeLevelUp(int percent) {
		if(LEVEL_COUNTER != -1f) return;
		LEVEL_COUNTER = 0f;
		LEVEL_PERCENT = percent;
	}
}
