package com.namefix.data;

import com.namefix.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DeadeyeTargetData {
	// Entity that has been targeted by the player
	public Entity target;
	// Position of the target
	private Vec3 initialPos;
	// Offset of the mark relative to the position
	private Vec3 markOffset;
	// Render ticks
	private int renderTicks = 0;

	public DeadeyeTargetData(Entity target, Vec3 initialPos) {
		this.target = target;
		this.initialPos = initialPos;
		this.markOffset = target.position().subtract(initialPos);
	}

	public boolean isInvalid() {
		return target == null || !target.isAlive() || target.isRemoved();
	}

	// Returns the current position of the mark
	public Vec3 getMarkPosition(float partialTick) {
		if(target == null || target.isRemoved()) return initialPos;
		return target.getPosition(partialTick).subtract(markOffset);
	}

	@Environment(EnvType.CLIENT)
	public Vec2 getMarkHeading() {
		return Utils.getHeadingFromTarget(target, EntityAnchorArgument.Anchor.EYES, getMarkPosition(0.0f));
	}

	public int getRenderTicks() { return renderTicks; }
	public int incrementRenderTicks() { return ++renderTicks; }
	public Vec3 getMarkOffset() {
		return markOffset;
	}
}
