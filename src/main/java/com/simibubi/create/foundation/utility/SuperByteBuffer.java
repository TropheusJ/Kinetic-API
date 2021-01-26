package com.simibubi.create.foundation.utility;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlShader.Type;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.GameMode;
import net.minecraft.world.TestableWorld;

public class SuperByteBuffer {

	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

	protected ByteBuffer template;
	protected int formatSize;

	// Vertex Position
	private BufferVertexConsumer transforms;

	// Vertex Texture Coords
	private boolean shouldShiftUV;
	private SpriteShiftEntry spriteShift;
	private float uTarget, vTarget;

	// Vertex Lighting
	private boolean shouldLight;
	private int packedLightCoords;
	private Matrix4f lightTransform;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private float sheetSize;

	public SuperByteBuffer(GlShader buf) {
		Pair<Type, ByteBuffer> state = buf.f();
		ByteBuffer rendered = state.getSecond();
		rendered.order(ByteOrder.nativeOrder()); // Vanilla bug, endianness does not carry over into sliced buffers

		formatSize = buf.getVertexFormat()
			.b();
		int size = state.getFirst()
			.b() * formatSize;

		template = GlDebugInfo.a(size);
		template.order(rendered.order());
		((Buffer) template).limit(((Buffer) rendered).limit());
		template.put(rendered);
		((Buffer) template).rewind();

		transforms = new BufferVertexConsumer();
	}

	public static float getUnInterpolatedU(MipmapHelper sprite, float u) {
		float f = sprite.i() - sprite.h();
		return (u - sprite.h()) / f * 16.0F;
	}

	public static float getUnInterpolatedV(MipmapHelper sprite, float v) {
		float f = sprite.k() - sprite.j();
		return (v - sprite.j()) / f * 16.0F;
	}

	private static final Long2DoubleMap skyLightCache = new Long2DoubleOpenHashMap();
	private static final Long2DoubleMap blockLightCache = new Long2DoubleOpenHashMap();
	Vector4f pos = new Vector4f();
	Vector4f lightPos = new Vector4f();

	public void renderInto(BufferVertexConsumer input, OverlayVertexConsumer builder) {
		ByteBuffer buffer = template;
		if (((Buffer) buffer).limit() == 0)
			return;
		((Buffer) buffer).rewind();

		Matrix4f t = input.c()
			.a()
			.copy();
		Matrix4f localTransforms = transforms.c()
			.a();
		t.multiply(localTransforms);

		if (shouldLight && lightTransform != null) {
			skyLightCache.clear();
			blockLightCache.clear();
		}

		float f = .5f;
		int vertexCount = vertexCount(buffer);
		for (int i = 0; i < vertexCount; i++) {
			float x = getX(buffer, i);
			float y = getY(buffer, i);
			float z = getZ(buffer, i);
			byte r = getR(buffer, i);
			byte g = getG(buffer, i);
			byte b = getB(buffer, i);
			byte a = getA(buffer, i);

			pos.set(x, y, z, 1F);
			pos.transform(t);
			builder.a(pos.getX(), pos.getY(), pos.getZ());

			if (shouldColor) {
				float lum = (r < 0 ? 255 + r : r) / 256f;
				builder.a((int) (this.r * lum), (int) (this.g * lum), (int) (this.b * lum), this.a);
			} else
				builder.a(r, g, b, a);

			float u = getU(buffer, i);
			float v = getV(buffer, i);

			if (shouldShiftUV) {
				float targetU = spriteShift.getTarget()
					.a((getUnInterpolatedU(spriteShift.getOriginal(), u) / sheetSize) + uTarget * 16);
				float targetV = spriteShift.getTarget()
					.b((getUnInterpolatedV(spriteShift.getOriginal(), v) / sheetSize) + vTarget * 16);
				builder.a(targetU, targetV);
			} else
				builder.a(u, v);

			if (shouldLight) {
				int light = packedLightCoords;
				if (lightTransform != null) {
					lightPos.set(((x - f) * 15 / 16f) + f, (y - f) * 15 / 16f + f, (z - f) * 15 / 16f + f, 1F);
					lightPos.transform(localTransforms);
					lightPos.transform(lightTransform);
					light = getLight(KeyBinding.B().r, lightPos);
				}
				builder.a(light);
			} else
				builder.a(getLight(buffer, i));

			builder.b(getNX(buffer, i), getNY(buffer, i), getNZ(buffer, i))
				.d();
		}

		transforms = new BufferVertexConsumer();
		shouldShiftUV = false;
		shouldColor = false;
		shouldLight = false;
	}

	public SuperByteBuffer translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.a(x, y, z);
		return this;
	}

	public SuperByteBuffer rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		transforms.a(axis.getUnitVector()
			.getRadialQuaternion(radians));
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		return translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		shouldShiftUV = true;
		spriteShift = entry;
		uTarget = 0;
		vTarget = 0;
		sheetSize = 1;
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		shouldShiftUV = true;
		spriteShift = entry;
		this.uTarget = uTarget;
		this.vTarget = vTarget;
		this.sheetSize = sheetSize;
		return this;
	}

	public SuperByteBuffer light(int packedLightCoords) {
		shouldLight = true;
		lightTransform = null;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform) {
		shouldLight = true;
		this.lightTransform = lightTransform;
		return this;
	}

	public SuperByteBuffer color(int color) {
		shouldColor = true;
		r = ((color >> 16) & 0xFF);
		g = ((color >> 8) & 0xFF);
		b = (color & 0xFF);
		a = 255;
		return this;
	}

	protected int vertexCount(ByteBuffer buffer) {
		return ((Buffer) buffer).limit() / formatSize;
	}

	protected int getBufferPosition(int vertexIndex) {
		return vertexIndex * formatSize;
	}

	protected float getX(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index));
	}

	protected float getY(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 4);
	}

	protected float getZ(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 8);
	}

	protected byte getR(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 12);
	}

	protected byte getG(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 13);
	}

	protected byte getB(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 14);
	}

	protected byte getA(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 15);
	}

	protected float getU(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 16);
	}

	protected float getV(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 20);
	}

	protected int getLight(ByteBuffer buffer, int index) {
		return buffer.getInt(getBufferPosition(index) + 24);
	}

	protected byte getNX(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 28);
	}

	protected byte getNY(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 29);
	}

	protected byte getNZ(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 30);
	}

	private static int getLight(GameMode world, Vector4f lightPos) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		double sky = 0, block = 0;
		float offset = 1 / 8f;
//		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset) {
//			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset) {
//				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
//					pos.setPos(lightPos.getX() + xOffset, lightPos.getY() + yOffset, lightPos.getZ() + zOffset);
		pos.set(lightPos.getX() + 0, lightPos.getY() + 0, lightPos.getZ() + 0);
		sky += skyLightCache.computeIfAbsent(pos.asLong(), $ -> world.a(TestableWorld.a, pos));
		block += blockLightCache.computeIfAbsent(pos.asLong(), $ -> world.a(TestableWorld.b, pos));
//				}
//			}
//		}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

	public boolean isEmpty() {
		return ((Buffer) template).limit() == 0;
	}

}