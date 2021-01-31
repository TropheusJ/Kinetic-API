package com.simibubi.kinetic_api.content.logistics.block.redstone;

import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.HayBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;

public class FilteredDetectorFilterSlot extends ValueBoxTransform {
	EntityHitResult position = VecHelper.voxelSpace(8f, 15.5f, 11f);

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		return rotateHorizontally(state, position);
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		float yRot = AngleHelper.horizontalAngle(state.c(HayBlock.aq)) + 180;
		MatrixStacker.of(ms)
			.rotateY(yRot)
			.rotateX(90);
	}

}
