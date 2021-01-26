package com.simibubi.create.content.contraptions.fluids.pipes;

import javax.annotation.Nullable;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction.Axis;

public interface IAxisPipe {

	@Nullable
	public static Axis getAxisOf(PistonHandler state) {
		if (state.b() instanceof IAxisPipe) 
			return ((IAxisPipe) state.b()).getAxis(state);
		return null;
	}

	public Axis getAxis(PistonHandler state);

}
