package com.namefix.deadeye.util;

import com.mojang.blaze3d.platform.Window;
import com.namefix.deadeye.data.PlayerDeadeyeState;
import com.namefix.deadeye.integration.IntegrationRegistry;
import com.namefix.deadeye.interactions.AbstractDeadeyeInteraction;
import com.namefix.deadeye.interactions.BowDeadeyeInteraction;
import com.namefix.deadeye.interactions.ProjectileDeadeyeInteraction;
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

	// entity movement prediction stuff
	public static Vec3 predictLeadPosition(Entity target, Vec3 shooterPos, double projectileSpeed) {
		if(target == null) return null;
		if(projectileSpeed <= 0.0d || shooterPos == null) return resolveAimBase(target);

		Vec3 aimBase = resolveAimBase(target);
		Vec3 velocity = target.getDeltaMovement();
		Vec3 delta = aimBase.subtract(shooterPos);

		double a = velocity.lengthSqr() - projectileSpeed * projectileSpeed;
		double b = 2.0d * delta.dot(velocity);
		double c = delta.lengthSqr();

		double time;
		if(Math.abs(a) < 1.0e-6d) {
			double denom = b;
			if(Math.abs(denom) < 1.0e-6d) time = 0.0d;
			else time = -c / denom;
		} else {
			double discriminant = b * b - 4.0d * a * c;
			if(discriminant < 0.0d) {
				time = -1.0d;
			} else {
				double sqrt = Math.sqrt(discriminant);
				double t1 = (-b - sqrt) / (2.0d * a);
				double t2 = (-b + sqrt) / (2.0d * a);
				time = Double.MAX_VALUE;
				if(t1 > 0.0d) time = Math.min(time, t1);
				if(t2 > 0.0d) time = Math.min(time, t2);
				if(time == Double.MAX_VALUE) time = -1.0d;
			}
		}

		if(time <= 0.0d) return aimBase;
		return aimBase.add(velocity.scale(time));
	}

	public static float solveBallisticPitch(Vec3 shooterPos, Vec3 aimPos, double projectileSpeed, double gravity) {
		if(shooterPos == null || aimPos == null) return 0.0f;
		Vec3 delta = aimPos.subtract(shooterPos);
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		if(horizontal < 1.0e-6d) {
			return (float) Mth.wrapDegrees(-Math.toDegrees(Math.atan2(delta.y, horizontal <= 0.0d ? 1.0e-6d : horizontal)));
		}
		if(projectileSpeed <= 0.0d || gravity <= 0.0d) {
			return (float)(-Math.toDegrees(Math.atan2(delta.y, horizontal)));
		}
		double speedSq = projectileSpeed * projectileSpeed;
		double speedFourth = speedSq * speedSq;
		double y = delta.y;
		double gh2 = gravity * horizontal * horizontal;
		double discriminant = speedFourth - gravity * (gh2 + 2.0d * y * speedSq);
		if(discriminant < 0.0d) {
			discriminant = 0.0d;
		}
		double sqrt = Math.sqrt(discriminant);
		double denom = gravity * horizontal;
		if(Math.abs(denom) < 1.0e-6d) {
			return (float)(-Math.toDegrees(Math.atan2(delta.y, horizontal)));
		}
		double angleLow = Math.atan((speedSq - sqrt) / denom);
		double angleHigh = Math.atan((speedSq + sqrt) / denom);
		double direct = Math.atan2(delta.y, horizontal);
		double chosen = angleLow;
		if(!Double.isFinite(angleLow)) {
			chosen = angleHigh;
		} else if(Double.isFinite(angleHigh)) {
			double pitchLow = Math.abs(Math.toDegrees(angleLow - direct));
			double pitchHigh = Math.abs(Math.toDegrees(angleHigh - direct));
			chosen = pitchLow <= pitchHigh ? angleLow : angleHigh;
		}
		float pitch = (float)(-Math.toDegrees(chosen));
		if(!Float.isFinite(pitch)) {
			pitch = (float)(-Math.toDegrees(direct));
		}
		return pitch;
	}

	private static Vec3 resolveAimBase(Entity target) {
		if(target instanceof LivingEntity living) {
			return new Vec3(living.getX(), living.getEyeY() - 0.1d, living.getZ());
		}
		return target.position();
	}

	public static AbstractDeadeyeInteraction getDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		AbstractDeadeyeInteraction integrationInteraction = IntegrationRegistry.resolveInteraction(state, player, itemStack);
		if (integrationInteraction != null) return integrationInteraction;

		Item item = itemStack.getItem();

		if(item instanceof BowItem || item instanceof CrossbowItem) return new BowDeadeyeInteraction(state, player, itemStack);
		else if(item instanceof ProjectileItem) {
			if(item instanceof ArrowItem || item instanceof FireworkRocketItem || item instanceof FireChargeItem) return null;
			return new ProjectileDeadeyeInteraction(state, player, itemStack);
		}
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
