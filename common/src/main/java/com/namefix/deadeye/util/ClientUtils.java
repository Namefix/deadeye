package com.namefix.deadeye.util;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.namefix.deadeye.config.DeadeyeConfig;
import com.namefix.deadeye.config.SyncedConfigCache;
import com.namefix.deadeye.data.DeadeyeTargetData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static com.namefix.deadeye.client.DeadeyeClient.DEADEYE_STATE;

public class ClientUtils {
	private static final Tesselator HUD_TESSELATOR = new Tesselator();

	public static DeadeyeTargetData getNextValidTarget() {
		if(!DEADEYE_STATE.targets.isEmpty()) {
			return DEADEYE_STATE.targets.get(0);
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

		float radius = hudScale * 0.375f;
		if (radius <= 0f) return;

		float centerX = hudPosition.x + hudScale / 2.0f + 0.25f;
		float centerY = hudPosition.y + hudScale / 2.0f;
		guiGraphics.flush();
		drawHudCircle(guiGraphics, centerX, centerY, radius, 12, 8, 14, Mth.clamp(Math.round(0.75f * 255f), 0, 255));
	}

	public static void drawDeadeyeCorePulse(GuiGraphics guiGraphics, Vector2i hudPosition, int hudScale, float scale) {
		if (hudScale <= 0 || scale <= 0f) return;

		float radius = hudScale * 0.375f * scale;
		if (radius <= 0f) return;

		float centerX = hudPosition.x + hudScale / 2.0f + 0.25f;
		float centerY = hudPosition.y + hudScale / 2.0f;
		guiGraphics.flush();
		drawHudCircle(guiGraphics, centerX, centerY, radius, 156, 9, 13, 200);
	}

	public static void drawDeadeyeCoreFadePulse(GuiGraphics guiGraphics, Vector2i hudPosition, int hudScale, float alphaFactor) {
		if (hudScale <= 0 || alphaFactor <= 0f) return;

		float radius = hudScale * 0.375f;
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

	public static HitResult raycastFromPlayer(Player player, double maxDistance) {
		if(player == null) return null;
		Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
		if(cameraEntity == null) cameraEntity = player;

		Vec3 cameraPos = player.getEyePosition(1.0f);
		Vec3 cameraRot = player.getViewVector(1.0f);
		Vec3 raycastContext = cameraPos.add(cameraRot.x * maxDistance, cameraRot.y * maxDistance, cameraRot.z * maxDistance);

		HitResult blockHit = player.level().clip(new ClipContext(cameraPos, raycastContext, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));

		double maxDistanceSqr = maxDistance * maxDistance;
		if(blockHit.getType() != HitResult.Type.MISS) {
			maxDistanceSqr = cameraPos.distanceToSqr(blockHit.getLocation());
		}

		double searchDistance = Math.sqrt(maxDistanceSqr);
		Vec3 entitySearchEnd = cameraPos.add(cameraRot.x * searchDistance, cameraRot.y * searchDistance, cameraRot.z * searchDistance);
		AABB box = cameraEntity.getBoundingBox().expandTowards(cameraRot.scale(searchDistance)).inflate(1.0d, 1.0d, 1.0d);

		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(cameraEntity, cameraPos, entitySearchEnd, box, (entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity), maxDistanceSqr);

		return entityHit != null ? entityHit : blockHit;
	}

	public static Vec2 worldToScreen(Vec3 worldPos, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		GameRenderer renderer = minecraft.gameRenderer;
		Camera camera = renderer.getMainCamera();
		Window window = minecraft.getWindow();

		int guiWidth = window.getGuiScaledWidth();
		int guiHeight = window.getGuiScaledHeight();
		if(guiWidth <= 0 || guiHeight <= 0) return null;

		Vec3 cameraPos = camera.getPosition();
		Vector3f relative = new Vector3f((float)(worldPos.x - cameraPos.x), (float)(worldPos.y - cameraPos.y), (float)(worldPos.z - cameraPos.z));

		Quaternionf rotation = new Quaternionf(camera.rotation()).conjugate();
		relative.rotate(rotation);
		if(relative.z >= 0f) return null;

		double fov = resolveFov(minecraft, camera, partialTick);
		Matrix4f projection = renderer.getProjectionMatrix(fov);

		Vector4f clipSpace = new Vector4f(relative.x, relative.y, relative.z, 1.0f);
		projection.transform(clipSpace);

		if(clipSpace.w <= 0f) return null;

		float ndcX = clipSpace.x / clipSpace.w;
		float ndcY = clipSpace.y / clipSpace.w;

		float screenX = (ndcX * 0.5f + 0.5f) * (float) guiWidth;
		float screenY = (0.5f - ndcY * 0.5f) * (float) guiHeight;

		return new Vec2(screenX, screenY);
	}

	private static double resolveFov(Minecraft minecraft, Camera camera, float partialTick) {
		GameRenderer renderer = minecraft.gameRenderer;
		if(renderer.isPanoramicMode()) return 90.0d;

		double baseFov;
		Integer optionFov = minecraft.options.fov().get();
		baseFov = optionFov.doubleValue();

		float fovMultiplier = 1.0f;
		Entity entity = camera.getEntity();
		if(entity instanceof AbstractClientPlayer clientPlayer) {
			fovMultiplier = clientPlayer.getFieldOfViewModifier();
		}

		double fov = baseFov * fovMultiplier;

		if(entity instanceof LivingEntity living && living.isDeadOrDying()) {
			float deathProgress = Math.min(living.deathTime + partialTick, 20.0f);
			float deathModifier = 1.0f + 2.0f * (1.0f - 500.0f / (deathProgress + 500.0f));
			if(deathModifier > 0.0f) fov /= deathModifier;
		}

		FogType fluidType = camera.getFluidInCamera();
		if(fluidType == FogType.LAVA || fluidType == FogType.WATER) {
			double effectScale;
			effectScale = minecraft.options.fovEffectScale().get();
			effectScale = Mth.clamp(effectScale, 0.0d, 1.0d);
			fov *= Mth.lerp(effectScale, 1.0d, 0.8571428656578064d);
		}

		return fov;
	}

	public static boolean isOnScreen(Vec2 screenPos) {
		if(screenPos == null) return false;
		Minecraft minecraft = Minecraft.getInstance();
		Window window = minecraft.getWindow();

		float screenWidth = (float)window.getGuiScaledWidth();
		float screenHeight = (float)window.getGuiScaledHeight();

		if(Float.isNaN(screenPos.x) || Float.isNaN(screenPos.y)) return false;
		if(Float.isInfinite(screenPos.x) || Float.isInfinite(screenPos.y)) return false;

		return screenPos.x >= 0f && screenPos.x <= screenWidth && screenPos.y >= 0f && screenPos.y <= screenHeight;
	}

	public static void onReloadServerConfigButtonClicked() {
		Minecraft mc = Minecraft.getInstance();
		if(mc.level == null) return;
		if(mc.getSingleplayerServer() == null) return;
		SyncedConfigCache.reloadAndSyncServer(mc.getSingleplayerServer());
	}
}