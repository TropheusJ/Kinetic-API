package com.simibubi.create.foundation.utility;

import afj;
import java.util.Random;

import javax.annotation.Nullable;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;

public class VecHelper {

	public static final EntityHitResult CENTER_OF_ORIGIN = new EntityHitResult(.5, .5, .5);

	public static EntityHitResult rotate(EntityHitResult vec, EntityHitResult rotationVec) {
		return rotate(vec, rotationVec.entity, rotationVec.c, rotationVec.d);
	}

	public static EntityHitResult rotate(EntityHitResult vec, double xRot, double yRot, double zRot) {
		return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
	}

	public static EntityHitResult rotateCentered(EntityHitResult vec, double deg, Axis axis) {
		EntityHitResult shift = getCenterOf(BlockPos.ORIGIN);
		return VecHelper.rotate(vec.d(shift), deg, axis)
			.e(shift);
	}

	public static EntityHitResult rotate(EntityHitResult vec, double deg, Axis axis) {
		if (deg == 0)
			return vec;
		if (vec == EntityHitResult.a)
			return vec;

		float angle = (float) (deg / 180f * Math.PI);
		double sin = afj.a(angle);
		double cos = afj.b(angle);
		double x = vec.entity;
		double y = vec.c;
		double z = vec.d;

		if (axis == Axis.X)
			return new EntityHitResult(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new EntityHitResult(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new EntityHitResult(x * cos - y * sin, y * cos + x * sin, z);
		return vec;
	}

	public static boolean isVecPointingTowards(EntityHitResult vec, Direction direction) {
		return EntityHitResult.b(direction.getVector()).f(vec.d()) < .75;
	}

	public static EntityHitResult getCenterOf(Vec3i pos) {
		if (pos.equals(Vec3i.ZERO))
			return CENTER_OF_ORIGIN;
		return EntityHitResult.b(pos).b(.5f, .5f, .5f);
	}

	public static EntityHitResult offsetRandomly(EntityHitResult vec, Random r, float radius) {
		return new EntityHitResult(vec.entity + (r.nextFloat() - .5f) * 2 * radius, vec.c + (r.nextFloat() - .5f) * 2 * radius,
			vec.d + (r.nextFloat() - .5f) * 2 * radius);
	}

	public static EntityHitResult axisAlingedPlaneOf(EntityHitResult vec) {
		vec = vec.d();
		return new EntityHitResult(1, 1, 1).a(Math.abs(vec.entity), Math.abs(vec.c), Math.abs(vec.d));
	}
	
	public static EntityHitResult axisAlingedPlaneOf(Direction face) {
		return axisAlingedPlaneOf(EntityHitResult.b(face.getVector()));
	}

	public static ListTag writeNBT(EntityHitResult vec) {
		ListTag listnbt = new ListTag();
		listnbt.add(DoubleTag.of(vec.entity));
		listnbt.add(DoubleTag.of(vec.c));
		listnbt.add(DoubleTag.of(vec.d));
		return listnbt;
	}

	public static EntityHitResult readNBT(ListTag list) {
		if (list.isEmpty())
			return EntityHitResult.a;
		return new EntityHitResult(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static EntityHitResult voxelSpace(double x, double y, double z) {
		return new EntityHitResult(x, y, z).a(1 / 16f);
	}

	public static int getCoordinate(Vec3i pos, Axis axis) {
		return axis.choose(pos.getX(), pos.getY(), pos.getZ());
	}

	public static float getCoordinate(EntityHitResult vec, Axis axis) {
		return (float) axis.choose(vec.entity, vec.c, vec.d);
	}

	public static boolean onSameAxis(BlockPos pos1, BlockPos pos2, Axis axis) {
		if (pos1.equals(pos2))
			return true;
		for (Axis otherAxis : Axis.values())
			if (axis != otherAxis)
				if (getCoordinate(pos1, otherAxis) != getCoordinate(pos2, otherAxis))
					return false;
		return true;
	}

	public static EntityHitResult clamp(EntityHitResult vec, float maxLength) {
		return vec.f() > maxLength ? vec.d()
			.a(maxLength) : vec;
	}

	public static EntityHitResult clampComponentWise(EntityHitResult vec, float maxLength) {
		return new EntityHitResult(afj.a(vec.entity, -maxLength, maxLength), afj.a(vec.c, -maxLength, maxLength),
			afj.a(vec.d, -maxLength, maxLength));
	}

	public static EntityHitResult project(EntityHitResult vec, EntityHitResult ontoVec) {
		if (ontoVec.equals(EntityHitResult.a))
			return EntityHitResult.a;
		return ontoVec.a(vec.b(ontoVec) / ontoVec.g());
	}

	@Nullable
	public static EntityHitResult intersectSphere(EntityHitResult origin, EntityHitResult lineDirection, EntityHitResult sphereCenter, double radius) {
		if (lineDirection.equals(EntityHitResult.a))
			return null;
		if (lineDirection.f() != 1)
			lineDirection = lineDirection.d();

		EntityHitResult diff = origin.d(sphereCenter);
		double lineDotDiff = lineDirection.b(diff);
		double delta = lineDotDiff * lineDotDiff - (diff.g() - radius * radius);
		if (delta < 0)
			return null;
		double t = -lineDotDiff + afj.a(delta);
		return origin.e(lineDirection.a(t));
	}

}
