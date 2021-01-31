package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.foundation.fluid.FluidRenderer;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.fluids.FluidStack;

public class SpoutRenderer extends SafeTileEntityRenderer<SpoutTileEntity> {

	public SpoutRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	static final AllBlockPartials[] BITS =
		{ AllBlockPartials.SPOUT_TOP, AllBlockPartials.SPOUT_MIDDLE, AllBlockPartials.SPOUT_BOTTOM };

	@Override
	protected void renderSafe(SpoutTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {

		SmartFluidTankBehaviour tank = te.tank;
		if (tank == null)
			return;

		TankSegment primaryTank = tank.getPrimaryTank();
		FluidStack fluidStack = primaryTank.getRenderedFluid();
		float level = primaryTank.getFluidLevel()
			.getValue(partialTicks);

		if (!fluidStack.isEmpty() && level != 0) {
			float min = 2.5f / 16f;
			float max = min + (11 / 16f);
			float yOffset = (11 / 16f) * level;
			ms.a();
			ms.a(0, yOffset, 0);
			FluidRenderer.renderTiledFluidBB(fluidStack, min, min - yOffset, min, max, min, max, buffer, ms, light,
				false);
			ms.b();
		}

		int processingTicks = te.getCorrectedProcessingTicks();
		float processingPT = te.getCorrectedProcessingTicks() - partialTicks;
		float processingProgress = 1 - (processingPT - 5) / 10;
		processingProgress = afj.a(processingProgress, 0, 1);
		float radius = 0;

		if (processingTicks != -1) {
			radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
			Timer bb = new Timer(0.5, .5, 0.5, 0.5, -1.2, 0.5).g(radius / 32f);
			FluidRenderer.renderTiledFluidBB(fluidStack, (float) bb.LOGGER, (float) bb.callback, (float) bb.events,
				(float) bb.eventCounter, (float) bb.eventsByName, (float) bb.f, buffer, ms, light, true);
		}

		float squeeze = radius;
		if (processingPT < 0)
			squeeze = 0;
		else if (processingPT < 2)
			squeeze = afj.g(processingPT / 2f, 0, -1);
		else if (processingPT < 10)
			squeeze = -1;

		ms.a();
		for (AllBlockPartials bit : BITS) {
			bit.renderOn(te.p())
				.light(light)
				.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
			ms.a(0, -3 * squeeze / 32f, 0);
		}
		ms.b();

	}

}
