package com.namefix.client;

import com.namefix.config.DeadeyeConfig;
import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.data.PlayerSavedData;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.network.payload.*;
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

public class DeadeyeClient {
	public static boolean DEADEYE_ENABLED = false;
	public static PlayerDeadeyeState DEADEYE_STATE = new PlayerDeadeyeState();
	public static PlayerSavedData DEADEYE_DATA = new PlayerSavedData();
	public static float PREVIOUS_TICK_RATE = -1.0f;

	// SHOOTING
	private static long LAST_DEADEYE_MARK = 0;
	private static long LAST_DEADEYE_LERP = 0;
	private static long DEADEYE_LERP_START = 0;
	private static long LAST_DEADEYE_SHOT = 0;
	private static AbstractDeadeyeInteraction CURRENT_PHASE_INTERACTION = null;

	public static void initialize() {
		DeadeyeBowVisuals.registerItemProperties();
	}

	public static void render() {
		shootingTick();
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

			NetworkManager.sendToServer(new InformShotPayload(target.getMarkPosition(mc.getTimer().getGameTimeDeltaPartialTick(false)).toVector3f()));
			boolean hasMoreTargets = DEADEYE_STATE.targets.size() > 1;
			CURRENT_PHASE_INTERACTION.postShot(hasMoreTargets);
			DEADEYE_STATE.targets.removeFirst();
			LAST_DEADEYE_LERP = System.currentTimeMillis();
			LAST_DEADEYE_SHOT = System.currentTimeMillis();
			DEADEYE_LERP_START = System.currentTimeMillis();
		}
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
	}

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
			requestDeadeye();
			return EventResult.interruptDefault();
		}

		if(KeybindRegistry.DEADEYE_MARK.matches(keyCode, scanCode) && action == 1 && DEADEYE_ENABLED && DEADEYE_STATE.phase != Phase.SHOOTING) {
			requestMark();
			return EventResult.interruptDefault();
		}

		if(KeybindRegistry.DEADEYE_SHOOT_TARGETS.matches(keyCode, scanCode) && action == 1 && DEADEYE_ENABLED && DEADEYE_STATE.phase == Phase.MARKED) {
			initShootingPhase();
			return EventResult.interruptDefault();
		}

		return EventResult.pass();
	}

	private static void setDeadeyeState(boolean enabled) {
		if(DEADEYE_ENABLED == enabled) return;
		DEADEYE_ENABLED = enabled;

		if(enabled) {
			// TODO: update shader logic with the profile system
			if(DeadeyeConfig.Client.enableShaders) ShaderManager.activateShader("rdr2_deadeye");
			if(DeadeyeConfig.Client.enableLightLeak) DeadeyeHud.playLightLeak();
		} else {
			ShaderManager.deactivateShader("rdr2_deadeye");
			DEADEYE_STATE.phase = Phase.IDLE;
			DEADEYE_STATE.targets.clear();
			DEADEYE_STATE.markItem = null;
			DeadeyeBowVisuals.reset(); // reset fake bow thingy
		}
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
		// TODO: Add deadeye mark sound

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
	}

}
