package com.simibubi.create.foundation.data;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.LavaFluid;

@MethodsReturnNonnullByDefault
public class SharedProperties {
	public static FluidState beltMaterial =
		new FluidState(Fluids.w, false, true, true, true, false, false, LavaFluid.a);

	public static BeetrootsBlock stone() {
		return BellBlock.BELL_LIP_SHAPE;
	}

	public static BeetrootsBlock softMetal() {
		return BellBlock.bE;
	}

	public static BeetrootsBlock wooden() {
		return BellBlock.ac;
	}
}
