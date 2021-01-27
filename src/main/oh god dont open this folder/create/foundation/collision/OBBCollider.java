package com.simibubi.create.foundation.collision;

import static com.simibubi.create.foundation.collision.CollisionDebugger.showDebugLine;
import static java.lang.Math.abs;
import static java.lang.Math.signum;

import net.minecraft.util.hit.EntityHitResult;



public class OBBCollider {

	static final EntityHitResult uA0 = new EntityHitResult(1, 0, 0);
	static final EntityHitResult uA1 = new EntityHitResult(0, 1, 0);
	static final EntityHitResult uA2 = new EntityHitResult(0, 0, 1);

	public static EntityHitResult separateBBs(EntityHitResult cA, EntityHitResult cB, EntityHitResult eA, EntityHitResult eB, Matrix3d m) {
		SeparationManifold mf = new SeparationManifold();

		EntityHitResult t = cB.d(cA);

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

		if (
		// Separate along A's local axes (global XYZ)
		!(isSeparatedAlong(mf, uA0, t.entity, eA.entity, a00 * eB.entity + a01 * eB.c + a02 * eB.d)
			|| isSeparatedAlong(mf, uA1, t.c, eA.c, a10 * eB.entity + a11 * eB.c + a12 * eB.d)
			|| isSeparatedAlong(mf, uA2, t.d, eA.d, a20 * eB.entity + a21 * eB.c + a22 * eB.d)

			// Separate along B's local axes
			|| isSeparatedAlong(mf, uB0, (t.entity * m.m00 + t.c * m.m10 + t.d * m.m20),
				eA.entity * a00 + eA.c * a10 + eA.d * a20, eB.entity)
			|| isSeparatedAlong(mf, uB1, (t.entity * m.m01 + t.c * m.m11 + t.d * m.m21),
				eA.entity * a01 + eA.c * a11 + eA.d * a21, eB.c)
			|| isSeparatedAlong(mf, uB2, (t.entity * m.m02 + t.c * m.m12 + t.d * m.m22),
				eA.entity * a02 + eA.c * a12 + eA.d * a22, eB.d)))
			return mf.asSeparationVec();

		return null;
	}

	static int checkCount = 0;

	static boolean isSeparatedAlong(SeparationManifold mf, EntityHitResult axis, double TL, double rA, double rB) {
		checkCount++;
		double distance = abs(TL);
		double diff = distance - (rA + rB);
		if (diff > 0)
			return true;

//		boolean isBestSeperation = distance != 0 && -(diff) <= abs(bestSeparation.getValue());
		boolean isBestSeperation = checkCount == 2; // Debug specific separations

		if (isBestSeperation) {
			double sTL = signum(TL);
			double value = sTL * abs(diff);
			mf.axis = axis.d();
			mf.separation = value;

			// Visualize values
			if (CollisionDebugger.AABB != null) {
				EntityHitResult normalizedAxis = axis.d();
				showDebugLine(EntityHitResult.a, normalizedAxis.a(TL), 0xbb00bb, "tl", 4);
				showDebugLine(EntityHitResult.a, normalizedAxis.a(sTL * rA), 0xff4444, "ra", 3);
				showDebugLine(normalizedAxis.a(sTL * (distance - rB)), normalizedAxis.a(TL), 0x4444ff, "rb", 2);
				showDebugLine(normalizedAxis.a(sTL * (distance - rB)),
					normalizedAxis.a(sTL * (distance - rB) + value), 0xff9966, "separation", 1);
				System.out.println("TL:" + TL + ", rA: " + rA + ", rB: " + rB);
			}
		}

		return false;
	}

	static class SeparationManifold {
		EntityHitResult axis;
		double separation;

		public SeparationManifold() {
			axis = EntityHitResult.a;
			separation = Double.MAX_VALUE;
		}

		public EntityHitResult asSeparationVec() {
			double sep = separation;
			EntityHitResult axis = this.axis;
			return createSeparationVec(sep, axis);
		}

		protected EntityHitResult createSeparationVec(double sep, EntityHitResult axis) {
			return axis.d()
				.a(signum(sep) * (abs(sep) + 1E-4));
		}
	}

}
