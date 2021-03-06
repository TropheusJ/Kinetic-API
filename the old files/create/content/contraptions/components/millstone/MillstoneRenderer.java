package com.simibubi.create.content.contraptions.components.millstone;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;

public class MillstoneRenderer extends KineticTileEntityRenderer {

	public MillstoneRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CreateClient.bufferCache.renderPartial(AllBlockPartials.MILLSTONE_COG, te.getCachedState());
	}
	
}
