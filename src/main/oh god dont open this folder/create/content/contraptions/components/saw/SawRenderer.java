package com.simibubi.create.content.contraptions.components.saw;

import static net.minecraft.block.enums.BambooLeaves.M;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import ebv;
import elg;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;

public class SawRenderer extends SafeTileEntityRenderer<SawTileEntity> {

	public SawRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(SawTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer, int light,
		int overlay) {
		renderBlade(te, ms, buffer, light);
		renderItems(te, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);
		renderShaft(te, ms, buffer, light, overlay);
	}

	protected void renderBlade(SawTileEntity te, BufferVertexConsumer ms, BackgroundRenderer buffer, int light) {
		PistonHandler blockState = te.p();
		SuperByteBuffer superBuffer;
		AllBlockPartials partial;
		float speed = te.getSpeed();

		ms.a();

		if (SawBlock.isHorizontal(blockState)) {
			if (speed > 0) {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_ACTIVE;
			} else if (speed < 0) {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_REVERSED;
			} else {
				partial = AllBlockPartials.SAW_BLADE_HORIZONTAL_INACTIVE;
			}
		} else {
			if (te.getSpeed() > 0) {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE;
			} else if (speed < 0) {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_REVERSED;
			} else {
				partial = AllBlockPartials.SAW_BLADE_VERTICAL_INACTIVE;
			}

			if (!blockState.c(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
				MatrixStacker.of(ms)
					.centre()
					.rotateY(90)
					.unCentre();
		}
		superBuffer = partial.renderOnDirectionalSouth(blockState);
		superBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.d()));

		ms.b();
	}

	protected void renderShaft(SawTileEntity te, BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay) {
		KineticTileEntityRenderer.renderRotatingBuffer(te, getRotatedModel(te), ms,
			buffer.getBuffer(VertexConsumerProvider.c()), light);
	}

	protected void renderItems(SawTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		boolean processingMode = te.p()
			.c(SawBlock.FACING) == Direction.UP;
		if (processingMode && !te.inventory.isEmpty()) {
			boolean alongZ = !te.p()
				.c(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
			ms.a();

			boolean moving = te.inventory.recipeDuration != 0;
			float offset = moving ? (float) (te.inventory.remainingTime) / te.inventory.recipeDuration : 0;
			float processingSpeed = afj.a(Math.abs(te.getSpeed()) / 32, 1, 128);
			if (moving)
				offset = afj.a(offset + ((-partialTicks + .5f) * processingSpeed) / te.inventory.recipeDuration, 0, 1);

			if (te.getSpeed() == 0)
				offset = .5f;
			if (te.getSpeed() < 0 ^ alongZ)
				offset = 1 - offset;

			for (int i = 0; i < te.inventory.getSlots(); i++) {
				ItemCooldownManager stack = te.inventory.getStackInSlot(i);
				if (stack.a())
					continue;

				HorseEntityRenderer itemRenderer = KeyBinding.B()
					.ac();
				elg modelWithOverrides = itemRenderer.a(stack, te.v(), null);
				boolean blockItem = modelWithOverrides.b();

				ms.a(alongZ ? offset : .5, blockItem ? .925f : 13f / 16f, alongZ ? .5 : offset);

				ms.a(.5f, .5f, .5f);
				if (alongZ)
					ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
				ms.a(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
				itemRenderer.a(stack, ModelElementTexture.b.i, light, overlay, ms, buffer);
				break;
			}

			ms.b();
		}
	}

	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		PistonHandler state = te.p();
		if (state.c(M).getAxis().isHorizontal())
			return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(state.rotate(te.v(), te.o(), RespawnAnchorBlock.field_26443));
		return CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE,
				getRenderedBlockState(te));
	}

	protected PistonHandler getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

	public static void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		BufferVertexConsumer[] matrixStacks = new BufferVertexConsumer[] { ms, msLocal };
		PistonHandler state = context.state;
		SuperByteBuffer superBuffer;
		Direction facing = state.c(SawBlock.FACING);

		EntityHitResult facingVec = EntityHitResult.b(context.state.c(SawBlock.FACING)
			.getVector());
		facingVec = context.rotation.apply(facingVec);

		Direction closestToFacing = Direction.getFacing(facingVec.entity, facingVec.c, facingVec.d);

		boolean horizontal = closestToFacing.getAxis()
			.isHorizontal();
		boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
		boolean moving = context.getAnimationSpeed() != 0;
		boolean shouldAnimate =
			(context.contraption.stalled && horizontal) || (!context.contraption.stalled && !backwards && moving);

		if (SawBlock.isHorizontal(state)) {
			if (shouldAnimate)
				superBuffer = AllBlockPartials.SAW_BLADE_HORIZONTAL_ACTIVE.renderOn(state);
			else
				superBuffer = AllBlockPartials.SAW_BLADE_HORIZONTAL_INACTIVE.renderOn(state);
		} else {
			if (shouldAnimate)
				superBuffer = AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE.renderOn(state);
			else
				superBuffer = AllBlockPartials.SAW_BLADE_VERTICAL_INACTIVE.renderOn(state);
		}

		for (BufferVertexConsumer m : matrixStacks) {
			MatrixStacker.of(m)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing));
			if (!SawBlock.isHorizontal(state))
				MatrixStacker.of(m)
					.rotateZ(state.c(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 0 : 90);
			MatrixStacker.of(m)
				.unCentre();
		}

		superBuffer.light(msLocal.c()
			.a())
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.d()));
	}

}
