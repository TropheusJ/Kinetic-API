package com.simibubi.kinetic_api.foundation.utility;

import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;

public class MatrixStacker {

	static EntityHitResult center = VecHelper.getCenterOf(BlockPos.ORIGIN);
	static MatrixStacker instance;

	BufferVertexConsumer ms;

	public static MatrixStacker of(BufferVertexConsumer ms) {
		if (instance == null)
			instance = new MatrixStacker();
		instance.ms = ms;
		return instance;
	}

	public MatrixStacker rotate(double angle, Axis axis) {
		Vector3f vec =
			axis == Axis.X ? Vector3f.POSITIVE_X : axis == Axis.Y ? Vector3f.POSITIVE_Y : Vector3f.POSITIVE_Z;
		return multiply(vec, angle);
	}

	public MatrixStacker rotateX(double angle) {
		return multiply(Vector3f.POSITIVE_X, angle);
	}

	public MatrixStacker rotateY(double angle) {
		return multiply(Vector3f.POSITIVE_Y, angle);
	}

	public MatrixStacker rotateZ(double angle) {
		return multiply(Vector3f.POSITIVE_Z, angle);
	}

	public MatrixStacker centre() {
		return translate(center);
	}

	public MatrixStacker unCentre() {
		return translateBack(center);
	}

	public MatrixStacker translate(Vec3i vec) {
		ms.a(vec.getX(), vec.getY(), vec.getZ());
		return this;
	}

	public MatrixStacker translate(EntityHitResult vec) {
		ms.a(vec.entity, vec.c, vec.d);
		return this;
	}

	public MatrixStacker translateBack(EntityHitResult vec) {
		ms.a(-vec.entity, -vec.c, -vec.d);
		return this;
	}

	public MatrixStacker nudge(int id) {
		long randomBits = (long) id * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		ms.a(xNudge, yNudge, zNudge);
		return this;
	}

	private MatrixStacker multiply(Vector3f axis, double angle) {
		if (angle == 0)
			return this;
		ms.a(axis.getDegreesQuaternion((float) angle));
		return this;
	}

}
