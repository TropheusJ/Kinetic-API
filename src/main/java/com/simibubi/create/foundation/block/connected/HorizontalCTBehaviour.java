package com.simibubi.create.foundation.block.connected;

import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;

public class HorizontalCTBehaviour extends ConnectedTextureBehaviour {

	CTSpriteShiftEntry topShift;
	CTSpriteShiftEntry layerShift;

	public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift) {
		this(layerShift, null);
	}

	public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		this.layerShift = layerShift;
		this.topShift = topShift;
	}

	@Override
	public CTSpriteShiftEntry get(PistonHandler state, Direction direction) {
		return direction.getAxis()
			.isHorizontal() ? layerShift : topShift;
	}

}