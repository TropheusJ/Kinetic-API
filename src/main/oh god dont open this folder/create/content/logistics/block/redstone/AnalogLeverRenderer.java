package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.util.math.Direction;

public class AnalogLeverRenderer extends SafeTileEntityRenderer<AnalogLeverTileEntity> {

	public AnalogLeverRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(AnalogLeverTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		PistonHandler leverState = te.p();
		int lightCoords = JsonGlProgram.a(te.v(), leverState, te.o());
		float state = te.clientState.get(partialTicks);

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());

		// Handle
		SuperByteBuffer handle = AllBlockPartials.ANALOG_LEVER_HANDLE.renderOn(leverState);
		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);
		transform(handle, leverState).translate(1 / 2f, 1 / 16f, 1 / 2f)
			.rotate(Direction.EAST, angle)
			.translate(-1 / 2f, -1 / 16f, -1 / 2f);
		handle.light(lightCoords)
			.renderInto(ms, vb);

		// Indicator
		int color = ColorHelper.mixColors(0x2C0300, 0xCD0000, state / 15f);
		SuperByteBuffer indicator = transform(AllBlockPartials.ANALOG_LEVER_INDICATOR.renderOn(leverState), leverState);
		indicator.light(lightCoords)
			.color(color)
			.renderInto(ms, vb);
	}

	private SuperByteBuffer transform(SuperByteBuffer buffer, PistonHandler leverState) {
		BlockPredicate face = leverState.c(AnalogLeverBlock.u);
		float rX = face == BlockPredicate.block ? 0 : face == BlockPredicate.b ? 90 : 180;
		float rY = AngleHelper.horizontalAngle(leverState.c(AnalogLeverBlock.aq));
		buffer.rotateCentered(Direction.UP, (float) (rY / 180 * Math.PI));
		buffer.rotateCentered(Direction.EAST, (float) (rX / 180 * Math.PI));
		return buffer;
	}

}
