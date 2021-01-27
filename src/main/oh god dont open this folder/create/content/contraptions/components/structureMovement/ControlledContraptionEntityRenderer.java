package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.DolphinEntityRenderer;
import net.minecraft.util.math.Direction.Axis;

public class ControlledContraptionEntityRenderer extends AbstractContraptionEntityRenderer<ControlledContraptionEntity> {

	public ControlledContraptionEntityRenderer(DolphinEntityRenderer p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	protected void transform(ControlledContraptionEntity entity, float partialTicks,
		BufferVertexConsumer[] matrixStacks) {
		float angle = entity.getAngle(partialTicks);
		Axis axis = entity.getRotationAxis();

		for (BufferVertexConsumer stack : matrixStacks)
			MatrixStacker.of(stack)
				.nudge(entity.X())
				.centre()
				.rotate(angle, axis)
				.unCentre();
	}

}
