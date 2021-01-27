package com.simibubi.create.content.contraptions.fluids.tank;

import bqx;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FluidTankCTBehaviour extends HorizontalCTBehaviour {

	public FluidTankCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		super(layerShift, topShift);
	}

	public boolean buildContextForOccludedDirections() {
		return true;
	}

	@Override
	public boolean connectsTo(PistonHandler state, PistonHandler other, bqx reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		return state.b() == other.b() && FluidTankConnectivityHandler.isConnected(reader, pos, otherPos);
	}
}
