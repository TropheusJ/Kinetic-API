package com.simibubi.create.foundation.block.connected;

import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;

public class StandardCTBehaviour extends ConnectedTextureBehaviour {

	CTSpriteShiftEntry shift;

	public StandardCTBehaviour(CTSpriteShiftEntry shift) {
		this.shift = shift;
	}

	@Override
	public CTSpriteShiftEntry get(PistonHandler state, Direction direction) {
		return shift;
	}

}
