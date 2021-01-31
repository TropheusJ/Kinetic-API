package com.simibubi.kinetic_api.foundation.utility.worldWrappers;

import java.util.HashMap;
import java.util.function.Predicate;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PlacementSimulationServerWorld extends WrappedServerWorld {
	public HashMap<BlockPos, PistonHandler> blocksAdded;

	public PlacementSimulationServerWorld(ServerWorld wrapped) {
		super(wrapped);
		blocksAdded = new HashMap<>();
	}
	
	public void clear() {
		blocksAdded.clear();
	}

	@Override
	public boolean a(BlockPos pos, PistonHandler newState, int flags) {
		blocksAdded.put(pos.toImmutable(), newState);
		return true;
	}

	@Override
	public boolean a(BlockPos pos, PistonHandler state) {
		return a(pos, state, 0);
	}

	@Override
	public boolean a(BlockPos pos, Predicate<PistonHandler> condition) {
		return condition.test(d_(pos));
	}
	
	@Override
	public boolean p(BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	@Override
	public PistonHandler d_(BlockPos pos) {
		if (blocksAdded.containsKey(pos))
			return blocksAdded.get(pos);
		return BellBlock.FACING.n();
	}
	
}
