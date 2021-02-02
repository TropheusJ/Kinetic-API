package com.tropheus_jay.kinetic_api.foundation.utility.outliner;

import java.util.Optional;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Matrix3f;

public abstract class Outline {

	protected OutlineParams params;
	protected Matrix3f transformNormals;

	public Outline() {
		params = new OutlineParams();
	}

	public abstract void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer);

	public void renderCuboidLine(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer, EntityHitResult start, EntityHitResult end) {
		EntityHitResult diff = end.d(start);
		float hAngle = AngleHelper.deg(afj.d(diff.entity, diff.d));
		float hDistance = (float) diff.d(1, 0, 1)
			.f();
		float vAngle = AngleHelper.deg(afj.d(hDistance, diff.c)) - 90;
		ms.a();
		MatrixStacker.of(ms)
			.translate(start)
			.rotateY(hAngle).rotateX(vAngle);
		renderAACuboidLine(ms, buffer, EntityHitResult.a, new EntityHitResult(0, 0, diff.f()));
		ms.b();
	}

	public void renderAACuboidLine(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer, EntityHitResult start, EntityHitResult end) {
		float lineWidth = params.getLineWidth();
		if (lineWidth == 0)
			return;
		
		OverlayVertexConsumer builder = buffer.getBuffer(RenderTypes.getOutlineSolid());

		EntityHitResult diff = end.d(start);
		if (diff.entity + diff.c + diff.d < 0) {
			EntityHitResult temp = start;
			start = end;
			end = temp;
			diff = diff.a(-1);
		}

		EntityHitResult extension = diff.d()
			.a(lineWidth / 2);
		EntityHitResult plane = VecHelper.axisAlingedPlaneOf(diff);
		Direction face = Direction.getFacing(diff.entity, diff.c, diff.d);
		Axis axis = face.getAxis();

		start = start.d(extension);
		end = end.e(extension);
		plane = plane.a(lineWidth / 2);

		EntityHitResult a1 = plane.e(start);
		EntityHitResult b1 = plane.e(end);
		plane = VecHelper.rotate(plane, -90, axis);
		EntityHitResult a2 = plane.e(start);
		EntityHitResult b2 = plane.e(end);
		plane = VecHelper.rotate(plane, -90, axis);
		EntityHitResult a3 = plane.e(start);
		EntityHitResult b3 = plane.e(end);
		plane = VecHelper.rotate(plane, -90, axis);
		EntityHitResult a4 = plane.e(start);
		EntityHitResult b4 = plane.e(end);

		if (params.disableNormals) {
			face = Direction.UP;
			putQuad(ms, builder, b4, b3, b2, b1, face);
			putQuad(ms, builder, a1, a2, a3, a4, face);
			putQuad(ms, builder, a1, b1, b2, a2, face);
			putQuad(ms, builder, a2, b2, b3, a3, face);
			putQuad(ms, builder, a3, b3, b4, a4, face);
			putQuad(ms, builder, a4, b4, b1, a1, face);
			return;
		}

		putQuad(ms, builder, b4, b3, b2, b1, face);
		putQuad(ms, builder, a1, a2, a3, a4, face.getOpposite());
		EntityHitResult vec = a1.d(a4);
		face = Direction.getFacing(vec.entity, vec.c, vec.d);
		putQuad(ms, builder, a1, b1, b2, a2, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getFacing(vec.entity, vec.c, vec.d);
		putQuad(ms, builder, a2, b2, b3, a3, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getFacing(vec.entity, vec.c, vec.d);
		putQuad(ms, builder, a3, b3, b4, a4, face);
		vec = VecHelper.rotate(vec, -90, axis);
		face = Direction.getFacing(vec.entity, vec.c, vec.d);
		putQuad(ms, builder, a4, b4, b1, a1, face);
	}

	public void putQuad(BufferVertexConsumer ms, OverlayVertexConsumer builder, EntityHitResult v1, EntityHitResult v2, EntityHitResult v3, EntityHitResult v4,
		Direction normal) {
		putQuadUV(ms, builder, v1, v2, v3, v4, 0, 0, 1, 1, normal);
	}

	public void putQuadUV(BufferVertexConsumer ms, OverlayVertexConsumer builder, EntityHitResult v1, EntityHitResult v2, EntityHitResult v3, EntityHitResult v4, float minU,
		float minV, float maxU, float maxV, Direction normal) {
		putVertex(ms, builder, v1, minU, minV, normal);
		putVertex(ms, builder, v2, maxU, minV, normal);
		putVertex(ms, builder, v3, maxU, maxV, normal);
		putVertex(ms, builder, v4, minU, maxV, normal);
	}

	protected void putVertex(BufferVertexConsumer ms, OverlayVertexConsumer builder, EntityHitResult pos, float u, float v, Direction normal) {
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		a peek = ms.c();
		EntityHitResult rgb = params.rgb;
		if (transformNormals == null)
			transformNormals = peek.b();

		int xOffset = 0;
		int yOffset = 0;
		int zOffset = 0;

		if (normal != null) {
			xOffset = normal.getOffsetX();
			yOffset = normal.getOffsetY();
			zOffset = normal.getOffsetZ();
		}

		builder.a(peek.a(), (float) pos.entity, (float) pos.c, (float) pos.d)
			.a((float) rgb.entity, (float) rgb.c, (float) rgb.d, params.alpha)
			.a(u, v)
			.b(ejo.a)
			.b(j, k)
			.a(peek.b(), xOffset, yOffset, zOffset)
			.d();

		transformNormals = null;
	}

	public void tick() {}

	public OutlineParams getParams() {
		return params;
	}

	public static class OutlineParams {
		protected Optional<AllSpecialTextures> faceTexture;
		protected Optional<AllSpecialTextures> hightlightedFaceTexture;
		protected Direction highlightedFace;
		protected boolean fadeLineWidth;
		protected boolean disableCull;
		protected boolean disableNormals;
		protected float alpha;
		protected int lightMapU, lightMapV;
		protected EntityHitResult rgb;
		private float lineWidth;

		public OutlineParams() {
			faceTexture = hightlightedFaceTexture = Optional.empty();
			alpha = 1;
			lineWidth = 1 / 32f;
			fadeLineWidth = true;
			rgb = ColorHelper.getRGB(0xFFFFFF);

			int i = 15 << 20 | 15 << 4;
			lightMapU = i >> 16 & '\uffff';
			lightMapV = i & '\uffff';
		}

		// builder

		public OutlineParams colored(int color) {
			rgb = ColorHelper.getRGB(color);
			return this;
		}

		public OutlineParams lineWidth(float width) {
			this.lineWidth = width;
			return this;
		}

		public OutlineParams withFaceTexture(AllSpecialTextures texture) {
			this.faceTexture = Optional.ofNullable(texture);
			return this;
		}

		public OutlineParams clearTextures() {
			return this.withFaceTextures(null, null);
		}

		public OutlineParams withFaceTextures(AllSpecialTextures texture, AllSpecialTextures highlightTexture) {
			this.faceTexture = Optional.ofNullable(texture);
			this.hightlightedFaceTexture = Optional.ofNullable(highlightTexture);
			return this;
		}

		public OutlineParams highlightFace(@Nullable Direction face) {
			highlightedFace = face;
			return this;
		}

		public OutlineParams disableNormals() {
			disableNormals = true;
			return this;
		}

		public OutlineParams disableCull() {
			disableCull = true;
			return this;
		}

		// getter

		public float getLineWidth() {
			return fadeLineWidth ? alpha * lineWidth : lineWidth;
		}

		public Direction getHighlightedFace() {
			return highlightedFace;
		}

	}

}
