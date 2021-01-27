package com.simibubi.create.content.contraptions.processing.burner;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class BlazeBurnerRenderer extends SafeTileEntityRenderer<BlazeBurnerTileEntity> {

	public BlazeBurnerRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BlazeBurnerTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		HeatLevel heatLevel = te.getHeatLevelFromBlock();
		if (heatLevel == HeatLevel.NONE)
			return;

		float renderTick = AnimationTickHolder.getRenderTick() + (te.hashCode() % 13) * 16f;
		float offset = (afj.a((float) ((renderTick / 16f) % (2 * Math.PI))) + .5f) / 16f;

		AllBlockPartials blazeModel = AllBlockPartials.BLAZES.get(heatLevel);
		SuperByteBuffer blazeBuffer = blazeModel.renderOn(te.p());
		blazeBuffer.rotateCentered(Direction.UP, AngleHelper.rad(te.headAngle.getValue(partialTicks)));
		blazeBuffer.translate(0, offset, 0);
		blazeBuffer.light(0xF000F0)
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}
}
