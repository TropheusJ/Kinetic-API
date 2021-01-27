package com.simibubi.create.content.contraptions.components.saw;

import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;

public class SawFilterSlot extends ValueBoxTransform {

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		if (state.c(SawBlock.FACING) != Direction.UP)
			return null;
		EntityHitResult x = VecHelper.voxelSpace(8f, 12.5f, 12.25f);
		EntityHitResult z = VecHelper.voxelSpace(12.25f, 12.5f, 8f);
		return state.c(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? z : x;
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		int yRot = state.c(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 270 : 180;
		MatrixStacker.of(ms)
			.rotateY(yRot)
			.rotateX(90);
	}

}
