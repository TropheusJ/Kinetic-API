package com.simibubi.create.foundation.block.connected;

import bqx;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;

public class GlassPaneCTBehaviour extends StandardCTBehaviour {

	public GlassPaneCTBehaviour(CTSpriteShiftEntry shift) {
		super(shift);
	}
	
	@Override
	public boolean buildContextForOccludedDirections() {
		return true;
	}

	@Override
	public boolean connectsTo(PistonHandler state, PistonHandler other, bqx reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		return state.b() == other.b();
	}

	@Override
	protected boolean reverseUVsHorizontally(PistonHandler state, net.minecraft.util.math.Direction face) {
		if (face.getDirection() == AxisDirection.NEGATIVE)
			return true;
		return super.reverseUVsHorizontally(state, face);
	}
}
