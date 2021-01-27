package com.simibubi.create.content.palettes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConnectedGlassPaneBlock extends GlassPaneBlock {

	public ConnectedGlassPaneBlock(c builder) {
		super(builder);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean a(PistonHandler state, PistonHandler adjacentBlockState, Direction side) {
		if (side.getAxis()
			.isVertical())
			return adjacentBlockState == state;
		return super.a(state, adjacentBlockState, side);
	}

}
