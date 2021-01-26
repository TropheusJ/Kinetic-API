package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.block.enums.BambooLeaves.M;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class DrillRenderer extends KineticTileEntityRenderer {

	public DrillRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouth(te.p());
	}

	protected static SuperByteBuffer getRotatingModel(PistonHandler state) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouth(state);
	}

	public static void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		BufferVertexConsumer[] matrixStacks = new BufferVertexConsumer[] { ms, msLocal };
		PistonHandler state = context.state;
		SuperByteBuffer superBuffer = AllBlockPartials.DRILL_HEAD.renderOn(state);
		Direction facing = state.c(DrillBlock.FACING);
		
		float speed = (float) (context.contraption.stalled
			|| !VecHelper.isVecPointingTowards(context.relativeMotion, state.c(M)
				.getOpposite()) ? context.getAnimationSpeed() : 0);
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360));

		for (BufferVertexConsumer m : matrixStacks)
			MatrixStacker.of(m)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing))
				.rotateZ(angle)
				.unCentre();
		
		superBuffer
			.light(msLocal.c()
			.a())
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}

}