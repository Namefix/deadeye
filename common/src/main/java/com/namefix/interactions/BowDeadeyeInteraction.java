package com.namefix.interactions;

import com.namefix.client.DeadeyeBowVisuals;
import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.server.DeadeyeServer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class BowDeadeyeInteraction extends AbstractDeadeyeInteraction {
	private static final int BOW_DRAW_TICKS = 20;
	private static final int BOW_ANIMATION_MS = 320;
	private static final int CROSSBOW_ANIMATION_MS = 360;

	private final boolean isCrossbow;
	private boolean clientAnimationTriggered;
	private boolean clientAnimationEnded;
	private boolean clientCrossbowWasCharged;

	public BowDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		super(state, player, itemStack);
		this.isCrossbow = itemStack.getItem() instanceof CrossbowItem;
	}

	@Override
	public boolean preMark() {
		if(player.isCreative()) return true;
		return getAvailableProjectileCount() > state.targets.size();
	}

	@Override
	public void postMark() {
		if(player.level().isClientSide) return;
		if(player.isCreative()) return;
		if(getAvailableProjectileCount() <= state.targets.size()) {
			state.markItem = player.getMainHandItem().copy();
			DeadeyeServer.updatePlayerPhase((ServerPlayer) player, PlayerDeadeyeState.Phase.SHOOTING);
		}
	}

	@Override
	public boolean preShot() {
		if(!player.level().isClientSide) return true;
		ItemStack item = player.getMainHandItem();
		if(item.isEmpty() || item.getItem() != itemStack.getItem()) return false;

		if(!clientAnimationTriggered) {
			clientAnimationTriggered = true;
			clientAnimationEnded = false;
			if(isCrossbow) clientCrossbowWasCharged = CrossbowItem.isCharged(item);
			int duration = isCrossbow ? CROSSBOW_ANIMATION_MS : BOW_ANIMATION_MS;
			DeadeyeBowVisuals.startVisualBowDrawAnimation(isCrossbow, isCrossbow && !clientCrossbowWasCharged, duration);
			return false;
		}

		if(!clientAnimationEnded && !DeadeyeBowVisuals.isVisualDrawAnimationEnded()) return false;
		clientAnimationEnded = true;
		return true;
	}

	@Override
	public void shoot() {
		if(player.level().isClientSide) return;
		if(state.targets.isEmpty()) return;

		DeadeyeTargetData targetData = state.targets.getFirst();
		if(targetData == null) return;
		Entity target = targetData.target;
		if(target == null || target.isRemoved()) return;

		ItemStack held = player.getMainHandItem();
		if(held.isEmpty() || held.getItem() != itemStack.getItem()) return;

		if(held.getItem() instanceof BowItem bowItem) {
			handleBowShot(bowItem, held, targetData, target);
		} else if(held.getItem() instanceof CrossbowItem crossbowItem) {
			handleCrossbowShot(crossbowItem, held, targetData, target);
		}
	}

	@Override
	public void postShot(boolean hasMoreTargets) {
		if(!player.level().isClientSide) return;
		ItemStack held = player.getMainHandItem();
		boolean canAnimate = !held.isEmpty() && held.getItem() == itemStack.getItem();
		DeadeyeBowVisuals.endVisualBowDrawAnimation();
		clientAnimationEnded = false;
		if(hasMoreTargets && canAnimate) {
			clientAnimationTriggered = true;
			clientCrossbowWasCharged = isCrossbow && CrossbowItem.isCharged(held);
			int duration = isCrossbow ? CROSSBOW_ANIMATION_MS : BOW_ANIMATION_MS;
			DeadeyeBowVisuals.startVisualBowDrawAnimation(isCrossbow, isCrossbow && !clientCrossbowWasCharged, duration);
		} else {
			clientAnimationTriggered = false;
			clientCrossbowWasCharged = false;
		}
	}

	private int getAvailableProjectileCount() {
		if(player.isCreative()) return Integer.MAX_VALUE;
		if(!(itemStack.getItem() instanceof ProjectileWeaponItem weaponItem)) {
			ItemStack projectile = player.getProjectile(itemStack);
			return projectile.isEmpty() ? 0 : projectile.getCount();
		}

		Predicate<ItemStack> projectilePredicate = weaponItem.getAllSupportedProjectiles();
		int total = 0;
		for(ItemStack inventoryStack : player.getInventory().items) {
			if(projectilePredicate.test(inventoryStack)) total += inventoryStack.getCount();
		}
		for(ItemStack inventoryStack : player.getInventory().offhand) {
			if(projectilePredicate.test(inventoryStack)) total += inventoryStack.getCount();
		}
		return total;
	}

	private void handleBowShot(BowItem bowItem, ItemStack bowStack, DeadeyeTargetData targetData, Entity target) {
		int drawTicks = BOW_DRAW_TICKS;
		float power = BowItem.getPowerForTime(drawTicks);
		double projectileSpeed = power * 3.0d;
		alignPlayerForShot(targetData, target, projectileSpeed, 0.05d);
		int useDuration = bowItem.getUseDuration(bowStack, player);
		int timeLeft = Mth.clamp(useDuration - drawTicks, 0, useDuration);
		bowItem.releaseUsing(bowStack, player.level(), player, timeLeft);
	}

	private void handleCrossbowShot(CrossbowItem crossbowItem, ItemStack stack, DeadeyeTargetData targetData, Entity target) {
		if(!CrossbowItem.isCharged(stack)) {
			int chargeDuration = CrossbowItem.getChargeDuration(stack, player);
			int timeLeft = Mth.clamp(crossbowItem.getUseDuration(stack, player) - chargeDuration, 0, crossbowItem.getUseDuration(stack, player));
			crossbowItem.releaseUsing(stack, player.level(), player, timeLeft);
		}

		if(!CrossbowItem.isCharged(stack)) return;

		ChargedProjectiles charged = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
		if(charged.isEmpty()) return;

		boolean hasFirework = charged.contains(Items.FIREWORK_ROCKET);
		double projectileSpeed = hasFirework ? 1.6d : 3.15d;
		double projectileGravity = hasFirework ? 0.0d : 0.05d;
		alignPlayerForShot(targetData, target, projectileSpeed, projectileGravity);
		crossbowItem.performShooting(player.level(), player, InteractionHand.MAIN_HAND, stack, (float) projectileSpeed, 1.0f, null);
	}

}
