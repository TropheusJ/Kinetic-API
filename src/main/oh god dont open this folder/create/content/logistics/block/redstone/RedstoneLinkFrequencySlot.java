package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class RedstoneLinkFrequencySlot extends ValueBoxTransform.Dual {

	public RedstoneLinkFrequencySlot(boolean first) {
		super(first);
	}

	EntityHitResult horizontal = VecHelper.voxelSpace(10f, 5.5f, 2.5f);
	EntityHitResult vertical = VecHelper.voxelSpace(10f, 2.5f, 5.5f);

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		Direction facing = state.c(RedstoneLinkBlock.SHAPE);
		EntityHitResult location = vertical;

		if (facing.getAxis()
			.isHorizontal()) {
			location = horizontal;
			if (!isFirst())
				location = location.b(0, 5 / 16f, 0);
			return rotateHorizontally(state, location);
		}

		if (!isFirst())
			location = location.b(0, 0, 5 / 16f);
		location = VecHelper.rotateCentered(location, facing == Direction.DOWN ? 180 : 0, Axis.X);
		return location;
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		Direction facing = state.c(RedstoneLinkBlock.SHAPE);
		float yRot = facing.getAxis()
			.isVertical() ? 0 : AngleHelper.horizontalAngle(facing) + 180;
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		MatrixStacker.of(ms)
			.rotateY(yRot)
			.rotateX(xRot);
	}

	@Override
	protected float getScale() {
		return .5f;
	}

}
