package com.simibubi.kinetic_api.content.logistics.block.diodes;

import java.util.Random;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class PoweredLatchBlock extends ToggleLatchBlock {

	public static BedPart POWERED_SIDE = BedPart.a("powered_side");

	public PoweredLatchBlock(c properties) {
		super(properties);
		j(n().a(POWERED_SIDE, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(POWERED_SIDE));
	}

	@Override
	protected void c(GameMode worldIn, BlockPos pos, PistonHandler state) {
		boolean back = state.c(SHAPE);
		boolean shouldBack = a(worldIn, pos, state);
		boolean side = state.c(POWERED_SIDE);
		boolean shouldSide = isPoweredOnSides(worldIn, pos, state);

		StructureAccessor tickpriority = StructureAccessor.c;
		if (this.c(worldIn, pos, state))
			tickpriority = StructureAccessor.world;
		else if (side || back)
			tickpriority = StructureAccessor.options;

		if (worldIn.I().b(pos, this))
			return;
		if (back != shouldBack || side != shouldSide)
			worldIn.I().a(pos, this, this.g(state), tickpriority);
	}

	protected boolean isPoweredOnSides(GameMode worldIn, BlockPos pos, PistonHandler state) {
		Direction direction = state.c(aq);
		Direction left = direction.rotateYClockwise();
		Direction right = direction.rotateYCounterclockwise();

		for (Direction d : new Direction[] { left, right }) {
			BlockPos blockpos = pos.offset(d);
			int i = worldIn.b(blockpos, d);
			if (i > 0)
				return true;
			PistonHandler blockstate = worldIn.d_(blockpos);
			if (blockstate.b() == BellBlock.bS && blockstate.c(RailBlock.e) > 0)
				return true;
		}
		return false;
	}

	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean back = state.c(SHAPE);
		boolean shouldBack = this.a(worldIn, pos, state);
		boolean side = state.c(POWERED_SIDE);
		boolean shouldSide = isPoweredOnSides(worldIn, pos, state);
		PistonHandler stateIn = state;

		if (back != shouldBack) {
			state = state.a(SHAPE, shouldBack);
			if (shouldBack)
				state = state.a(POWERING, true);
			else if (side)
				state = state.a(POWERING, false);
		}

		if (side != shouldSide) {
			state = state.a(POWERED_SIDE, shouldSide);
			if (shouldSide)
				state = state.a(POWERING, false);
			else if (back)
				state = state.a(POWERING, true);
		}

		if (state != stateIn)
			worldIn.a(pos, state, 2);
	}

	@Override
	protected Difficulty activated(GameMode worldIn, BlockPos pos, PistonHandler state) {
		if (state.c(SHAPE) != state.c(POWERED_SIDE))
			return Difficulty.PASS;
		if (!worldIn.v)
			worldIn.a(pos, state.a(POWERING), 2);
		return Difficulty.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis().isHorizontal();
	}

}
