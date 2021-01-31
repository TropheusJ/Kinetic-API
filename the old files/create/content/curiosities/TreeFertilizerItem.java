package com.simibubi.kinetic_api.content.curiosities;

import bnx;
import com.simibubi.kinetic_api.foundation.utility.worldWrappers.PlacementSimulationServerWorld;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.BedItem;
import net.minecraft.item.HoeItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;

public class TreeFertilizerItem extends HoeItem {

	public TreeFertilizerItem(a properties) {
		super(properties);
	}

	@Override
	public Difficulty a(bnx context) {
		PistonHandler state = context.p()
			.d_(context.a());
		BeetrootsBlock block = state.b();
		if (block instanceof PillarBlock) {

			if (context.p().v) {
				BedItem.a(context.p(), context.a(), 100);
				return Difficulty.SUCCESS;
			}

			TreesDreamWorld world = new TreesDreamWorld((ServerWorld) context.p(), context.a());
			BlockPos saplingPos = context.a();

			for (BlockPos pos : BlockPos.iterate(-1, 0, -1, 1, 0, 1)) {
				if (context.p()
					.d_(saplingPos.add(pos))
					.b() == block)
					world.a(pos.up(10), state.a(PillarBlock.a, 1));
			}

			((PillarBlock) block).a(world, world.getRandom(), BlockPos.ORIGIN.up(10),
				state.a(PillarBlock.a, 1));

			for (BlockPos pos : world.blocksAdded.keySet()) {
				BlockPos actualPos = pos.add(saplingPos)
					.down(10);

				// Don't replace Bedrock
				if (context.p()
					.d_(actualPos)
					.h(context.p(), actualPos) == -1)
					continue;
				// Don't replace solid blocks with leaves
				if (!world.d_(pos)
					.g(world, pos)
					&& !context.p()
						.d_(actualPos)
						.k(context.p(), actualPos)
						.b())
					continue;
				if (world.d_(pos)
					.b() == BellBlock.NORTH_SOUTH_WALLS_SHAPE
					|| world.d_(pos)
						.b() == BellBlock.l)
					continue;

				context.p()
					.a(actualPos, world.d_(pos));
			}

			if (context.n() != null && !context.n()
				.b_())
				context.m()
					.g(1);
			return Difficulty.SUCCESS;

		}

		return super.a(context);
	}

	private class TreesDreamWorld extends PlacementSimulationServerWorld {
		private final BlockPos saplingPos;

		protected TreesDreamWorld(ServerWorld wrapped, BlockPos saplingPos) {
			super(wrapped);
			this.saplingPos = saplingPos;
		}

		@Override
		public PistonHandler d_(BlockPos pos) {
			if (pos.getY() <= 9)
				return world.d_(saplingPos.down());
			return super.d_(pos);
		}

	}



}
