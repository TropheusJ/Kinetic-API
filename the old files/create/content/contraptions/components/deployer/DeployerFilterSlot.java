package com.simibubi.kinetic_api.content.contraptions.components.deployer;

import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class DeployerFilterSlot extends ValueBoxTransform {

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		Direction facing = state.c(DeployerBlock.FACING);
		EntityHitResult vec = VecHelper.voxelSpace(8f, 13.5f, 11.5f);

		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
		vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
		vec = VecHelper.rotateCentered(vec, zRot, Axis.Z);

		return vec;
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		Direction facing = state.c(DeployerBlock.FACING);
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		MatrixStacker.of(ms)
			.rotateY(yRot)
			.rotateX(xRot);
	}

}
