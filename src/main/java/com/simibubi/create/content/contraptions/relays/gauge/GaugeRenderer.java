package com.simibubi.create.content.contraptions.relays.gauge;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock.Type;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class GaugeRenderer extends KineticTileEntityRenderer {

	protected GaugeBlock.Type type;

	public static GaugeRenderer speed(ebv dispatcher) {
		return new GaugeRenderer(dispatcher, Type.SPEED);
	}
	
	public static GaugeRenderer stress(ebv dispatcher) {
		return new GaugeRenderer(dispatcher, Type.STRESS);
	}
	
	protected GaugeRenderer(ebv dispatcher, GaugeBlock.Type type) {
		super(dispatcher);
		this.type = type;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		PistonHandler gaugeState = te.p();
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		int lightCoords = JsonGlProgram.a(te.v(), gaugeState, te.o());

		SuperByteBuffer headBuffer =
			(type == Type.SPEED ? AllBlockPartials.GAUGE_HEAD_SPEED : AllBlockPartials.GAUGE_HEAD_STRESS)
				.renderOn(gaugeState);
		SuperByteBuffer dialBuffer = AllBlockPartials.GAUGE_DIAL.renderOn(gaugeState);

		for (Direction facing : Iterate.directions) {
			if (!((GaugeBlock) gaugeState.b()).shouldRenderHeadOnFace(te.v(), te.o(), gaugeState,
				facing))
				continue;

			float dialPivot = 5.75f / 16;
			float progress = afj.g(partialTicks, gaugeTE.prevDialState, gaugeTE.dialState);

			OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
			rotateBufferTowards(dialBuffer, facing).translate(0, dialPivot, dialPivot)
				.rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
				.translate(0, -dialPivot, -dialPivot)
				.light(lightCoords)
				.renderInto(ms, vb);
			rotateBufferTowards(headBuffer, facing).light(lightCoords)
				.renderInto(ms, vb);
		}

	}

	@Override
	protected PistonHandler getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

	protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
		return buffer.rotateCentered(Direction.UP, (float) ((-target.asRotation() - 90) / 180 * Math.PI));
	}

}
