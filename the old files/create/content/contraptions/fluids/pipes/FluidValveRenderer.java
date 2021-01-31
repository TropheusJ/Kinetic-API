package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class FluidValveRenderer extends KineticTileEntityRenderer {

	public FluidValveRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		PistonHandler blockState = te.p();
		SuperByteBuffer pointer = AllBlockPartials.FLUID_VALVE_POINTER.renderOn(blockState);
		Direction facing = blockState.c(FluidValveBlock.FACING);

		if (!(te instanceof FluidValveTileEntity))
			return;
		FluidValveTileEntity valve = (FluidValveTileEntity) te;
		float pointerRotation = afj.g(valve.pointer.getValue(partialTicks), 0, -90);
		Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
		Axis shaftAxis = KineticTileEntityRenderer.getRotationAxisOf(te);

		int pointerRotationOffset = 0;
		if (pipeAxis.isHorizontal() && shaftAxis == Axis.Z || pipeAxis.isVertical())
			pointerRotationOffset = 90;

		MatrixStacker.of(ms)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
			.rotateY(pointerRotationOffset + pointerRotation)
			.unCentre();

		pointer.light(light)
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}

	@Override
	protected PistonHandler getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

}
