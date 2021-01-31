package com.simibubi.kinetic_api.content.logistics.block.diodes;

import java.util.Random;

import com.simibubi.kinetic_api.AllItems;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class ToggleLatchBlock extends AbstractDiodeBlock {

	public static BedPart POWERING = BedPart.a("powering");

	public ToggleLatchBlock(c properties) {
		super(properties);
		j(n().a(POWERING, false)
			.a(SHAPE, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(SHAPE, POWERING, aq);
	}

	@Override
	public int a(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		return blockState.c(aq) == side ? this.b(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int g(PistonHandler state) {
		return 1;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (!player.eJ())
			return Difficulty.PASS;
		if (player.bt())
			return Difficulty.PASS;
		if (AllItems.WRENCH.isIn(player.b(handIn)))
			return Difficulty.PASS;
		return activated(worldIn, pos, state);
	}

	@Override
	protected int b(MobSpawnerLogic worldIn, BlockPos pos, PistonHandler state) {
		return state.c(POWERING) ? 15 : 0;
	}

	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean poweredPreviously = state.c(SHAPE);
		super.a(state, worldIn, pos, random);
		PistonHandler newState = worldIn.d_(pos);
		if (newState.c(SHAPE) && !poweredPreviously)
			worldIn.a(pos, newState.a(POWERING), 2);
	}

	protected Difficulty activated(GameMode worldIn, BlockPos pos, PistonHandler state) {
		if (!worldIn.v)
			worldIn.a(pos, state.a(POWERING), 2);
		return Difficulty.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.c(aq)
			.getAxis();
	}

}
