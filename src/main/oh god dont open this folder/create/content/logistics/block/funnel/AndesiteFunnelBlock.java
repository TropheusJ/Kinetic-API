package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlocks;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerLogic;

public class AndesiteFunnelBlock extends FunnelBlock {

	public AndesiteFunnelBlock(c p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public PistonHandler getEquivalentBeltFunnel(MobSpawnerLogic world, BlockPos pos, PistonHandler state) {
		Direction facing = state.c(SHAPE);
		return AllBlocks.ANDESITE_BELT_FUNNEL.getDefaultState()
			.a(BeltFunnelBlock.aq, facing)
			.a(POWERED, state.c(POWERED));
	}

}
