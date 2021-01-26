package com.simibubi.create.content.contraptions.components.millstone;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;

public class MillstoneRenderer extends KineticTileEntityRenderer {

	public MillstoneRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CreateClient.bufferCache.renderPartial(AllBlockPartials.MILLSTONE_COG, te.p());
	}
	
}
