package com.simibubi.create.content.contraptions.components.crank;

import static net.minecraft.block.enums.BambooLeaves.M;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class HandCrankRenderer extends KineticTileEntityRenderer {

	public HandCrankRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		PistonHandler state = te.p();
		BeetrootsBlock block = state.b();
		AllBlockPartials renderedHandle = null;
		if (block instanceof HandCrankBlock)
			renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
		if (renderedHandle == null)
			return;

		Direction facing = state.c(M);
		SuperByteBuffer handle = renderedHandle.renderOnDirectionalSouth(state, facing.getOpposite());
		HandCrankTileEntity crank = (HandCrankTileEntity) te;
		kineticRotationTransform(handle, te, facing.getAxis(),
			(crank.independentAngle + partialTicks * crank.chasingVelocity) / 360, light);
		handle.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}

}
