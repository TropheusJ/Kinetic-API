package com.simibubi.kinetic_api.foundation.collision;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import net.minecraft.util.hit.EntityHitResult;


public class ContinuousOBBCollider extends OBBCollider {

	public static ContinuousSeparationManifold separateBBs(EntityHitResult cA, EntityHitResult cB, EntityHitResult eA, EntityHitResult eB, Matrix3d m,
														   EntityHitResult motion) {
		ContinuousSeparationManifold mf = new ContinuousSeparationManifold();

		EntityHitResult diff = cB.d(cA);

		m.transpose();
		EntityHitResult diff2 = m.transform(diff);
		EntityHitResult motion2 = m.transform(motion);
		m.transpose();

		double a00 = abs(m.m00);
		double a01 = abs(m.m01);
		double a02 = abs(m.m02);
		double a10 = abs(m.m10);
		double a11 = abs(m.m11);
		double a12 = abs(m.m12);
		double a20 = abs(m.m20);
		double a21 = abs(m.m21);
		double a22 = abs(m.m22);

		EntityHitResult uB0 = new EntityHitResult(m.m00, m.m10, m.m20);
		EntityHitResult uB1 = new EntityHitResult(m.m01, m.m11, m.m21);
		EntityHitResult uB2 = new EntityHitResult(m.m02, m.m12, m.m22);

		checkCount = 0;
		mf.stepSeparationAxis = uB1;
		mf.stepSeparation = Double.MAX_VALUE;

		if (
		// Separate along A's local axes (global XYZ)
		!(separate(mf, uA0, diff.entity, eA.entity, a00 * eB.entity + a01 * eB.c + a02 * eB.d, motion.entity)
			|| separate(mf, uA1, diff.c, eA.c, a10 * eB.entity + a11 * eB.c + a12 * eB.d, motion.c)
			|| separate(mf, uA2, diff.d, eA.d, a20 * eB.entity + a21 * eB.c + a22 * eB.d, motion.d)

			// Separate along B's local axes
			|| separate(mf, uB0, diff2.entity, eA.entity * a00 + eA.c * a10 + eA.d * a20, eB.entity, motion2.entity)
			|| separate(mf, uB1, diff2.c, eA.entity * a01 + eA.c * a11 + eA.d * a21, eB.c, motion2.c)
			|| separate(mf, uB2, diff2.d, eA.entity * a02 + eA.c * a12 + eA.d * a22, eB.d, motion2.d)))
			return mf;

		return null;
	}

	static boolean separate(ContinuousSeparationManifold mf, EntityHitResult axis, double TL, double rA, double rB,
		double projectedMotion) {
		checkCount++;
		double distance = abs(TL);
		double diff = distance - (rA + rB);

		boolean discreteCollision = diff <= 0;
		if (!discreteCollision && signum(projectedMotion) == signum(TL))
			return true;

		double sTL = signum(TL);
		double seperation = sTL * abs(diff);

		double entryTime = 0;
		double exitTime = Double.MAX_VALUE;
		if (!discreteCollision) {
			mf.isDiscreteCollision = false;

			if (abs(seperation) > abs(projectedMotion))
				return true;

			entryTime = abs(seperation) / abs(projectedMotion);
			exitTime = (diff + abs(rA) + abs(rB)) / abs(projectedMotion);
			mf.latestCollisionEntryTime = Math.max(entryTime, mf.latestCollisionEntryTime);
			mf.earliestCollisionExitTime = Math.min(exitTime, mf.earliestCollisionExitTime);
		}

		EntityHitResult normalizedAxis = axis.d();

		boolean isBestSeperation = distance != 0 && -(diff) <= abs(mf.separation);
//		boolean isBestSeperation = discreteCollision && checkCount == 5; // Debug specific separations

		double dot = mf.stepSeparationAxis.b(axis);
		if (dot != 0 && discreteCollision) {
			EntityHitResult cross = axis.c(mf.stepSeparationAxis);
			double dotSeparation = signum(dot) * TL - (rA + rB);
			double stepSeparation = -dotSeparation;
			EntityHitResult stepSeparationVec = axis;

			if (!cross.equals(EntityHitResult.a)) {
				EntityHitResult sepVec = normalizedAxis.a(dotSeparation);
				EntityHitResult axisPlane = axis.c(cross);
				EntityHitResult stepPlane = mf.stepSeparationAxis.c(cross);
				stepSeparationVec =
					sepVec.d(axisPlane.a(sepVec.b(stepPlane) / axisPlane.b(stepPlane)));
				stepSeparation = stepSeparationVec.f();


				if (abs(mf.stepSeparation) > abs(stepSeparation) && stepSeparation != 0) {
//					CollisionDebugger.showDebugLine(Vector3d.ZERO, sepVec, 0x111155, "stepsep", -16);
					mf.stepSeparation = stepSeparation;
				}

			} else {
				if (abs(mf.stepSeparation) > stepSeparation) {
					mf.stepSeparation = stepSeparation;
//					CollisionDebugger.showDebugLine(Vector3d.ZERO, stepSeparationVec, 0xff9999, "axis", -16);
				}
			}

//			if (abs(mf.separation) < abs(stepSeparation) && stepSeparation != 0)
		}

		if (isBestSeperation) {

			mf.axis = normalizedAxis;
			mf.separation = seperation;

			// Visualize values
//			if (CollisionDebugger.AABB != null) {
//				Vector3d normalizedAxis = axis.normalize();
//				showDebugLine(Vector3d.ZERO, normalizedAxis.scale(projectedMotion), 0x111155, "motion", 5);
//				showDebugLine(Vector3d.ZERO, normalizedAxis.scale(TL), 0xbb00bb, "tl", 4);
//				showDebugLine(Vector3d.ZERO, normalizedAxis.scale(sTL * rA), 0xff4444, "ra", 3);
//				showDebugLine(normalizedAxis.scale(sTL * rA),
//					normalizedAxis.scale(sTL * rA - entryTime * projectedMotion), 0x44ff44, "entry", 0);
//				showDebugLine(normalizedAxis.scale(sTL * rA - entryTime * projectedMotion),
//					normalizedAxis.scale(sTL * rA - entryTime * projectedMotion + exitTime * projectedMotion), 0x44ffff,
//					"exit", -1);
//				showDebugLine(normalizedAxis.scale(sTL * (distance - rB)), normalizedAxis.scale(TL), 0x4444ff, "rb", 2);
//				showDebugLine(normalizedAxis.scale(sTL * (distance - rB)),
//					normalizedAxis.scale(sTL * (distance - rB) + value), 0xff9966, "separation", 1);
////				System.out.println("TL:" + TL + ", rA: " + rA + ", rB: " + rB);
//			}

		}

		return false;
	}

	public static class ContinuousSeparationManifold extends SeparationManifold {

		static final double UNDEFINED = -1;
		double latestCollisionEntryTime = UNDEFINED;
		double earliestCollisionExitTime = Double.MAX_VALUE;
		boolean isDiscreteCollision = true;

		EntityHitResult stepSeparationAxis;
		double stepSeparation;

		public double getTimeOfImpact() {
			if (latestCollisionEntryTime == UNDEFINED)
				return UNDEFINED;
			if (latestCollisionEntryTime > earliestCollisionExitTime)
				return UNDEFINED;
			return latestCollisionEntryTime;
		}

		public boolean isSurfaceCollision() {
			return true;
		}

		public EntityHitResult asSeparationVec(double obbStepHeight) {
			if (isDiscreteCollision) {
				if (stepSeparation <= obbStepHeight) 
					return createSeparationVec(stepSeparation, stepSeparationAxis);
				return super.asSeparationVec();
			}
			double t = getTimeOfImpact();
			if (t == UNDEFINED)
				return null;
			return EntityHitResult.a;
		}
		
		@Override
		public EntityHitResult asSeparationVec() {
			return asSeparationVec(0);
		}

	}

}
