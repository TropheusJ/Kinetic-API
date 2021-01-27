package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.AbstractPulleyRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.util.math.Direction.Axis;

public class HosePulleyRenderer extends AbstractPulleyRenderer {

	public HosePulleyRenderer(ebv dispatcher) {
		super(dispatcher, AllBlockPartials.HOSE_HALF, AllBlockPartials.HOSE_HALF_MAGNET);
	}

	@Override
	protected Axis getShaftAxis(KineticTileEntity te) {
		return te.p()
			.c(HosePulleyBlock.HORIZONTAL_FACING)
			.rotateYClockwise()
			.getAxis();
	}

	@Override
	protected AllBlockPartials getCoil() {
		return AllBlockPartials.HOSE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticTileEntity te) {
		return AllBlockPartials.HOSE.renderOn(te.p());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticTileEntity te) {
		return AllBlockPartials.HOSE_MAGNET.renderOn(te.p());
	}

	@Override
	protected float getOffset(KineticTileEntity te, float partialTicks) {
		return ((HosePulleyTileEntity) te).getInterpolatedOffset(partialTicks);
	}

	@Override
	protected boolean isRunning(KineticTileEntity te) {
		return true;
	}

}
