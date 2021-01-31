package com.simibubi.kinetic_api.content.contraptions.components.millstone;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
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
