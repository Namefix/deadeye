package com.namefix.client;

import com.namefix.config.DeadeyeConfig;
import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.data.PlayerSavedData;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.interactions.PointBlankDeadeyeInteraction;
import com.namefix.network.payload.*;
import com.namefix.platform.PointBlankIntegration;
import com.namefix.registry.KeybindRegistry;
import com.namefix.shader.ShaderManager;
import com.namefix.util.ClientUtils;
import com.namefix.util.Utils;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import com.namefix.data.PlayerDeadeyeState.Phase;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class DeadeyeClient {
	public static boolean DEADEYE_ENABLED = false;
	public static PlayerDeadeyeState DEADEYE_STATE = new PlayerDeadeyeState();
	public static PlayerSavedData DEADEYE_DATA = new PlayerSavedData();
	public static float PREVIOUS_TICK_RATE = -1.0f;
	public static float DEADEYE_ENDING = 0.0f;

	private static final String SHADER_NAME = "rdr2_deadeye";
	private static float SHADER_FADE_PROGRESS = 0.0f;
	private static float SHADER_ENDING_VISUAL = 0.0f;
	private static long LAST_SHADER_UPDATE_NS = System.nanoTime();

	// SHOOTING
	private static long LAST_DEADEYE_MARK = 0;
	private static long LAST_DEADEYE_LERP = 0;
	private static long DEADEYE_LERP_START = 0;
	private static long LAST_DEADEYE_SHOT = 0;
	private static AbstractDeadeyeInteraction CURRENT_PHASE_INTERACTION = null;
	private static long LAST_AUTO_MARK = 0;
	private static long GUN_ALIGNMENT_WAIT = 0;

	public static void initialize() {
		DeadeyeBowVisuals.registerItemProperties();
	}

	public static void render() {
		shootingTick();
		autoMark();
		updateShaderVisuals();
		DeadeyeSound.tick();
	}

	public static void shootingTick() {
		Minecraft mc = Minecraft.getInstance();
		if(		mc.isPaused() || !DEADEYE_ENABLED ||
				DEADEYE_STATE.targets.isEmpty() || DEADEYE_STATE.phase != Phase.SHOOTING ||
				mc.player == null || System.currentTimeMillis() - LAST_DEADEYE_LERP < 100
		) return;

		if(CURRENT_PHASE_INTERACTION == null) return;

		if(!mc.player.getMainHandItem().getItem().equals(DEADEYE_STATE.markItem.getItem())) {
			requestDeadeye();
			return;
		}

		DeadeyeTargetData target = ClientUtils.getNextValidTarget();
		if(target == null) {
			requestDeadeye();
			return;
		}

		float pPitch = mc.player.getXRot();
		float pYaw = mc.player.getYRot();

		float interpolationFactor = (mc.getTimer().getRealtimeDeltaTicks() / 2.0f);
		if(System.currentTimeMillis() - DEADEYE_LERP_START > 3_000) interpolationFactor *= 4;

		Vec2 targetHeading = Utils.getHeadingFromTarget(mc.player, EntityAnchorArgument.Anchor.EYES, target.getMarkPosition(mc.getTimer().getGameTimeDeltaPartialTick(false)));
		float targetPitch = targetHeading.x;
		float targetYaw = targetHeading.y;
		float shortestPitch = pPitch + Mth.wrapDegrees(targetPitch - pPitch);
		float shortestYaw = pYaw + Mth.wrapDegrees(targetYaw - pYaw);
		float finalPitch =  Mth.lerp(interpolationFactor, pPitch, shortestPitch);
		float finalYaw = Mth.lerp(interpolationFactor, pYaw, shortestYaw);

		if(System.currentTimeMillis() - DEADEYE_LERP_START > 10_000) {
			finalPitch = targetPitch;
			finalYaw = targetYaw;
		}

		mc.player.setXRot(finalPitch);
		mc.player.setYRot(finalYaw);

		float wrappedFinalPitch = Mth.wrapDegrees(finalPitch);
		float wrappedFinalYaw = Mth.wrapDegrees(finalYaw);
		float wrappedTargetPitch = Mth.wrapDegrees(targetPitch);
		float wrappedTargetYaw = Mth.wrapDegrees(targetYaw);

		if(Mth.abs(wrappedTargetPitch - wrappedFinalPitch) < 1f && Math.abs(wrappedTargetYaw - wrappedFinalYaw) < 1f) {
			if(System.currentTimeMillis() - LAST_DEADEYE_SHOT < 250) return;

			if(!CURRENT_PHASE_INTERACTION.preShot()) return;

			AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(DEADEYE_STATE, mc.player, mc.player.getMainHandItem());

			if(interaction.isGun) {
				if(GUN_ALIGNMENT_WAIT == -1) GUN_ALIGNMENT_WAIT = System.currentTimeMillis();
				if(System.currentTimeMillis() - GUN_ALIGNMENT_WAIT < 250) return;
			}

			if(interaction.clientSideShoot) interaction.shoot();
			NetworkManager.sendToServer(new InformShotPayload(target.getMarkPosition(mc.getTimer().getGameTimeDeltaPartialTick(false)).toVector3f()));
			boolean hasMoreTargets = DEADEYE_STATE.targets.size() > 1;
			CURRENT_PHASE_INTERACTION.postShot(hasMoreTargets);
			DEADEYE_STATE.targets.removeFirst();

			LAST_DEADEYE_LERP = System.currentTimeMillis();
			LAST_DEADEYE_SHOT = System.currentTimeMillis();
			DEADEYE_LERP_START = System.currentTimeMillis();
			GUN_ALIGNMENT_WAIT = -1;
		}
	}

	public static void autoMark() {
		Minecraft mc = Minecraft.getInstance();
		if(mc.isPaused() || !DEADEYE_ENABLED || mc.player == null || System.currentTimeMillis() - LAST_AUTO_MARK < 250 || DEADEYE_STATE.phase == Phase.SHOOTING || DEADEYE_DATA.deadeyeSkill != 1) return;
		LAST_AUTO_MARK = System.currentTimeMillis();
		requestMark();
	}

	public static void initShootingPhase() {
		if(DEADEYE_STATE.targets.isEmpty()) return;
		CURRENT_PHASE_INTERACTION = Utils.getDeadeyeInteraction(DEADEYE_STATE, Minecraft.getInstance().player, DEADEYE_STATE.markItem);
		LAST_DEADEYE_LERP = System.currentTimeMillis();
		DEADEYE_LERP_START = System.currentTimeMillis();

		DEADEYE_STATE.phase = Phase.SHOOTING;
		NetworkManager.sendToServer(new InformShootingPhasePayload());
	}

	public static void onQuit(LocalPlayer localPlayer) {
		DEADEYE_ENABLED = false;
		DEADEYE_STATE = new PlayerDeadeyeState();
		DeadeyeBowVisuals.reset(); // reset fake bow animation thingy
		hardStopShader();
	}

	public static EventResult onKeyPressed(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if(!canHandleInput(minecraft)) {
			return EventResult.pass();
		}

		return handleKeybindActivation(
			KeybindRegistry.DEADEYE_TOGGLE.matches(keyCode, scanCode),
			KeybindRegistry.DEADEYE_MARK.matches(keyCode, scanCode),
			KeybindRegistry.DEADEYE_SHOOT_TARGETS.matches(keyCode, scanCode),
			KeybindRegistry.DEADEYE_INFO_SHOW.matches(keyCode, scanCode),
			action
		);
	}

	public static EventResult onMouseClicked(Minecraft minecraft, int button, int action, int modifiers) {
		if(!canHandleInput(minecraft)) {
			return EventResult.pass();
		}

		return handleKeybindActivation(
			KeybindRegistry.DEADEYE_TOGGLE.matchesMouse(button),
			KeybindRegistry.DEADEYE_MARK.matchesMouse(button),
			KeybindRegistry.DEADEYE_SHOOT_TARGETS.matchesMouse(button),
			KeybindRegistry.DEADEYE_INFO_SHOW.matchesMouse(button),
			action
		);
	}

	private static boolean canHandleInput(Minecraft minecraft) {
		return minecraft.level != null &&
			minecraft.player != null &&
			!minecraft.player.isSpectator() &&
			minecraft.player.isAlive() &&
			minecraft.screen == null;
	}

	private static EventResult handleKeybindActivation(boolean toggleMatch, boolean markMatch, boolean shootMatch, boolean info, int action) {
		if(action != GLFW.GLFW_PRESS) {
			return EventResult.pass();
		}

		if(toggleMatch) {
			requestDeadeye();
			return EventResult.interruptDefault();
		}

		if(markMatch && DEADEYE_ENABLED && DEADEYE_STATE.phase != Phase.SHOOTING && DEADEYE_DATA.deadeyeSkill != 1) {
			requestMark();
			return EventResult.interruptDefault();
		}

		if(shootMatch && DEADEYE_ENABLED && DEADEYE_STATE.phase == Phase.MARKED) {
			initShootingPhase();
			return EventResult.interruptDefault();
		}

		if(DeadeyeConfig.HUD.enableInfoToast && info && !DeadeyeHud.isDeadeyeInfoVisible()) {
			DeadeyeHud.showDeadeyeInfo();
			DeadeyeSound.playUIAppear();
			return EventResult.interruptDefault();
		}

		return EventResult.pass();
	}

	private static void setDeadeyeState(boolean enabled) {
		if(DEADEYE_ENABLED == enabled) return;
		DEADEYE_ENABLED = enabled;

		Player player = Minecraft.getInstance().player;

		if(enabled) {
			SHADER_FADE_PROGRESS = 0.0f;
			if(DeadeyeConfig.Client.enableLightLeak) DeadeyeHud.playLightLeak();
			DeadeyeSound.playEnterSound();
			DeadeyeSound.startBackgroundSounds();
			calculateDeadeyeEnding();

			AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(DEADEYE_STATE, player, player.getMainHandItem());
			if(interaction != null && interaction.isGun) {
				if(interaction instanceof PointBlankDeadeyeInteraction) PointBlankIntegration.refillAmmo(player, player.getMainHandItem());
			}
		} else {
			DEADEYE_STATE.phase = Phase.IDLE;
			DEADEYE_STATE.targets.clear();
			DEADEYE_STATE.markItem = null;
			DeadeyeBowVisuals.reset(); // reset fake bow thingy
			DeadeyeSound.playExitSound();
			DeadeyeSound.stopBackgroundSounds();

			DEADEYE_ENDING = 0.0f;
		}
	}

	private static void calculateDeadeyeEnding() {
		DEADEYE_ENDING = Mth.clamp(1f - ((DEADEYE_DATA.deadeyeCore/20f)+(DEADEYE_DATA.deadeyeMeter/20f)), 0.0f, 1.0f);
	}

	// Request Dead Eye toggle from the server
	public static void requestDeadeye() {
		if(DEADEYE_DATA.deadeyeSkill <= 0 || DEADEYE_DATA.deadeyeMeter + DEADEYE_DATA.deadeyeCore <= 0f) return;
		NetworkManager.sendToServer(new RequestDeadeyePayload());
	}

	public static void requestMark() {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player == null) return;
		ItemStack markingItem = player.getMainHandItem();
		if(System.currentTimeMillis() - LAST_DEADEYE_MARK < 250) return;

		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(DEADEYE_STATE, player, markingItem);
		if(interaction == null) return;
		if(DEADEYE_STATE.targets.size() > 50) return;
		if(!interaction.preMark()) return;

		HitResult hit = Utils.raycastFromPlayer(player, 4000.0);
		if(hit == null || hit.getType() != HitResult.Type.ENTITY) return;
		EntityHitResult entityHit = (EntityHitResult) hit;

		LivingEntity target = (LivingEntity) entityHit.getEntity();
		NetworkManager.sendToServer(new RequestMarkPayload(hit.getLocation().toVector3f(), target.getId()));
	}

	// Handle server Dead Eye phase
	public static void handleDeadeyeState(DeadeyeStatePayload payload, NetworkManager.PacketContext packetContext) {
		setDeadeyeState(payload.state());
		PREVIOUS_TICK_RATE = payload.previousTickrate();
		DEADEYE_STATE.phase = Phase.values()[payload.phase()];

		if(DEADEYE_STATE.phase == Phase.SHOOTING) initShootingPhase();
	}

	public static void handleDeadeyeMark(RequestMarkPayload payload, NetworkManager.PacketContext packetContext) {
		Minecraft mc = Minecraft.getInstance();
		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(DEADEYE_STATE, mc.player, mc.player.getMainHandItem());
		if(interaction == null) return;
		Entity target = mc.level.getEntity(payload.entityId());
		if(target == null) return;
		LAST_DEADEYE_MARK = System.currentTimeMillis();
		DEADEYE_STATE.targets.add(new DeadeyeTargetData(target, new Vec3(payload.markPos())));
		DEADEYE_STATE.markItem = mc.player.getMainHandItem();
		DeadeyeSound.playMarkSound();

		interaction.postMark();
	}

	public static void handleLevelData(LevelDataPayload payload, NetworkManager.PacketContext packetContext) {
		DEADEYE_DATA.deadeyeSkill = payload.deadeyeSkill();
		DEADEYE_DATA.deadeyeLevel = payload.deadeyeLevel();
		DEADEYE_DATA.deadeyeXp = payload.deadeyeXp();
	}

	public static void handleMeterData(MeterDataPayload payload, NetworkManager.PacketContext packetContext) {
		DEADEYE_DATA.deadeyeMeter = payload.deadeyeMeter();
		DEADEYE_DATA.deadeyeCore = payload.deadeyeCore();
		calculateDeadeyeEnding();
	}

	private static void updateShaderVisuals() {
		boolean shadersAllowed = DeadeyeConfig.Client.enableShaders;
		float desiredFade = (shadersAllowed && DEADEYE_ENABLED) ? 1.0f : 0.0f;
		float deltaSeconds = computeShaderDelta();
		float fadeSpeed = desiredFade > SHADER_FADE_PROGRESS ? 9.75f : 6.6f;
		SHADER_FADE_PROGRESS = approach(SHADER_FADE_PROGRESS, desiredFade, deltaSeconds * fadeSpeed);
		SHADER_ENDING_VISUAL = approach(SHADER_ENDING_VISUAL, DEADEYE_ENDING, deltaSeconds * 1.5f);

		boolean shouldKeepAlive = shadersAllowed && (desiredFade > 0.0f || SHADER_FADE_PROGRESS > 0.002f);
		if(shouldKeepAlive) {
			if(!ShaderManager.isShaderActive(SHADER_NAME)) {
				ShaderManager.activateShader(SHADER_NAME);
			}
			ShaderManager.setUniform(SHADER_NAME, "Fade", SHADER_FADE_PROGRESS);
			ShaderManager.setUniform(SHADER_NAME, "Ending", SHADER_ENDING_VISUAL);
		} else if(ShaderManager.isShaderActive(SHADER_NAME)) {
			ShaderManager.deactivateShader(SHADER_NAME);
		}
	}

	private static float computeShaderDelta() {
		long now = System.nanoTime();
		float seconds = (now - LAST_SHADER_UPDATE_NS) / 1_000_000_000f;
		LAST_SHADER_UPDATE_NS = now;
		return Mth.clamp(seconds, 0.0f, 0.1f);
	}

	private static float approach(float current, float target, float delta) {
		if(current < target) {
			return Math.min(target, current + delta);
		}
		return Math.max(target, current - delta);
	}

	private static void hardStopShader() {
		SHADER_FADE_PROGRESS = 0.0f;
		SHADER_ENDING_VISUAL = 0.0f;
		LAST_SHADER_UPDATE_NS = System.nanoTime();
		if(ShaderManager.isShaderActive(SHADER_NAME)) {
			ShaderManager.deactivateShader(SHADER_NAME);
		}
	}

}
