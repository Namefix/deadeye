package com.namefix.util;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

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
}
