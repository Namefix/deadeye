package com.namefix.client;

import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.interactions.AbstractDeadeyeInteraction;
import com.namefix.network.payload.DeadeyeStatePayload;
import com.namefix.network.payload.RequestDeadeyePayload;
import com.namefix.network.payload.RequestMarkPayload;
import com.namefix.registry.KeybindRegistry;
import com.namefix.shader.ShaderManager;
import com.namefix.util.Utils;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.minecraft.client.Minecraft;
import com.namefix.data.PlayerDeadeyeState.Phase;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DeadeyeClient {
	public static boolean DEADEYE_ENABLED = false;
	public static PlayerDeadeyeState DEADEYE_STATE = new PlayerDeadeyeState();
	public static float PREVIOUS_TICK_RATE = -1.0f;

	public static long LAST_DEADEYE_MARK = 0;

	public static void initialize() {
		modifyBowAnimations();
	}

	public static void onQuit(LocalPlayer localPlayer) {
		DEADEYE_ENABLED = false;
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

		return EventResult.pass();
	}

	private static void setDeadeyeState(boolean enabled) {
		DEADEYE_ENABLED = enabled;

		if(enabled) {
			ShaderManager.activateShader("rdr2_deadeye");
		} else {
			ShaderManager.deactivateShader("rdr2_deadeye");
			DEADEYE_STATE.targets.clear();
		}
	}

	// Request Dead Eye toggle from the server
	public static void requestDeadeye() {
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
	}

	public static void handleDeadeyeMark(RequestMarkPayload payload, NetworkManager.PacketContext packetContext) {
		Minecraft mc = Minecraft.getInstance();
		AbstractDeadeyeInteraction interaction = Utils.getDeadeyeInteraction(DEADEYE_STATE, mc.player, mc.player.getMainHandItem());
		if(interaction == null) return;
		Entity target = mc.level.getEntity(payload.entityId());
		if(target == null) return;
		LAST_DEADEYE_MARK = System.currentTimeMillis();
		DEADEYE_STATE.targets.add(new DeadeyeTargetData(target, new Vec3(payload.markPos())));
		// TODO: Add deadeye mark sound

		interaction.postMark();
	}

	// Modify bow pulling animations
	public static void modifyBowAnimations() {
		ItemPropertiesRegistry.register(Items.BOW, ResourceLocation.parse("pull"), (itemStack, world, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			}
			if (entity.isUsingItem() && entity.getUseItem() == itemStack) {
				int useTicks = entity.getTicksUsingItem();

				return Math.min(useTicks / 20.0f, 1.0f);
			}
			return 0.0F;
		});

		ItemPropertiesRegistry.register(Items.CROSSBOW, ResourceLocation.parse("pull"), (itemStack, world, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			}
			if (entity.isUsingItem() && entity.getUseItem() == itemStack) {
				int useTicks = entity.getTicksUsingItem();

				return Math.min(useTicks / 20.0f, 1.0f);
			}
			return 0.0F;
		});
	}
}
