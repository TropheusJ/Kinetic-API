package com.simibubi.kinetic_api.content.logistics.block.funnel;

import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.DirectionHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		Direction side = getSide();
		float horizontalAngle = AngleHelper.horizontalAngle(side);
		Direction funnelFacing = FunnelBlock.getFunnelFacing(state);
		float stateAngle = AngleHelper.horizontalAngle(funnelFacing);

		if (state.b() instanceof BeltFunnelBlock) {
			switch (state.c(BeltFunnelBlock.SHAPE)) {

//			case CONNECTED:
//				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 15.5f, 8), stateAngle, Axis.Y);
			case EXTENDED:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 15.5f, 13), stateAngle, Axis.Y);
			case PULLING:
			case PUSHING:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.1, 8.7f), horizontalAngle, Axis.Y);
			default:
			case RETRACTED:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 13, 7.5f), horizontalAngle, Axis.Y);
			}
		}

		if (!funnelFacing.getAxis()
			.isHorizontal()) {
			EntityHitResult southLocation = VecHelper.voxelSpace(8, funnelFacing == Direction.DOWN ? 3 : 13, 15.5f);
			return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
		}

		Direction verticalDirection = DirectionHelper.rotateAround(getSide(), funnelFacing.rotateYClockwise()
			.getAxis());
		if (funnelFacing.getAxis() == Axis.Z)
			verticalDirection = verticalDirection.getOpposite();
		float yRot = -AngleHelper.horizontalAngle(verticalDirection) + 180;
		float xRot = -90;
		boolean alongX = funnelFacing.getAxis() == Axis.X;
		float zRotLast = alongX ^ funnelFacing.getDirection() == AxisDirection.POSITIVE ? 180 : 0;

		EntityHitResult vec = VecHelper.voxelSpace(8, 13, .5f);
		vec = vec.a(.5, .5, .5);
		vec = VecHelper.rotate(vec, zRotLast, Axis.Z);
		vec = VecHelper.rotate(vec, yRot, Axis.Y);
		vec = VecHelper.rotate(vec, alongX ? 0 : xRot, Axis.X);
		vec = VecHelper.rotate(vec, alongX ? xRot : 0, Axis.Z);
		vec = vec.b(.5, .5, .5);
		return vec;
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (facing.getAxis()
			.isVertical()) {
			super.rotate(state, ms);
			return;
		}

		boolean isBeltFunnel = state.b() instanceof BeltFunnelBlock;
		if (isBeltFunnel && state.c(BeltFunnelBlock.SHAPE) != Shape.EXTENDED) {
			Shape shape = state.c(BeltFunnelBlock.SHAPE);
			super.rotate(state, ms);
			if (shape == Shape.PULLING || shape == Shape.PUSHING)
				MatrixStacker.of(ms).rotateX(-22.5f);
			return;
		}

		Direction verticalDirection = DirectionHelper.rotateAround(getSide(), facing.rotateYClockwise()
			.getAxis());
		if (facing.getAxis() == Axis.Z)
			verticalDirection = verticalDirection.getOpposite();

		float yRot = -AngleHelper.horizontalAngle(verticalDirection) + 180;
		float xRot = -90;
		boolean alongX = facing.getAxis() == Axis.X;
		float zRotLast = alongX ^ facing.getDirection() == AxisDirection.POSITIVE ? 180 : 0;


		MatrixStacker.of(ms)
			.rotateZ(alongX ? xRot : 0)
			.rotateX(alongX ? 0 : xRot)
			.rotateY(yRot)
			.rotateZ(zRotLast);
	}

	@Override
	protected boolean isSideActive(PistonHandler state, Direction direction) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (facing == null)
			return false;

		if (state.b() instanceof BeltFunnelBlock)
			return state.c(BeltFunnelBlock.SHAPE) != Shape.EXTENDED ? direction == facing : direction == Direction.UP;

		return direction.getAxis() != facing.getAxis();
	}

	@Override
	protected EntityHitResult getSouthLocation() {
		return EntityHitResult.a;
	}

}
