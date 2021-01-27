package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class PlacementSimulationWorld extends WrappedWorld {
	public HashMap<BlockPos, PistonHandler> blocksAdded;
	public HashMap<BlockPos, BeehiveBlockEntity> tesAdded;

	public PlacementSimulationWorld(GameMode wrapped) {
		super(wrapped);
		blocksAdded = new HashMap<>();
		tesAdded = new HashMap<>();
	}

	public void setTileEntities(Collection<BeehiveBlockEntity> tileEntities) {
		tesAdded.clear();
		tileEntities.forEach(te -> tesAdded.put(te.o(), te));
	}

	public void clear() {
		blocksAdded.clear();
	}

	@Override
	public boolean a(BlockPos pos, PistonHandler newState, int flags) {
		blocksAdded.put(pos, newState);
		return true;
	}

	@Override
	public boolean a(BlockPos pos, PistonHandler state) {
		return a(pos, state, 0);
	}

	@Override
	public BeehiveBlockEntity c(BlockPos pos) {
		return tesAdded.get(pos);
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
