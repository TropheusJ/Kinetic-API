package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class BearingRenderer extends KineticTileEntityRenderer {

	public BearingRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		IBearingTileEntity bearingTe = (IBearingTileEntity) te;
		final Direction facing = te.p()
			.c(BambooLeaves.M);
		AllBlockPartials top =
			bearingTe.isWoodenTop() ? AllBlockPartials.BEARING_TOP_WOODEN : AllBlockPartials.BEARING_TOP;
		SuperByteBuffer superBuffer = top.renderOn(te.p());

		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

		if (facing.getAxis()
			.isHorizontal())
			superBuffer.rotateCentered(Direction.UP,
				AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));
		superBuffer.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(te.p(), te.p()
			.c(BearingBlock.FACING)
			.getOpposite());
	}

}
