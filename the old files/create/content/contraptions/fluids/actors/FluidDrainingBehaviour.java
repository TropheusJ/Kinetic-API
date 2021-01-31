package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.AllFluids;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.FluidDrainingBehaviour.FluidBlockType;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.BehaviourType;
import cqx;
import cut;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.block.BellBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;

public class FluidDrainingBehaviour extends FluidManipulationBehaviour {

	cut fluid;

	// Execution
	Set<BlockPos> validationSet;
	PriorityQueue<BlockPosEntry> queue;
	boolean isValid;

	// Validation
	List<BlockPosEntry> validationFrontier;
	Set<BlockPos> validationVisited;
	Set<BlockPos> newValidationSet;

	public FluidDrainingBehaviour(SmartTileEntity te) {
		super(te);
		validationVisited = new HashSet<>();
		validationFrontier = new ArrayList<>();
		validationSet = new HashSet<>();
		newValidationSet = new HashSet<>();
		queue = new ObjectHeapPriorityQueue<>(this::comparePositions);
	}

	@Nullable
	public boolean pullNext(BlockPos root, boolean simulate) {
		if (!frontier.isEmpty())
			return false;
		if (!Objects.equals(root, rootPos)) {
			rebuildContext(root);
			return false;
		}

		if (counterpartActed) {
			counterpartActed = false;
			softReset(root);
			return false;
		}

		if (affectedArea == null)
			affectedArea = new cqx(root, root);

		GameMode world = getWorld();
		if (!queue.isEmpty() && !isValid) {
			rebuildContext(root);
			return false;
		}

		if (validationFrontier.isEmpty() && !queue.isEmpty() && !simulate && revalidateIn == 0)
			revalidate(root);

		while (!queue.isEmpty()) {
			// Dont dequeue here, so we can decide not to dequeue a valid entry when
			// simulating
			BlockPos currentPos = queue.first().pos;
			PistonHandler blockState = world.d_(currentPos);
			PistonHandler emptied = blockState;
			cut fluid = FlowableFluid.FALLING;

			if (blockState.b(BambooLeaves.C) && blockState.c(BambooLeaves.C)) {
				emptied = blockState.a(BambooLeaves.C, Boolean.valueOf(false));
				fluid = FlowableFluid.c;
			} else if (blockState.b() instanceof LecternBlock) {
				LecternBlock flowingFluid = (LecternBlock) blockState.b();
				emptied = BellBlock.FACING.n();
				if (blockState.c(LecternBlock.FACING) == 0)
					fluid = flowingFluid.getFluid();
				else {
					affectedArea.c(new cqx(currentPos, currentPos));
					world.a(currentPos, emptied, 2 | 16);
					queue.dequeue();
					if (queue.isEmpty()) {
						isValid = checkValid(world, rootPos);
						reset();
					}
					continue;
				}
			} else if (blockState.m()
				.a() != FlowableFluid.FALLING
				&& blockState.b(world, currentPos, ArrayVoxelShape.a())
					.b()) {
				fluid = blockState.m()
					.a();
				emptied = BellBlock.FACING.n();
			}

			if (this.fluid == null)
				this.fluid = fluid;

			if (!this.fluid.a(fluid)) {
				queue.dequeue();
				if (queue.isEmpty()) {
					isValid = checkValid(world, rootPos);
					reset();
				}
				continue;
			}

			if (simulate)
				return true;

			playEffect(world, currentPos, fluid, true);
			AllTriggers.triggerForNearbyPlayers(AllTriggers.HOSE_PULLEY, world, tileEntity.o(), 8);

			if (infinite) {
				if (FluidHelper.isLava(fluid))
					AllTriggers.triggerForNearbyPlayers(AllTriggers.INFINITE_LAVA, world, tileEntity.o(), 8);
				if (FluidHelper.isWater(fluid))
					AllTriggers.triggerForNearbyPlayers(AllTriggers.INFINITE_WATER, world, tileEntity.o(), 8);
				if (fluid.a(AllFluids.CHOCOLATE.get()))
					AllTriggers.triggerForNearbyPlayers(AllTriggers.INFINITE_CHOCOLATE, world, tileEntity.o(), 8);
				return true;
			}

			world.a(currentPos, emptied, 2 | 16);
			affectedArea.c(new cqx(currentPos, currentPos));

			queue.dequeue();
			if (queue.isEmpty()) {
				isValid = checkValid(world, rootPos);
				reset();
			} else if (!validationSet.contains(currentPos)) {
				reset();
			}
			return true;
		}

		if (rootPos == null)
			return false;

		if (isValid)
			rebuildContext(root);

		return false;
	}

	protected void softReset(BlockPos root) {
		queue.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		visited.clear();
		infinite = false;
		setValidationTimer();
		frontier.add(new BlockPosEntry(root, 0));
		tileEntity.sendData();
	}

	protected boolean checkValid(GameMode world, BlockPos root) {
		BlockPos currentPos = root;
		for (int timeout = 1000; timeout > 0 && !root.equals(tileEntity.o()); timeout--) {
			FluidBlockType canPullFluidsFrom = canPullFluidsFrom(world.d_(currentPos), currentPos);
			if (canPullFluidsFrom == FluidBlockType.FLOWING) {
				currentPos = currentPos.up();
				continue;
			}
			if (canPullFluidsFrom == FluidBlockType.SOURCE)
				return true;
			break;
		}
		return false;
	}

	enum FluidBlockType {
		NONE, SOURCE, FLOWING;
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		if (!clientPacket && affectedArea != null)
			frontier.add(new BlockPosEntry(rootPos, 0));
	}

	protected FluidBlockType canPullFluidsFrom(PistonHandler blockState, BlockPos pos) {
		if (blockState.b(BambooLeaves.C) && blockState.c(BambooLeaves.C))
			return FluidBlockType.SOURCE;
		if (blockState.b() instanceof LecternBlock)
			return blockState.c(LecternBlock.FACING) == 0 ? FluidBlockType.SOURCE : FluidBlockType.FLOWING;
		if (blockState.m()
			.a() != FlowableFluid.FALLING && blockState.b(getWorld(), pos, ArrayVoxelShape.a())
				.b())
			return FluidBlockType.SOURCE;
		return FluidBlockType.NONE;
	}

	@Override
	public void tick() {
		super.tick();
		if (rootPos != null)
			isValid = checkValid(getWorld(), rootPos);
		if (!frontier.isEmpty()) {
			continueSearch();
			return;
		}
		if (!validationFrontier.isEmpty()) {
			continueValidation();
			return;
		}
		if (revalidateIn > 0)
			revalidateIn--;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
	}

	public void rebuildContext(BlockPos root) {
		reset();
		rootPos = root;
		affectedArea = new cqx(rootPos, rootPos);
		if (isValid)
			frontier.add(new BlockPosEntry(root, 0));
	}

	public void revalidate(BlockPos root) {
		validationFrontier.clear();
		validationVisited.clear();
		newValidationSet.clear();
		validationFrontier.add(new BlockPosEntry(root, 0));
		setValidationTimer();
	}

	private void continueSearch() {
		fluid = search(fluid, frontier, visited, (e, d) -> {
			queue.enqueue(new BlockPosEntry(e, d));
			validationSet.add(e);
		}, false);

		GameMode world = getWorld();
		int maxBlocks = maxBlocks();
		if (visited.size() > maxBlocks && canDrainInfinitely(fluid)) {
			infinite = true;
			// Find first block with valid fluid
			while (true) {
				BlockPos first = queue.first().pos;
				if (canPullFluidsFrom(world.d_(first), first) != FluidBlockType.SOURCE) {
					queue.dequeue();
					continue;
				}
				break;
			}
			BlockPos firstValid = queue.first().pos;
			frontier.clear();
			visited.clear();
			queue.clear();
			queue.enqueue(new BlockPosEntry(firstValid, 0));
			tileEntity.sendData();
			return;
		}

		if (!frontier.isEmpty())
			return;

		tileEntity.sendData();
		visited.clear();
	}

	private void continueValidation() {
		search(fluid, validationFrontier, validationVisited, (e, d) -> newValidationSet.add(e), false);

		int maxBlocks = maxBlocks();
		if (validationVisited.size() > maxBlocks && canDrainInfinitely(fluid)) {
			if (!infinite)
				reset();
			validationFrontier.clear();
			setLongValidationTimer();
			return;
		}

		if (!validationFrontier.isEmpty())
			return;
		if (infinite) {
			reset();
			return;
		}

		validationSet = newValidationSet;
		newValidationSet = new HashSet<>();
		validationVisited.clear();
	}

	@Override
	public void reset() {
		super.reset();

		fluid = null;
		rootPos = null;
		queue.clear();
		validationSet.clear();
		newValidationSet.clear();
		validationFrontier.clear();
		validationVisited.clear();
		tileEntity.sendData();
	}

	public static BehaviourType<FluidDrainingBehaviour> TYPE = new BehaviourType<>();

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	protected boolean isSearching() {
		return !frontier.isEmpty();
	}

	public FluidStack getDrainableFluid(BlockPos rootPos) {
		return fluid == null || isSearching() || !pullNext(rootPos, true) ? FluidStack.EMPTY
			: new FluidStack(fluid, 1000);
	}

}
