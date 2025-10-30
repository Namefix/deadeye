package com.namefix.util;

import com.mojang.blaze3d.platform.Window;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.BowDeadeyeInteraction;
import com.namefix.interactions.ProjectileDeadeyeInteraction;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Utils {
	public static Vec2 getHeadingFromTarget(Entity entity, EntityAnchorArgument.Anchor anchorPoint, Vec3 target) {
		Vec3 vec3d = anchorPoint.apply(entity);
		double d = target.x - vec3d.x;
		double e = target.y - vec3d.y;
		double f = target.z - vec3d.z;
		double g = Math.sqrt(d * d + f * f);
		float pitch = Mth.wrapDegrees((float)(-(Mth.atan2(e, g) * 57.2957763671875)));
		float yaw = Mth.wrapDegrees((float)(Mth.atan2(f, d) * 57.2957763671875) - 90.0F);
		return new Vec2(pitch, yaw);
	}

	public static AbstractDeadeyeInteraction getDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		Item item = itemStack.getItem();

		if(item instanceof BowItem || item instanceof CrossbowItem) return new BowDeadeyeInteraction(state, player, itemStack);
		else if(item instanceof ProjectileItem) return new ProjectileDeadeyeInteraction(state, player, itemStack);
		else return null;
	}

	public static HitResult raycastFromPlayer(Player player, double maxDistance) {
		Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
		if(cameraEntity == null) return null;

		Vec3 cameraPos = player.getEyePosition(1.0f);
		Vec3 cameraRot = player.getViewVector(1.0f);
		Vec3 raycastContext = cameraPos.add(cameraRot.x * maxDistance, cameraRot.y * maxDistance, cameraRot.z * maxDistance);
		AABB box = cameraEntity.getBoundingBox().expandTowards(cameraRot.scale(maxDistance)).expandTowards(1d,1d,1d);
		return ProjectileUtil.getEntityHitResult(cameraEntity, cameraPos, raycastContext, box, (entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity), maxDistance);
	}

	// convert world space coordinates to screenspace
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

}
