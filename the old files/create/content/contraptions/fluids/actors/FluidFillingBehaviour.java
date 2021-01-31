package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.FluidFillingBehaviour.SpaceType;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import cqx;
import cut;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.chunk.ChunkCache;

public class FluidFillingBehaviour extends FluidManipulationBehaviour {

	PriorityQueue<BlockPosEntry> queue;

	List<BlockPosEntry> infinityCheckFrontier;
	Set<BlockPos> infinityCheckVisited;

	public FluidFillingBehaviour(SmartTileEntity te) {
		super(te);
		queue = new ObjectHeapPriorityQueue<>((p, p2) -> -comparePositions(p, p2));
		revalidateIn = 1;
		infinityCheckFrontier = new ArrayList<>();
		infinityCheckVisited = new HashSet<>();
	}

	@Override
	public void tick() {
		super.tick();
		if (!infinityCheckFrontier.isEmpty() && rootPos != null) {
			cut fluid = getWorld().b(rootPos)
				.a();
			if (fluid != FlowableFluid.FALLING)
				continueValidation(fluid);
		}
		if (revalidateIn > 0)
			revalidateIn--;
	}

	protected void continueValidation(cut fluid) {
		search(fluid, infinityCheckFrontier, infinityCheckVisited,
			(p, d) -> infinityCheckFrontier.add(new BlockPosEntry(p, d)), true);
		int maxBlocks = maxBlocks();

		if (infinityCheckVisited.size() > maxBlocks && maxBlocks != -1) {
			if (!infinite) {
				reset();
				infinite = true;
				tileEntity.sendData();
			}
			infinityCheckFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!infinityCheckFrontier.isEmpty())
			return;
		if (infinite) {
			reset();
			return;
		}

		infinityCheckVisited.clear();
	}

	public boolean tryDeposit(cut fluid, BlockPos root, boolean simulate) {
		if (!Objects.equals(root, rootPos)) {
			reset();
			rootPos = root;
			queue.enqueue(new BlockPosEntry(root, 0));
			affectedArea = new cqx(rootPos, rootPos);
			return false;
		}

		if (counterpartActed) {
			counterpartActed = false;
			softReset(root);
			return false;
		}

		if (affectedArea == null)
			affectedArea = new cqx(root, root);

		if (revalidateIn == 0) {
			visited.clear();
			infinityCheckFrontier.clear();
			infinityCheckVisited.clear();
			infinityCheckFrontier.add(new BlockPosEntry(root, 0));
			setValidationTimer();
			softReset(root);
		}

		GameMode world = getWorld();
		int maxRange = maxRange();
		int maxRangeSq = maxRange * maxRange;
		int maxBlocks = maxBlocks();
		boolean evaporate = world.k()
			.d() && fluid.a(BlockTags.field_15481);

		if (infinite || evaporate) {
			EmptyFluid fluidState = world.b(rootPos);
			boolean equivalentTo = fluidState.a()
				.a(fluid);
			if (!equivalentTo && !evaporate)
				return false;
			if (simulate)
				return true;
			playEffect(world, null, fluid, false);
			if (evaporate) {
				int i = root.getX();
				int j = root.getY();
				int k = root.getZ();
				world.a(null, i, j, k, MusicType.ej, SoundEvent.e, 0.5F,
					2.6F + (world.t.nextFloat() - world.t.nextFloat()) * 0.8F);
			}
			return true;
		}

		boolean success = false;
		for (int i = 0; !success && !queue.isEmpty() && i < searchedPerTick; i++) {
			BlockPosEntry entry = queue.first();
			BlockPos currentPos = entry.pos;

			if (visited.contains(currentPos)) {
				queue.dequeue();
				continue;
			}

			if (!simulate)
				visited.add(currentPos);

			if (visited.size() >= maxBlocks && maxBlocks != -1) {
				infinite = true;
				visited.clear();
				queue.clear();
				return false;
			}

			SpaceType spaceType = getAtPos(world, currentPos, fluid);
			if (spaceType == SpaceType.BLOCKING)
				continue;
			if (spaceType == SpaceType.FILLABLE) {
				success = true;
				if (!simulate) {
					playEffect(world, currentPos, fluid, false);

					PistonHandler blockState = world.d_(currentPos);
					if (blockState.b(BambooLeaves.C) && fluid.a(FlowableFluid.c)) {
						world.a(currentPos,
							updatePostWaterlogging(blockState.a(BambooLeaves.C, true)), 2 | 16);
					} else {
						replaceBlock(world, currentPos, blockState);
						world.a(currentPos, FluidHelper.convertToStill(fluid)
							.h()
							.g(), 2 | 16);
					}

					ServerTickScheduler<cut> pendingFluidTicks = world.H();
					if (pendingFluidTicks instanceof ChunkCache) {
						ChunkCache<cut> serverTickList = (ChunkCache<cut>) pendingFluidTicks;
						MobSpawnerEntry<cut> removedEntry = null;
						for (MobSpawnerEntry<cut> nextTickListEntry : serverTickList.chunks) {
							if (nextTickListEntry.a.equals(currentPos)) {
								removedEntry = nextTickListEntry;
								break;
							}
						}
						if (removedEntry != null) {
							serverTickList.chunks.remove(removedEntry);
							serverTickList.empty.remove(removedEntry);
						}
					}

					affectedArea.c(new cqx(currentPos, currentPos));
				}
			}

			if (simulate && success)
				return true;

			visited.add(currentPos);
			queue.dequeue();

			for (Direction side : Iterate.directions) {
				if (side == Direction.UP)
					continue;

				BlockPos offsetPos = currentPos.offset(side);
				if (visited.contains(offsetPos))
					continue;
				if (offsetPos.getSquaredDistance(rootPos) > maxRangeSq)
					continue;

				SpaceType nextSpaceType = getAtPos(world, offsetPos, fluid);
				if (nextSpaceType != SpaceType.BLOCKING)
					queue.enqueue(new BlockPosEntry(offsetPos, entry.distance + 1));
			}
		}

		if (!simulate && success)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.HOSE_PULLEY, world, tileEntity.o(), 8);
		return success;
	}

	protected void softReset(BlockPos root) {
		visited.clear();
		queue.clear();
		queue.enqueue(new BlockPosEntry(root, 0));
		infinite = false;
		setValidationTimer();
		tileEntity.sendData();
	}

	enum SpaceType {
		FILLABLE, FILLED, BLOCKING
	}

	protected SpaceType getAtPos(GameMode world, BlockPos pos, cut toFill) {
		PistonHandler blockState = world.d_(pos);
		EmptyFluid fluidState = blockState.m();

		if (blockState.b(BambooLeaves.C))
			return toFill.a(FlowableFluid.c)
				? blockState.c(BambooLeaves.C) ? SpaceType.FILLED : SpaceType.FILLABLE
				: SpaceType.BLOCKING;

		if (blockState.b() instanceof LecternBlock)
			return blockState.c(LecternBlock.FACING) == 0
				? toFill.a(fluidState.a()) ? SpaceType.FILLED : SpaceType.BLOCKING
				: SpaceType.FILLABLE;

		if (fluidState.a() != FlowableFluid.FALLING
			&& blockState.b(getWorld(), pos, ArrayVoxelShape.a())
				.b())
			return toFill.a(fluidState.a()) ? SpaceType.FILLED : SpaceType.BLOCKING;

		return canBeReplacedByFluid(world, pos, blockState) ? SpaceType.FILLABLE : SpaceType.BLOCKING;
	}

	protected void replaceBlock(GameMode world, BlockPos pos, PistonHandler state) {
		BeehiveBlockEntity tileentity = state.b()
			.hasTileEntity(state) ? world.c(pos) : null;
		BeetrootsBlock.a(state, world, pos, tileentity);
	}

	// From FlowingFluidBlock#isBlocked
	protected boolean canBeReplacedByFluid(MobSpawnerLogic world, BlockPos pos, PistonHandler state) {
		BeetrootsBlock block = state.b();
		if (!(block instanceof AbstractRedstoneGateBlock) && !block.a(StatHandler.af) && block != BellBlock.cg
			&& block != BellBlock.cH && block != BellBlock.lc) {
			FluidState material = state.c();
			if (material != FluidState.c && material != FluidState.b && material != FluidState.f
				&& material != FluidState.i) {
				return !material.isEmpty();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected PistonHandler updatePostWaterlogging(PistonHandler state) {
		if (state.b(BambooLeaves.r))
			state = state.a(BambooLeaves.r, false);
		return state;
	}

	@Override
	public void reset() {
		super.reset();
		queue.clear();
		infinityCheckFrontier.clear();
		infinityCheckVisited.clear();
	}

	public static BehaviourType<FluidFillingBehaviour> TYPE = new BehaviourType<>();

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
