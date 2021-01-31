package com.simibubi.kinetic_api.content.palettes;

import bqx;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConnectedGlassBlock extends FrostedIceBlock {

	public ConnectedGlassBlock(c p_i48392_1_) {
		super(p_i48392_1_);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean a(PistonHandler state, PistonHandler adjacentBlockState, Direction side) {
		return adjacentBlockState.b() instanceof ConnectedGlassBlock ? true
			: super.a(state, adjacentBlockState, side);
	}

	@Override
	public boolean shouldDisplayFluidOverlay(PistonHandler state, bqx world, BlockPos pos, EmptyFluid fluidState) {
		return true;
	}
}
