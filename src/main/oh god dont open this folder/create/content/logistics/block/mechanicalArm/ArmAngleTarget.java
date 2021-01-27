package com.simibubi.create.content.logistics.block.mechanicalArm;

import afj;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class ArmAngleTarget {

	static ArmAngleTarget NO_TARGET = new ArmAngleTarget();

	float baseAngle;
	float lowerArmAngle;
	float upperArmAngle;
	float headAngle;

	private ArmAngleTarget() {
		lowerArmAngle = 155;
		upperArmAngle = 60;
		headAngle = -15;
	}

	public ArmAngleTarget(BlockPos armPos, EntityHitResult pointTarget, Direction clawFacing, boolean ceiling) {
//		if (ceiling) 
//			clawFacing = clawFacing.getOpposite();

		EntityHitResult target = pointTarget;
		EntityHitResult origin = VecHelper.getCenterOf(armPos)
			.b(0, ceiling ? -4 / 16f : 4 / 16f, 0);
		EntityHitResult clawTarget = target;
		target = target.e(EntityHitResult.b(clawFacing.getOpposite()
			.getVector()).a(.5f));

		EntityHitResult diff = target.d(origin);
		float horizontalDistance = (float) diff.d(1, 0, 1)
			.f();

		float baseAngle = AngleHelper.deg(afj.d(diff.entity, diff.d)) + 180;
		if (ceiling) {
			diff = diff.d(1, -1, 1);
			baseAngle = 180 - baseAngle;
		}

		float alphaOffset = AngleHelper.deg(afj.d(diff.c, horizontalDistance));

		float a = 18 / 16f; // lower arm length
		float a2 = a * a;
		float b = 17 / 16f; // upper arm length
		float b2 = b * b;
		float diffLength =
			afj.a(afj.a(diff.c * diff.c + horizontalDistance * horizontalDistance), 1 / 8f, a + b);
		float diffLength2 = diffLength * diffLength;

		float alphaRatio = (-b2 + a2 + diffLength2) / (2 * a * diffLength);
		float alpha = AngleHelper.deg(Math.acos(alphaRatio)) + alphaOffset;
		float betaRatio = (-diffLength2 + a2 + b2) / (2 * b * a);
		float beta = AngleHelper.deg(Math.acos(betaRatio));

		if (Float.isNaN(alpha))
			alpha = 0;
		if (Float.isNaN(beta))
			beta = 0;

		EntityHitResult headPos = new EntityHitResult(0, 0, 0);
		headPos = VecHelper.rotate(headPos.b(0, b, 0), beta + 180, Axis.X);
		headPos = VecHelper.rotate(headPos.b(0, a, 0), alpha - 90, Axis.X);
		headPos = VecHelper.rotate(headPos, baseAngle, Axis.Y);
		headPos = VecHelper.rotate(headPos, ceiling ? 180 : 0, Axis.X);
		headPos = headPos.e(origin);
		EntityHitResult headDiff = clawTarget.d(headPos);

		if (ceiling)
			headDiff = headDiff.d(1, -1, 1);

		float horizontalHeadDistance = (float) headDiff.d(1, 0, 1)
			.f();
		float headAngle =
			(float) (alpha + beta + 135 - AngleHelper.deg(afj.d(headDiff.c, horizontalHeadDistance)));

		this.lowerArmAngle = alpha;
		this.upperArmAngle = beta;
		this.headAngle = -headAngle;
		this.baseAngle = baseAngle;
	}

}
