package com.simibubi.create.foundation.collision;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import dcg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Box.a;
import net.minecraft.world.timer.Timer;

public class CollisionDebugger {

	public static Timer AABB = new Timer(BlockPos.ORIGIN.up(10));
	public static OrientedBB OBB = new OrientedBB(new Timer(BlockPos.ORIGIN));
	public static EntityHitResult motion = EntityHitResult.a;
	static ContinuousSeparationManifold seperation;
	static double angle = 0;
	static AABBOutline outline;

	public static void onScroll(double delta) {
		angle += delta;
		angle = (int) angle;
		OBB.setRotation(new Matrix3d().asZRotation(AngleHelper.rad(angle)));
	}

	public static void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		ms.a();
		outline = new AABBOutline(OBB.getAsAxisAlignedBB());
		outline.getParams()
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null)
			.colored(0xffffff);
		if (seperation != null)
			outline.getParams()
				.lineWidth(1 / 64f)
				.colored(0xff6544);
		MatrixStacker.of(ms)
			.translate(OBB.center);
		ms.c()
			.a()
			.multiply(OBB.rotation.getAsMatrix4f());
		MatrixStacker.of(ms)
			.translateBack(OBB.center);
		outline.render(ms, buffer);
		ms.b();

//		ms.push();
//		if (motion.length() != 0 && (seperation == null || seperation.getTimeOfImpact() != 1)) {
//			outline.getParams()
//				.colored(0x6544ff)
//				.lineWidth(1 / 32f);
//			MatrixStacker.of(ms)
//				.translate(seperation != null ? seperation.getAllowedMotion(motion) : motion)
//				.translate(OBB.center);
//			ms.peek()
//				.getModel()
//				.multiply(OBB.rotation.getAsMatrix4f());
//			MatrixStacker.of(ms)
//				.translateBack(OBB.center);
//			outline.render(ms, buffer);
//		}
//		ms.pop();

		ms.a();
		if (seperation != null) {
			EntityHitResult asSeparationVec = seperation.asSeparationVec(.5f);
			if (asSeparationVec != null) {
				outline.getParams()
					.colored(0x65ff44)
					.lineWidth(1 / 32f);
				MatrixStacker.of(ms)
					.translate(asSeparationVec)
					.translate(OBB.center);
				ms.c()
					.a()
					.multiply(OBB.rotation.getAsMatrix4f());
				MatrixStacker.of(ms)
					.translateBack(OBB.center);
				outline.render(ms, buffer);
			}
		}
		ms.b();
	}

	public static void tick() {
		AABB = new Timer(BlockPos.ORIGIN.up(60)).d(.5, 0, .5);
		motion = EntityHitResult.a;
		Box mouse = KeyBinding.B().v;
		if (mouse != null && mouse.c() == a.b) {
			dcg hit = (dcg) mouse;
			OBB.setCenter(hit.e());
			seperation = OBB.intersect(AABB, motion);
		}
		CreateClient.outliner.showAABB(AABB, AABB)
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null);
	}

	static void showDebugLine(EntityHitResult relativeStart, EntityHitResult relativeEnd, int color, String id, int offset) {
		EntityHitResult center = CollisionDebugger.AABB.f()
			.b(0, 1 + offset / 16f, 0);
		CreateClient.outliner.showLine(id + OBBCollider.checkCount, center.e(relativeStart), center.e(relativeEnd))
			.colored(color)
			.lineWidth(1 / 32f);
	}

}
