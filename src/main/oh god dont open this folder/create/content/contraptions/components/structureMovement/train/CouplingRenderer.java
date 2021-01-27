package com.simibubi.create.content.contraptions.components.structureMovement.train;

import static afj.d;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer.CartEndpoint;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

public class CouplingRenderer {

	public static void renderAll(BufferVertexConsumer ms, BackgroundRenderer buffer) {
		CouplingHandler.forEachLoadedCoupling(KeyBinding.B().r,
			c -> {
				if (c.getFirst().hasContraptionCoupling(true))
					return;
				CouplingRenderer.renderCoupling(ms, buffer, c.map(MinecartController::cart));	
			});
	}

	public static void tickDebugModeRenders() {
		if (KineticDebugger.isActive())
			CouplingHandler.forEachLoadedCoupling(KeyBinding.B().r, CouplingRenderer::doDebugRender);
	}

	public static void renderCoupling(BufferVertexConsumer ms, BackgroundRenderer buffer, Couple<ScheduleBuilder> carts) {
		DragonHeadEntityModel world = KeyBinding.B().r;
		
		if (carts.getFirst() == null || carts.getSecond() == null)
			return;
		
		Couple<Integer> lightValues =
			carts.map(c -> JsonGlProgram.a(world, new BlockPos(c.cb()
				.f())));

		EntityHitResult center = carts.getFirst()
			.cz()
			.e(carts.getSecond()
				.cz())
			.a(.5f);

		Couple<CartEndpoint> transforms = carts.map(c -> getSuitableCartEndpoint(c, center));

		PistonHandler renderState = BellBlock.FACING.n();
		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.c());
		SuperByteBuffer attachment = AllBlockPartials.COUPLING_ATTACHMENT.renderOn(renderState);
		SuperByteBuffer ring = AllBlockPartials.COUPLING_RING.renderOn(renderState);
		SuperByteBuffer connector = AllBlockPartials.COUPLING_CONNECTOR.renderOn(renderState);

		EntityHitResult zero = EntityHitResult.a;
		EntityHitResult firstEndpoint = transforms.getFirst()
			.apply(zero);
		EntityHitResult secondEndpoint = transforms.getSecond()
			.apply(zero);
		EntityHitResult endPointDiff = secondEndpoint.d(firstEndpoint);
		double connectorYaw = -Math.atan2(endPointDiff.d, endPointDiff.entity) * 180.0D / Math.PI;
		double connectorPitch = Math.atan2(endPointDiff.c, endPointDiff.d(1, 0, 1)
			.f()) * 180 / Math.PI;

		MatrixStacker msr = MatrixStacker.of(ms);
		carts.forEachWithContext((cart, isFirst) -> {
			CartEndpoint cartTransform = transforms.get(isFirst);

			ms.a();
			cartTransform.apply(ms);
			attachment.light(lightValues.get(isFirst))
				.renderInto(ms, builder);
			msr.rotateY(connectorYaw - cartTransform.yaw);
			ring.light(lightValues.get(isFirst))
				.renderInto(ms, builder);
			ms.b();
		});

		int l1 = lightValues.getFirst();
		int l2 = lightValues.getSecond();
		int meanBlockLight = (((l1 >> 4) & 0xf) + ((l2 >> 4) & 0xf)) / 2;
		int meanSkyLight = (((l1 >> 20) & 0xf) + ((l2 >> 20) & 0xf)) / 2;

		ms.a();
		msr.translate(firstEndpoint)
			.rotateY(connectorYaw)
			.rotateZ(connectorPitch);
		ms.a((float) endPointDiff.f(), 1, 1);

		connector.light(meanSkyLight << 20 | meanBlockLight << 4)
			.renderInto(ms, builder);
		ms.b();
	}

	private static CartEndpoint getSuitableCartEndpoint(ScheduleBuilder cart, EntityHitResult centerOfCoupling) {
		long i = cart.X() * 493286711L;
		i = i * i * 4392167121L + i * 98761L;
		float x = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float y = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F + 0.375F;
		float z = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

		float pt = KeyBinding.B()
			.ai();

		double xIn = d(pt, cart.D, cart.cC());
		double yIn = d(pt, cart.E, cart.cD());
		double zIn = d(pt, cart.F, cart.cG());

		float yaw = g(pt, cart.r, cart.p);
		float pitch = g(pt, cart.s, cart.q);
		float roll = cart.m() - pt;

		float rollAmplifier = cart.k() - pt;
		if (rollAmplifier < 0.0F)
			rollAmplifier = 0.0F;
		roll = roll > 0 ? afj.a(roll) * roll * rollAmplifier / 10.0F * cart.n() : 0;

		EntityHitResult positionVec = new EntityHitResult(xIn, yIn, zIn);
		EntityHitResult frontVec = positionVec.e(VecHelper.rotate(new EntityHitResult(.5, 0, 0), 180 - yaw, Axis.Y));
		EntityHitResult backVec = positionVec.e(VecHelper.rotate(new EntityHitResult(-.5, 0, 0), 180 - yaw, Axis.Y));

		EntityHitResult railVecOfPos = cart.p(xIn, yIn, zIn);
		boolean flip = false;

		if (railVecOfPos != null) {
			frontVec = cart.a(xIn, yIn, zIn, (double) 0.3F);
			backVec = cart.a(xIn, yIn, zIn, (double) -0.3F);
			if (frontVec == null)
				frontVec = railVecOfPos;
			if (backVec == null)
				backVec = railVecOfPos;

			x += railVecOfPos.entity;
			y += (frontVec.c + backVec.c) / 2;
			z += railVecOfPos.d;

			EntityHitResult endPointDiff = backVec.b(-frontVec.entity, -frontVec.c, -frontVec.d);
			if (endPointDiff.f() != 0.0D) {
				endPointDiff = endPointDiff.d();
				yaw = (float) (Math.atan2(endPointDiff.d, endPointDiff.entity) * 180.0D / Math.PI);
				pitch = (float) (Math.atan(endPointDiff.c) * 73.0D);
			}
		} else {
			x += xIn;
			y += yIn;
			z += zIn;
		}

		final float offsetMagnitude = 13 / 16f;
		boolean isBackFaceCloser =
			frontVec.g(centerOfCoupling) > backVec.g(centerOfCoupling);
		flip = isBackFaceCloser;
		float offset = isBackFaceCloser ? -offsetMagnitude : offsetMagnitude;

		return new CartEndpoint(x, y + 2 / 16f, z, 180 - yaw, -pitch, roll, offset, flip);
	}

	static class CartEndpoint {

		float x;
		float y;
		float z;
		float yaw;
		float pitch;
		float roll;
		float offset;
		boolean flip;

		public CartEndpoint(float x, float y, float z, float yaw, float pitch, float roll, float offset, boolean flip) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
			this.offset = offset;
			this.flip = flip;
		}

		public EntityHitResult apply(EntityHitResult vec) {
			vec = vec.b(offset, 0, 0);
			vec = VecHelper.rotate(vec, roll, Axis.X);
			vec = VecHelper.rotate(vec, pitch, Axis.Z);
			vec = VecHelper.rotate(vec, yaw, Axis.Y);
			return vec.b(x, y, z);
		}

		public void apply(BufferVertexConsumer ms) {
			ms.a(x, y, z);
			ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(yaw));
			ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(pitch));
			ms.a(Vector3f.POSITIVE_X.getDegreesQuaternion(roll));
			ms.a(offset, 0, 0);
			if (flip)
				ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
		}

	}

	public static void doDebugRender(Couple<MinecartController> c) {
		int yOffset = 1;
		MinecartController first = c.getFirst();
		ScheduleBuilder mainCart = first.cart();
		EntityHitResult mainCenter = mainCart.cz()
			.b(0, yOffset, 0);
		EntityHitResult connectedCenter = c.getSecond()
			.cart()
			.cz()
			.b(0, yOffset, 0);

		int color = ColorHelper.mixColors(0xabf0e9, 0xee8572, (float) afj
			.a(Math.abs(first.getCouplingLength(true) - connectedCenter.f(mainCenter)) * 8, 0, 1));

		CreateClient.outliner.showLine(mainCart.X() + "", mainCenter, connectedCenter)
			.colored(color)
			.lineWidth(1 / 8f);

		EntityHitResult point = mainCart.cz()
			.b(0, yOffset, 0);
		CreateClient.outliner.showLine(mainCart.X() + "_dot", point, point.b(0, 1 / 128f, 0))
			.colored(0xffffff)
			.lineWidth(1 / 4f);
	}

}
