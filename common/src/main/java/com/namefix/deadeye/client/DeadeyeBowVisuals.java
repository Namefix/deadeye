package com.namefix.deadeye.client;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DeadeyeBowVisuals {
	private static final VisualDrawState VISUAL_DRAW_STATE = new VisualDrawState();

	private DeadeyeBowVisuals() {}

	public static void registerItemProperties() {
		ItemPropertiesRegistry.register(Items.BOW, ResourceLocation.parse("pull"), (itemStack, world, entity, seed) -> resolvePullProperty(itemStack, entity, false));
		ItemPropertiesRegistry.register(Items.BOW, ResourceLocation.parse("pulling"), (itemStack, world, entity, seed) -> resolvePullingProperty(itemStack, entity, false));
		ItemPropertiesRegistry.register(Items.CROSSBOW, ResourceLocation.parse("pull"), (itemStack, world, entity, seed) -> resolvePullProperty(itemStack, entity, true));
		ItemPropertiesRegistry.register(Items.CROSSBOW, ResourceLocation.parse("pulling"), (itemStack, world, entity, seed) -> resolvePullingProperty(itemStack, entity, true));
		ItemPropertiesRegistry.register(Items.CROSSBOW, ResourceLocation.parse("charged"), (itemStack, world, entity, seed) -> resolveChargedProperty(itemStack, entity));
	}

	public static void reset() {
		VISUAL_DRAW_STATE.reset();
	}

	public static void startVisualBowDrawAnimation(boolean crossbow, boolean fakeCharge, int durationMs) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null) return;
		VISUAL_DRAW_STATE.start(player, crossbow, fakeCharge, durationMs);
	}

	public static boolean shouldForceVisualItemUse(Player player) {
		if(player == null) return false;
		return VISUAL_DRAW_STATE.shouldForceItemUse(player);
	}

	public static ItemStack getForcedUseItem(Player player) {
		if(player == null) return ItemStack.EMPTY;
		return VISUAL_DRAW_STATE.getForcedItem(player);
	}

	public static int getForcedUseRemainingTicks(Player player) {
		if(player == null) return -1;
		return VISUAL_DRAW_STATE.getForcedRemainingTicks(player);
	}

	public static InteractionHand getForcedUseHand(Player player) {
		if(player == null) return null;
		return VISUAL_DRAW_STATE.getForcedHand(player);
	}

	public static boolean isVisualDrawAnimationEnded() {
		return !VISUAL_DRAW_STATE.active || VISUAL_DRAW_STATE.isFinished();
	}

	public static void endVisualBowDrawAnimation() {
		VISUAL_DRAW_STATE.reset();
	}

	private static float resolvePullProperty(ItemStack stack, LivingEntity entity, boolean crossbow) {
		if(entity == null) return 0.0f;
		float visual = getVisualPullOverride(stack, entity, crossbow);
		if(visual >= 0.0f) return visual;
		if(entity.isUsingItem() && entity.getUseItem() == stack) {
			int useTicks = entity.getTicksUsingItem();
			return Math.min(useTicks / 20.0f, 1.0f);
		}
		return 0.0f;
	}

	private static float resolveChargedProperty(ItemStack stack, LivingEntity entity) {
		if(CrossbowItem.isCharged(stack)) return 1.0f;
		if(entity instanceof Player player && VISUAL_DRAW_STATE.shouldFakeCharged(player, stack)) return 1.0f;
		return 0.0f;
	}

	private static float resolvePullingProperty(ItemStack stack, LivingEntity entity, boolean crossbow) {
		if(entity == null) return 0.0f;
		if(entity.isUsingItem() && entity.getUseItem() == stack) return 1.0f;
		if(entity instanceof Player player && VISUAL_DRAW_STATE.isActiveFor(player, stack, crossbow)) return 1.0f;
		return 0.0f;
	}

	private static float getVisualPullOverride(ItemStack stack, LivingEntity entity, boolean crossbow) {
		if(!(entity instanceof Player player)) return -1.0f;
		return VISUAL_DRAW_STATE.getProgress(player, stack, crossbow);
	}

	private static class VisualDrawState {
		private boolean active;
		private boolean finished;
		private boolean crossbow;
		private boolean fakeCharge;
		private InteractionHand hand;
		private Item animationItem;
		private long startMs;
		private int durationMs;

		void start(Player player, boolean crossbow, boolean fakeCharge, int durationMs) {
			this.active = true;
			this.finished = false;
			this.crossbow = crossbow;
			this.fakeCharge = fakeCharge;
			this.durationMs = Math.max(durationMs, 50);
			this.startMs = Util.getMillis();
			this.hand = InteractionHand.MAIN_HAND;
			this.animationItem = player.getMainHandItem().getItem();
		}

		float getProgress(Player player, ItemStack stack, boolean requestCrossbow) {
			if(!active) return -1.0f;
			if(player != Minecraft.getInstance().player) return -1.0f;
			if(player.getMainHandItem() != stack) return -1.0f;
			if(this.crossbow != requestCrossbow) return -1.0f;

			long elapsed = Util.getMillis() - startMs;
			if(elapsed >= durationMs) {
				finished = true;
				return 1.0f;
			}
			float progress = (float) elapsed / (float) durationMs;
			return Mth.clamp(progress, 0.0f, 1.0f);
		}

		boolean shouldForceItemUse(Player player) {
			if(!active) return false;
			if(player != Minecraft.getInstance().player) return false;
			if(hand == null || animationItem == null) return false;
			ItemStack held = player.getItemInHand(hand);
			if(held.isEmpty()) return false;
			if(held.getItem() != animationItem) return false;
			return true;
		}

		ItemStack getForcedItem(Player player) {
			if(!shouldForceItemUse(player)) return ItemStack.EMPTY;
			return player.getItemInHand(hand);
		}

		int getForcedRemainingTicks(Player player) {
			if(!shouldForceItemUse(player)) return -1;
			ItemStack held = player.getItemInHand(hand);
			int duration = held.getUseDuration(player);
			int usedTicks = getForcedUsedTicks();
			return Math.max(duration - usedTicks, 0);
		}

		InteractionHand getForcedHand(Player player) {
			if(!shouldForceItemUse(player)) return null;
			return hand;
		}

		private int getForcedUsedTicks() {
			if(durationMs <= 0) return 0;
			long elapsed = Util.getMillis() - startMs;
			if(elapsed <= 0) return 0;
			float progress = Math.min((float) elapsed / (float) durationMs, 1.0f);
			return Mth.clamp(Math.round(progress * 20.0f), 0, 20);
		}

		boolean shouldFakeCharged(Player player, ItemStack stack) {
			if(!active || !fakeCharge || !crossbow) return false;
			if(player != Minecraft.getInstance().player) return false;
			return player.getMainHandItem() == stack;
		}

		boolean isActiveFor(Player player, ItemStack stack, boolean requestCrossbow) {
			if(!active) return false;
			if(player != Minecraft.getInstance().player) return false;
			if(player.getMainHandItem() != stack) return false;
			return this.crossbow == requestCrossbow;
		}

		boolean isFinished() {
			if(!active) return false;
			if(finished) return true;
			if(Util.getMillis() - startMs >= durationMs) {
				finished = true;
				return true;
			}
			return false;
		}

		void reset() {
			active = false;
			finished = false;
			crossbow = false;
			fakeCharge = false;
			hand = null;
			animationItem = null;
			startMs = 0L;
			durationMs = 0;
		}
	}
}
