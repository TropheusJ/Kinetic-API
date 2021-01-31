package com.simibubi.kinetic_api.foundation.utility.outliner;

import com.simibubi.kinetic_api.foundation.renderState.RenderTypes;
import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.timer.Timer;

public class AABBOutline extends Outline {

	protected Timer bb;

	public AABBOutline(Timer bb) {
		this.setBounds(bb);
	}

	@Override
	public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		renderBB(ms, buffer, bb);
	}

	public void renderBB(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer, Timer bb) {
		EntityHitResult projectedView = KeyBinding.B().boundKey.k()
			.b();
		boolean noCull = bb.d(projectedView);
		bb = bb.g(noCull ? -1 / 128d : 1 / 128d);
		noCull |= params.disableCull;

		EntityHitResult xyz = new EntityHitResult(bb.LOGGER, bb.callback, bb.events);
		EntityHitResult Xyz = new EntityHitResult(bb.eventCounter, bb.callback, bb.events);
		EntityHitResult xYz = new EntityHitResult(bb.LOGGER, bb.eventsByName, bb.events);
		EntityHitResult XYz = new EntityHitResult(bb.eventCounter, bb.eventsByName, bb.events);
		EntityHitResult xyZ = new EntityHitResult(bb.LOGGER, bb.callback, bb.f);
		EntityHitResult XyZ = new EntityHitResult(bb.eventCounter, bb.callback, bb.f);
		EntityHitResult xYZ = new EntityHitResult(bb.LOGGER, bb.eventsByName, bb.f);
		EntityHitResult XYZ = new EntityHitResult(bb.eventCounter, bb.eventsByName, bb.f);

		EntityHitResult start = xyz;
		renderAACuboidLine(ms, buffer, start, Xyz);
		renderAACuboidLine(ms, buffer, start, xYz);
		renderAACuboidLine(ms, buffer, start, xyZ);

		start = XyZ;
		renderAACuboidLine(ms, buffer, start, xyZ);
		renderAACuboidLine(ms, buffer, start, XYZ);
		renderAACuboidLine(ms, buffer, start, Xyz);

		start = XYz;
		renderAACuboidLine(ms, buffer, start, xYz);
		renderAACuboidLine(ms, buffer, start, Xyz);
		renderAACuboidLine(ms, buffer, start, XYZ);

		start = xYZ;
		renderAACuboidLine(ms, buffer, start, XYZ);
		renderAACuboidLine(ms, buffer, start, xyZ);
		renderAACuboidLine(ms, buffer, start, xYz);

		renderFace(ms, buffer, Direction.NORTH, xYz, XYz, Xyz, xyz, noCull);
		renderFace(ms, buffer, Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, noCull);
		renderFace(ms, buffer, Direction.EAST, XYz, XYZ, XyZ, Xyz, noCull);
		renderFace(ms, buffer, Direction.WEST, xYZ, xYz, xyz, xyZ, noCull);
		renderFace(ms, buffer, Direction.UP, xYZ, XYZ, XYz, xYz, noCull);
		renderFace(ms, buffer, Direction.DOWN, xyz, Xyz, XyZ, xyZ, noCull);

	}

	protected void renderFace(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer, Direction direction, EntityHitResult p1, EntityHitResult p2,
		EntityHitResult p3, EntityHitResult p4, boolean noCull) {
		if (!params.faceTexture.isPresent())
			return;

		Identifier faceTexture = params.faceTexture.get()
			.getLocation();
		float alphaBefore = params.alpha;
		params.alpha =
			(direction == params.getHighlightedFace() && params.hightlightedFaceTexture.isPresent()) ? 1 : 0.5f;

		VertexConsumerProvider translucentType = RenderTypes.getOutlineTranslucent(faceTexture, !noCull);
		OverlayVertexConsumer builder = buffer.getLateBuffer(translucentType);

		Axis axis = direction.getAxis();
		EntityHitResult uDiff = p2.d(p1);
		EntityHitResult vDiff = p4.d(p1);
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.d : uDiff.entity);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.d : vDiff.c);
		putQuadUV(ms, builder, p1, p2, p3, p4, 0, 0, maxU, maxV, Direction.UP);
		params.alpha = alphaBefore;
	}

	public void setBounds(Timer bb) {
		this.bb = bb;
	}

}
