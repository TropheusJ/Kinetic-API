package com.simibubi.kinetic_api.content.contraptions.relays.encased;

import com.simibubi.kinetic_api.AllTileEntities;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class ClutchBlock extends GearshiftBlock {

	public ClutchBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CLUTCH.create();
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.v)
			return;

		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos)) {
			worldIn.a(pos, state.a(POWERED), 2 | 16);
			detachKinetics(worldIn, pos, previouslyPowered);
		}
	}

}
