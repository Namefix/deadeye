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

	public DeadeyeTargetData(Entity target, Vec3 initialPos) {
		this.target = target;
		this.initialPos = initialPos;
		this.markOffset = getMarkOffset();
	}

	// Returns the current position of the mark
	public Vec3 getMarkOffset() {
		return target.position().subtract(markOffset);
	}

	@Environment(EnvType.CLIENT)
	public Vec2 getMarkHeading() {
		return Utils.getHeadingFromTarget(target, EntityAnchorArgument.Anchor.EYES, getMarkOffset());
	}
}
