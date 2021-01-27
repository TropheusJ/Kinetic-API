package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;

public class AdjustableRepeaterScrollSlot extends ValueBoxTransform {

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		return VecHelper.voxelSpace(8, 3f, 8);
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		float yRot = AngleHelper.horizontalAngle(state.c(BambooLeaves.O)) + 180;
		MatrixStacker.of(ms)
			.rotateY(yRot)
			.rotateX(90);
	}

}
