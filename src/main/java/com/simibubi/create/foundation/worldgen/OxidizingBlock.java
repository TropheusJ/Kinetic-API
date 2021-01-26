package com.simibubi.create.foundation.worldgen;

import java.util.LinkedList;
import java.util.OptionalDouble;
import java.util.Random;

import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class OxidizingBlock extends BeetrootsBlock {

	public static final DoubleBlockHalf OXIDIZATION = DoubleBlockHalf.of("oxidization", 0, 7);
	private float chance;

	public OxidizingBlock(c properties, float chance) {
		super(properties);
		this.chance = chance;
		j(n().a(OXIDIZATION, 0));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(OXIDIZATION));
	}

	@Override
	public boolean a_(PistonHandler state) {
		return super.a_(state) || state.c(OXIDIZATION) < 7;
	}

	@Override
	public void b(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (worldIn.getRandom().nextFloat() <= chance) {
			int currentState = state.c(OXIDIZATION);
			boolean canIncrease = false;
			LinkedList<Integer> neighbors = new LinkedList<>();
			for (Direction facing : Iterate.directions) {
				BlockPos neighbourPos = pos.offset(facing);
				if (!worldIn.isAreaLoaded(neighbourPos, 0))
					continue;
				if (!worldIn.p(neighbourPos))
					continue;
				PistonHandler neighborState = worldIn.d_(neighbourPos);
				if (BlockHelper.hasBlockStateProperty(neighborState, OXIDIZATION) && neighborState.c(OXIDIZATION) != 0) {
					neighbors.add(neighborState.c(OXIDIZATION));
				}
				if (BlockHelper.hasBlockSolidSide(neighborState, worldIn, neighbourPos, facing.getOpposite())) {
					continue;
				}
				canIncrease = true;
			}
			if (canIncrease) {
				OptionalDouble average = neighbors.stream().mapToInt(v -> v).average();
				if (average.orElse(7d) >= currentState)
					worldIn.a(pos, state.a(OXIDIZATION, Math.min(currentState + 1, 7)));
			}
		}
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos,
			PlayerAbilities player, ItemScatterer hand, dcg blockRayTraceResult) {
		if(state.c(OXIDIZATION) > 0 && player.b(hand).b() instanceof SandPaperItem) {
			if(!player.b_())
				player.b(hand).a(1, player, p -> p.d(p.dW()));
			world.a(pos, state.a(OXIDIZATION, 0));
			return Difficulty.SUCCESS;
		}
		return Difficulty.PASS;
	}
}
