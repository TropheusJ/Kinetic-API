package com.simibubi.kinetic_api.content.logistics.block.funnel;

import com.simibubi.kinetic_api.AllBlocks;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerLogic;

public class BrassFunnelBlock extends FunnelBlock {

	public BrassFunnelBlock(c p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public PistonHandler getEquivalentBeltFunnel(MobSpawnerLogic world, BlockPos pos, PistonHandler state) {
		Direction facing = state.c(SHAPE);
		return AllBlocks.BRASS_BELT_FUNNEL.getDefaultState()
			.a(BeltFunnelBlock.aq, facing)
			.a(POWERED, state.c(POWERED));
	}

}
