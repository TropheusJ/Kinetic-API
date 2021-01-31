package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston;

import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import ebv;
import net.minecraft.block.piston.PistonHandler;

public class MechanicalPistonRenderer extends KineticTileEntityRenderer {

	public MechanicalPistonRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected PistonHandler getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

}
