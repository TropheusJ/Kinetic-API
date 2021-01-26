package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StabilizedBearingMovementBehaviour extends MovementBehaviour {

	@Override
	@Environment(EnvType.CLIENT)
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		Direction facing = context.state.c(BambooLeaves.M);
		AllBlockPartials top = AllBlockPartials.BEARING_TOP;
		SuperByteBuffer superBuffer = top.renderOn(context.state);
		float renderPartialTicks = KeyBinding.B()
			.ai();

		// rotate to match blockstate
		Axis axis = facing.getAxis();
		if (axis.isHorizontal())
			superBuffer.rotateCentered(Direction.UP,
				AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));

		// rotate against parent
		float offset = 0;
		int offsetMultiplier = facing.getDirection().offset();
		
		AbstractContraptionEntity entity = context.contraption.entity;
		if (entity instanceof ControlledContraptionEntity) {
			ControlledContraptionEntity controlledCE = (ControlledContraptionEntity) entity;
			if (controlledCE.getRotationAxis() == axis)
				offset = -controlledCE.getAngle(renderPartialTicks);

		} else if (entity instanceof OrientedContraptionEntity) {
			OrientedContraptionEntity orientedCE = (OrientedContraptionEntity) entity;
			if (axis.isVertical())
				offset = -orientedCE.h(renderPartialTicks);
			else {
				if (orientedCE.isInitialOrientationPresent() && orientedCE.getInitialOrientation()
					.getAxis() == axis)
					offset = -orientedCE.g(renderPartialTicks);
			}
		}
		if (offset != 0)
			superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(offset * offsetMultiplier));

		// render
		superBuffer.light(msLocal.c()
			.a());
		superBuffer.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}

}
