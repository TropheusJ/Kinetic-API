package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicates;
import com.simibubi.create.AllTags;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.KelpPlantBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerLogic;

public class TreeCutter {

	public static class Tree {
		public List<BlockPos> logs;
		public List<BlockPos> leaves;

		public Tree(List<BlockPos> logs, List<BlockPos> leaves) {
			this.logs = logs;
			this.leaves = leaves;
		}
	}

	/**
	 * Finds a tree at the given pos. Block at the position should be air
	 * 
	 * @param reader
	 * @param pos
	 * @return null if not found or not fully cut
	 */
	public static Tree cutTree(MobSpawnerLogic reader, BlockPos pos) {
		List<BlockPos> logs = new ArrayList<>();
		List<BlockPos> leaves = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		// Bamboo, Sugar Cane, Cactus
		PistonHandler stateAbove = reader.d_(pos.up());
		if (isVerticalPlant(stateAbove)) {
			logs.add(pos.up());
			for (int i = 1; i < 256; i++) {
				BlockPos current = pos.up(i);
				if (!isVerticalPlant(reader.d_(current)))
					break;
				logs.add(current);
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves);
		}

		// Chorus
		if (isChorus(stateAbove)) {
			frontier.add(pos.up());
			while (!frontier.isEmpty()) {
				BlockPos current = frontier.remove(0);
				visited.add(current);
				logs.add(current);
				for (Direction direction : Iterate.directions) {
					BlockPos offset = current.offset(direction);
					if (visited.contains(offset))
						continue;
					if (!isChorus(reader.d_(offset)))
						continue;
					frontier.add(offset);
				}
			}
			Collections.reverse(logs);
			return new Tree(logs, leaves);
		}

		// Regular Tree
		if (!validateCut(reader, pos))
			return null;

		visited.add(pos);
		BlockPos.stream(pos.add(-1, 0, -1), pos.add(1, 1, 1))
			.forEach(p -> frontier.add(new BlockPos(p)));

		// Find all logs
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			if (!isLog(reader.d_(currentPos)))
				continue;
			logs.add(currentPos);
			addNeighbours(currentPos, frontier, visited);
		}

		// Find all leaves
		visited.clear();
		visited.addAll(logs);
		frontier.addAll(logs);
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (!logs.contains(currentPos))
				if (visited.contains(currentPos))
					continue;
			visited.add(currentPos);

			PistonHandler blockState = reader.d_(currentPos);
			boolean isLog = isLog(blockState);
			boolean isLeaf = isLeaf(blockState);
			boolean isGenericLeaf = isLeaf || isNonDecayingLeaf(blockState);

			if (!isLog && !isGenericLeaf)
				continue;
			if (isGenericLeaf)
				leaves.add(currentPos);

			int distance = !isLeaf ? 0 : blockState.c(KelpPlantBlock.a);
			for (Direction direction : Iterate.directions) {
				BlockPos offset = currentPos.offset(direction);
				if (visited.contains(offset))
					continue;
				PistonHandler state = reader.d_(offset);
				BlockPos subtract = offset.subtract(pos);
				int horizontalDistance = Math.max(Math.abs(subtract.getX()), Math.abs(subtract.getZ()));
				if (isLeaf(state) && state.c(KelpPlantBlock.a) > distance || isNonDecayingLeaf(state) && horizontalDistance < 4)
					frontier.add(offset);
			}

		}

		return new Tree(logs, leaves);
	}

	public static boolean isChorus(PistonHandler stateAbove) {
		return stateAbove.b() instanceof ChainBlock || stateAbove.b() instanceof CauldronBlock;
	}

	public static boolean isVerticalPlant(PistonHandler stateAbove) {
		BeetrootsBlock block = stateAbove.b();
		if (block instanceof AirBlock)
			return true;
		if (block instanceof FluidDrainable)
			return true;
		if (block instanceof StonecutterBlock)
			return true;
		if (block instanceof JigsawBlock)
			return true;
		if (block instanceof PaneBlock)
			return true;
		return false;
	}

	/**
	 * Checks whether a tree was fully cut by seeing whether the layer above the cut
	 * is not supported by any more logs.
	 * 
	 * @param reader
	 * @param pos
	 * @return
	 */
	private static boolean validateCut(MobSpawnerLogic reader, BlockPos pos) {
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();
		frontier.add(pos);
		frontier.add(pos.up());
		int posY = pos.getY();

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			visited.add(currentPos);
			boolean lowerLayer = currentPos.getY() == posY;

			if (!isLog(reader.d_(currentPos)))
				continue;
			if (!lowerLayer && !pos.equals(currentPos.down()) && isLog(reader.d_(currentPos.down())))
				return false;

			for (Direction direction : Iterate.directions) {
				if (direction == Direction.DOWN)
					continue;
				if (direction == Direction.UP && !lowerLayer)
					continue;
				BlockPos offset = currentPos.offset(direction);
				if (visited.contains(offset))
					continue;
				frontier.add(offset);
			}

		}

		return true;
	}

	private static void addNeighbours(BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited) {
		BlockPos.stream(pos.add(-1, -1, -1), pos.add(1, 1, 1))
			.filter(Predicates.not(visited::contains))
			.forEach(p -> frontier.add(new BlockPos(p)));
	}

	private static boolean isLog(PistonHandler state) {
		return state.a(StatHandler.s) || AllTags.AllBlockTags.SLIMY_LOGS.matches(state);
	}

	private static boolean isNonDecayingLeaf(PistonHandler state) {
		return state.a(StatHandler.ap) || state.b() == BellBlock.mw;
	}

	private static boolean isLeaf(PistonHandler state) {
		return BlockHelper.hasBlockStateProperty(state, KelpPlantBlock.a);
	}

}
