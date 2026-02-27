package com.namefix.interactions;

import com.namefix.data.DeadeyeTargetData;
import com.namefix.data.PlayerDeadeyeState;
import com.namefix.util.Utils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/*
	- Dead Eye Interactions -
	This class is made to simplify adding mod support and not altering the main code.
	There are "hooks" that provide a way to cancel marks/shots if the current held interaction is not ready for it.
	Example: An interaction for a weapon inside a gun mod, the ammo count in the current magazine should not pass the total mark count.

 */
public abstract class AbstractDeadeyeInteraction {
	protected final PlayerDeadeyeState state;
	protected final Player player;
	protected final ItemStack itemStack;
	public boolean clientSideShoot = false;
	public boolean isGun = false;

	public AbstractDeadeyeInteraction(PlayerDeadeyeState state, Player player, ItemStack itemStack) {
		this.state = state;
		this.player = player;
		this.itemStack = itemStack;
	}

	// Before mark request is processed. Cancellable.
	public abstract boolean preMark();
	// After the mark request is processed.
	public abstract void postMark();

	// Before the shooting request is processed. Cancellable.
	public abstract boolean preShot();
	// Process interaction shot
	public abstract void shoot();
	// After the shooting request, flag indicates whether more targets remain client-side.
	public abstract void postShot(boolean hasMoreTargets);

	protected void alignPlayerForShot(DeadeyeTargetData targetData, Entity target, double projectileSpeed, double projectileGravity) {
		Vec3 shooterPos = player.getEyePosition();
		Vec3 currentMark = targetData.getMarkPosition(0.0f);
		Vec3 predictedTarget = Utils.predictLeadPosition(target, shooterPos, projectileSpeed);
		Vec3 markOffset = targetData.getMarkOffset();
		Vec3 predictedMark = predictedTarget == null ? currentMark : predictedTarget.subtract(markOffset == null ? Vec3.ZERO : markOffset);
		Vec3 aim = blendAimPoint(shooterPos, currentMark, predictedMark);
		Vec2 heading = Utils.getHeadingFromTarget(player, EntityAnchorArgument.Anchor.EYES, aim);
		float pitch = Utils.solveBallisticPitch(shooterPos, aim, projectileSpeed, projectileGravity);
		if(Float.isNaN(pitch) || Float.isInfinite(pitch)) pitch = heading.x;
		player.setXRot(pitch);
		player.setYRot(heading.y);
		player.setYHeadRot(heading.y);
		player.setYBodyRot(heading.y);
	}

	// try to predict target position
	protected Vec3 blendAimPoint(Vec3 shooterPos, Vec3 currentMark, Vec3 predictedMark) {
		if(predictedMark == null) return currentMark;
		double distance = shooterPos.distanceTo(currentMark);
		double blendStart = 12.0d;
		double blendEnd = 48.0d;
		double blend = Mth.clamp((distance - blendStart) / (blendEnd - blendStart), 0.0d, 1.0d);
		if(blend <= 0.0d) return currentMark;
		if(blend >= 1.0d) return predictedMark;
		Vec3 delta = predictedMark.subtract(currentMark);
		return currentMark.add(delta.scale(blend));
	}
}
