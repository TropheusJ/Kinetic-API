package com.simibubi.kinetic_api.foundation.fluid;

import java.util.function.Function;
import afj;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import cut;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.BufferVertexConsumer.a;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class FluidRenderer {

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, BackgroundRenderer buffer, BufferVertexConsumer ms, int light) {
		cut fluid = fluidStack.getFluid();
		FluidAttributes fluidAttributes = fluid.getAttributes();
		Function<Identifier, MipmapHelper> spriteAtlas = KeyBinding.B()
			.a(GrindstoneScreenHandler.result);
		MipmapHelper flowTexture = spriteAtlas.apply(fluidAttributes.getFlowingTexture(fluidStack));
		MipmapHelper stillTexture = spriteAtlas.apply(fluidAttributes.getStillTexture(fluidStack));

		int color = fluidAttributes.getColor(fluidStack);
		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.f());
		MatrixStacker msr = MatrixStacker.of(ms);
		int blockLightIn = (light >> 4) & 0xf;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity(fluidStack));
		light = (light & 0xf00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		ms.a();
		msr.centre()
			.rotateY(AngleHelper.horizontalAngle(direction))
			.rotateX(direction == Direction.UP ? 0 : direction == Direction.DOWN ? 180 : 90)
			.unCentre();
		ms.a(.5, 0, .5);

		float h = (float) (radius);
		float hMin = (float) (-radius);
		float hMax = (float) (radius);
		float y = inbound ? 0 : .5f;
		float yMin = y;
		float yMax = y + afj.a(progress * .5f - 1e-6f, 0, 1);

		for (int i = 0; i < 4; i++) {
			ms.a();
			renderTiledHorizontalFace(h, Direction.SOUTH, hMin, yMin, hMax, yMax, builder, ms, light, color,
				flowTexture);
			ms.b();
			msr.rotateY(90);
		}

		if (progress != 1)
			renderTiledVerticalFace(yMax, Direction.UP, hMin, hMin, hMax, hMax, builder, ms, light, color,
				stillTexture);

		ms.b();

	}

	public static void renderTiledFluidBB(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, BackgroundRenderer buffer, BufferVertexConsumer ms, int light, boolean renderBottom) {
		cut fluid = fluidStack.getFluid();
		FluidAttributes fluidAttributes = fluid.getAttributes();
		MipmapHelper fluidTexture = KeyBinding.B()
			.a(GrindstoneScreenHandler.result)
			.apply(fluidAttributes.getStillTexture(fluidStack));

		int color = fluidAttributes.getColor(fluidStack);
		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.f());
		MatrixStacker msr = MatrixStacker.of(ms);
		EntityHitResult center = new EntityHitResult(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);

		int blockLightIn = (light >> 4) & 0xf;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity(fluidStack));
		light = (light & 0xf00000) | luminosity << 4;

		ms.a();
		if (fluidStack.getFluid()
			.getAttributes()
			.isLighterThanAir())
			MatrixStacker.of(ms)
				.translate(center)
				.rotateX(180)
				.translateBack(center);

		for (Direction side : Iterate.directions) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			if (side.getAxis()
				.isHorizontal()) {
				ms.a();

				if (side.getDirection() == AxisDirection.NEGATIVE)
					msr.translate(center)
						.rotateY(180)
						.translateBack(center);

				boolean X = side.getAxis() == Axis.X;
				int darkColor = ColorHelper.mixColors(color, 0xff000011, 1 / 4f);
				renderTiledHorizontalFace(X ? xMax : zMax, side, X ? zMin : xMin, yMin, X ? zMax : xMax, yMax, builder,
					ms, light, darkColor, fluidTexture);

				ms.b();
				continue;
			}

			renderTiledVerticalFace(side == Direction.UP ? yMax : yMin, side, xMin, zMin, xMax, zMax, builder, ms,
				light, color, fluidTexture);
		}

		ms.b();

	}

	private static void renderTiledVerticalFace(float y, Direction face, float xMin, float zMin, float xMax, float zMax,
		OverlayVertexConsumer builder, BufferVertexConsumer ms, int light, int color, MipmapHelper texture) {
		float x2 = 0;
		float z2 = 0;
		for (float x1 = xMin; x1 < xMax; x1 = x2) {
			x2 = Math.min((int) (x1 + 1), xMax);
			for (float z1 = zMin; z1 < zMax; z1 = z2) {
				z2 = Math.min((int) (z1 + 1), zMax);

				float u1 = texture.a(local(x1) * 16);
				float v1 = texture.b(local(z1) * 16);
				float u2 = texture.a(x2 == xMax ? local(x2) * 16 : 16);
				float v2 = texture.b(z2 == zMax ? local(z2) * 16 : 16);

				putVertex(builder, ms, x1, y, z2, color, u1, v2, face, light);
				putVertex(builder, ms, x2, y, z2, color, u2, v2, face, light);
				putVertex(builder, ms, x2, y, z1, color, u2, v1, face, light);
				putVertex(builder, ms, x1, y, z1, color, u1, v1, face, light);
			}
		}
	}

	private static void renderTiledHorizontalFace(float h, Direction face, float hMin, float yMin, float hMax,
		float yMax, OverlayVertexConsumer builder, BufferVertexConsumer ms, int light, int color, MipmapHelper texture) {
		boolean X = face.getAxis() == Axis.X;

		float h2 = 0;
		float y2 = 0;

		for (float h1 = hMin; h1 < hMax; h1 = h2) {
			h2 = Math.min((int) (h1 + 1), hMax);
			for (float y1 = yMin; y1 < yMax; y1 = y2) {
				y2 = Math.min((int) (y1 + 1), yMax);

				int multiplier = texture.f() == 32 ? 8 : 16;
				float u1 = texture.a(local(h1) * multiplier);
				float v1 = texture.b(local(y1) * multiplier);
				float u2 = texture.a(h2 == hMax ? local(h2) * multiplier : multiplier);
				float v2 = texture.b(y2 == yMax ? local(y2) * multiplier : multiplier);

				float x1 = X ? h : h1;
				float x2 = X ? h : h2;
				float z1 = X ? h1 : h;
				float z2 = X ? h2 : h;

				putVertex(builder, ms, x2, y2, z1, color, u1, v2, face, light);
				putVertex(builder, ms, x1, y2, z2, color, u2, v2, face, light);
				putVertex(builder, ms, x1, y1, z2, color, u2, v1, face, light);
				putVertex(builder, ms, x2, y1, z1, color, u1, v1, face, light);
			}
		}
	}

	private static float local(float f) {
		if (f < 0)
			f += 10;
		return f - ((int) f);
	}

	private static void putVertex(OverlayVertexConsumer builder, BufferVertexConsumer ms, float x, float y, float z, int color, float u,
		float v, Direction face, int light) {

		Vec3i n = face.getVector();
		a peek = ms.c();
		int ff = 0xff;
		int a = color >> 24 & ff;
		int r = color >> 16 & ff;
		int g = color >> 8 & ff;
		int b = color & ff;

		builder.a(peek.a(), x, y, z)
			.a(r, g, b, a)
			.a(u, v)
			.a(light)
			.b(n.getX(), n.getY(), n.getZ())
			.d();
	}

}
