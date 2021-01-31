package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import com.simibubi.kinetic_api.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.kinetic_api.content.contraptions.fluids.PipeConnection.Flow;
import com.simibubi.kinetic_api.foundation.fluid.FluidRenderer;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.LerpedFloat;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.math.Direction;
import net.minecraftforge.fluids.FluidStack;

public class TransparentStraightPipeRenderer extends SafeTileEntityRenderer<StraightPipeTileEntity> {

	public TransparentStraightPipeRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(StraightPipeTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		FluidTransportBehaviour pipe = te.getBehaviour(FluidTransportBehaviour.TYPE);
		if (pipe == null)
			return;

		for (Direction side : Iterate.directions) {

			Flow flow = pipe.getFlow(side);
			if (flow == null)
				continue;
			FluidStack fluidStack = flow.fluid;
			if (fluidStack.isEmpty())
				continue;
			LerpedFloat progress = flow.progress;
			if (progress == null)
				continue;

			float value = progress.getValue(partialTicks);
			boolean inbound = flow.inbound;
			if (value == 1) {
				if (inbound) {
					Flow opposite = pipe.getFlow(side.getOpposite());
					if (opposite == null)
						value -= 1e-6f;
				} else {
					FluidTransportBehaviour adjacent = TileEntityBehaviour.get(te.v(), te.o()
						.offset(side), FluidTransportBehaviour.TYPE);
					if (adjacent == null)
						value -= 1e-6f;
					else {
						Flow other = adjacent.getFlow(side.getOpposite());
						if (other == null || !other.inbound && !other.complete)
							value -= 1e-6f;
					}
				}
			}

			FluidRenderer.renderFluidStream(fluidStack, side, 3 / 16f, value, inbound, buffer, ms, light);
		}

	}

}
