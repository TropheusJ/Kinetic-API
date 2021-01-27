package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.util.math.Direction.Axis;

public class PulleyRenderer extends AbstractPulleyRenderer {

	public PulleyRenderer(ebv dispatcher) {
		super(dispatcher, AllBlockPartials.ROPE_HALF, AllBlockPartials.ROPE_HALF_MAGNET);
	}

	@Override
	protected Axis getShaftAxis(KineticTileEntity te) {
		return te.p()
			.c(PulleyBlock.HORIZONTAL_AXIS);
	}

	@Override
	protected AllBlockPartials getCoil() {
		return AllBlockPartials.ROPE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticTileEntity te) {
		return CreateClient.bufferCache.renderBlock(AllBlocks.ROPE.getDefaultState());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticTileEntity te) {
		return CreateClient.bufferCache.renderBlock(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	@Override
	protected float getOffset(KineticTileEntity te, float partialTicks) {
		PulleyTileEntity pulley = (PulleyTileEntity) te;
		boolean running = pulley.running;
		boolean moving = running && (pulley.movedContraption == null || !pulley.movedContraption.isStalled());
		float offset = pulley.getInterpolatedOffset(moving ? partialTicks : 0.5f);

		if (pulley.movedContraption != null) {
			AbstractContraptionEntity e = pulley.movedContraption;
			PulleyContraption c = (PulleyContraption) pulley.movedContraption.getContraption();
			double entityPos = afj.d(partialTicks, e.E, e.cD());
			offset = (float) -(entityPos - c.anchor.getY() - c.initialOffset);
		}

		return offset;
	}

	@Override
	protected boolean isRunning(KineticTileEntity te) {
		return ((PulleyTileEntity) te).running;
	}

}
