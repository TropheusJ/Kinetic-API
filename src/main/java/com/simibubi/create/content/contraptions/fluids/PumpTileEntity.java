package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import bqx;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.fluids.PumpTileEntity.PumpFluidTransferBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class PumpTileEntity extends KineticTileEntity {

	LerpedFloat arrowDirection;
	Couple<MutableBoolean> sidesToUpdate;
	boolean reversed;

	public PumpTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
		arrowDirection = LerpedFloat.linear()
			.startWithValue(1);
		sidesToUpdate = Couple.create(MutableBoolean::new);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(new PumpFluidTransferBehaviour(this));
	}

	@Override
	public void initialize() {
		super.initialize();
		reversed = getSpeed() < 0;
	}

	@Override
	public void aj_() {
		super.aj_();
		float speed = getSpeed();

		if (d.v) {
			if (speed == 0)
				return;
			arrowDirection.chase(speed >= 0 ? 1 : -1, .5f, Chaser.EXP);
			arrowDirection.tickChaser();
			return;
		}

		sidesToUpdate.forEachWithContext((update, isFront) -> {
			if (update.isFalse())
				return;
			update.setFalse();
			distributePressureTo(isFront ? getFront() : getFront().getOpposite());
		});

		if (speed == 0)
			return;
		if (speed < 0 != reversed) {
			reversed = speed < 0;
			return;
		}
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);

		if (previousSpeed == getSpeed())
			return;
		if (speed != 0)
			reversed = speed < 0;
		if (d.v)
			return;

		BlockPos frontPos = e.offset(getFront());
		BlockPos backPos = e.offset(getFront().getOpposite());
		FluidPropagator.propagateChangedPipe(d, frontPos, d.d_(frontPos));
		FluidPropagator.propagateChangedPipe(d, backPos, d.d_(backPos));
	}

	protected void distributePressureTo(Direction side) {
		if (getSpeed() == 0)
			return;

		BlockFace start = new BlockFace(e, side);
		boolean pull = isPullingOnSide(isFront(side));
		Set<BlockFace> targets = new HashSet<>();
		Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

		if (!pull)
			FluidPropagator.resetAffectedFluidNetworks(d, e, side.getOpposite());

		if (!hasReachedValidEndpoint(d, start, pull)) {

			pipeGraph.computeIfAbsent(e, $ -> Pair.of(0, new IdentityHashMap<>()))
				.getSecond()
				.put(side, pull);
			pipeGraph.computeIfAbsent(start.getConnectedPos(), $ -> Pair.of(1, new IdentityHashMap<>()))
				.getSecond()
				.put(side.getOpposite(), !pull);

			List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
			Set<BlockPos> visited = new HashSet<>();
			int maxDistance = FluidPropagator.getPumpRange();
			frontier.add(Pair.of(1, start.getConnectedPos()));

			while (!frontier.isEmpty()) {
				Pair<Integer, BlockPos> entry = frontier.remove(0);
				int distance = entry.getFirst();
				BlockPos currentPos = entry.getSecond();

				if (!d.isAreaLoaded(currentPos, 0))
					continue;
				if (visited.contains(currentPos))
					continue;
				visited.add(currentPos);
				PistonHandler currentState = d.d_(currentPos);
				FluidTransportBehaviour pipe = FluidPropagator.getPipe(d, currentPos);
				if (pipe == null)
					continue;

				for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
					BlockFace blockFace = new BlockFace(currentPos, face);
					BlockPos connectedPos = blockFace.getConnectedPos();

					if (!d.isAreaLoaded(connectedPos, 0))
						continue;
					if (blockFace.isEquivalent(start))
						continue;
					if (hasReachedValidEndpoint(d, blockFace, pull)) {
						pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
							.getSecond()
							.put(face, pull);
						targets.add(blockFace);
						continue;
					}

					FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(d, connectedPos);
					if (pipeBehaviour == null)
						continue;
					if (pipeBehaviour instanceof PumpFluidTransferBehaviour)
						continue;
					if (visited.contains(connectedPos))
						continue;
					if (distance + 1 >= maxDistance) {
						pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
							.getSecond()
							.put(face, pull);
						targets.add(blockFace);
						continue;
					}

					pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
						.getSecond()
						.put(face, pull);
					pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>()))
						.getSecond()
						.put(face.getOpposite(), !pull);
					frontier.add(Pair.of(distance + 1, connectedPos));
				}
			}
		}

		// DFS
		Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
		searchForEndpointRecursively(pipeGraph, targets, validFaces,
			new BlockFace(start.getPos(), start.getOppositeFace()), pull);

		float pressure = Math.abs(getSpeed());
		for (Set<BlockFace> set : validFaces.values()) {
			int parallelBranches = set.size();
			for (BlockFace face : set) {
				BlockPos pipePos = face.getPos();
				Direction pipeSide = face.getFace();

				if (pipePos.equals(e))
					continue;

				boolean inbound = pipeGraph.get(pipePos)
					.getSecond()
					.get(pipeSide);
				FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(d, pipePos);
				if (pipeBehaviour == null)
					continue;

				pipeBehaviour.addPressure(pipeSide, inbound, pressure / parallelBranches);
			}
		}

	}

	protected boolean searchForEndpointRecursively(Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
		Set<BlockFace> targets, Map<Integer, Set<BlockFace>> validFaces, BlockFace currentFace, boolean pull) {
		BlockPos currentPos = currentFace.getPos();
		if (!pipeGraph.containsKey(currentPos))
			return false;
		Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
		int distance = pair.getFirst();

		boolean atLeastOneBranchSuccessful = false;
		for (Direction nextFacing : Iterate.directions) {
			if (nextFacing == currentFace.getFace())
				continue;
			Map<Direction, Boolean> map = pair.getSecond();
			if (!map.containsKey(nextFacing))
				continue;

			BlockFace localTarget = new BlockFace(currentPos, nextFacing);
			if (targets.contains(localTarget)) {
				validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
					.add(localTarget);
				atLeastOneBranchSuccessful = true;
				continue;
			}

			if (map.get(nextFacing) != pull)
				continue;
			if (!searchForEndpointRecursively(pipeGraph, targets, validFaces,
				new BlockFace(currentPos.offset(nextFacing), nextFacing.getOpposite()), pull))
				continue;

			validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
				.add(localTarget);
			atLeastOneBranchSuccessful = true;
		}

		if (atLeastOneBranchSuccessful)
			validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
				.add(currentFace);

		return atLeastOneBranchSuccessful;
	}

	private boolean hasReachedValidEndpoint(GrassColors world, BlockFace blockFace, boolean pull) {
		BlockPos connectedPos = blockFace.getConnectedPos();
		PistonHandler connectedState = world.d_(connectedPos);
		BeehiveBlockEntity tileEntity = world.c(connectedPos);
		Direction face = blockFace.getFace();

		// facing a pump
		if (PumpBlock.isPump(connectedState) && connectedState.c(PumpBlock.FACING)
			.getAxis() == face.getAxis() && tileEntity instanceof PumpTileEntity) {
			PumpTileEntity pumpTE = (PumpTileEntity) tileEntity;
			return pumpTE.isPullingOnSide(pumpTE.isFront(blockFace.getOppositeFace())) != pull;
		}

		// other pipe, no endpoint
		FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
		if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
			return false;

		// fluid handler endpoint
		if (tileEntity != null) {
			LazyOptional<IFluidHandler> capability =
				tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
			if (capability.isPresent())
				return true;
		}

		// open endpoint
		return FluidPropagator.isOpenEnd(world, blockFace.getPos(), face);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Reversed", reversed);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		reversed = compound.getBoolean("Reversed");
		super.fromTag(state, compound, clientPacket);
	}

	public void updatePipesOnSide(Direction side) {
		if (!isSideAccessible(side))
			return;
		updatePipeNetwork(isFront(side));
		getBehaviour(FluidTransportBehaviour.TYPE).wipePressure();
	}

	protected boolean isFront(Direction side) {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof PumpBlock))
			return false;
		Direction front = blockState.c(PumpBlock.FACING);
		boolean isFront = side == front;
		return isFront;
	}

	@Nullable
	protected Direction getFront() {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof PumpBlock))
			return null;
		return blockState.c(PumpBlock.FACING);
	}

	protected void updatePipeNetwork(boolean front) {
		sidesToUpdate.get(front)
			.setTrue();
	}

	public boolean isSideAccessible(Direction side) {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof PumpBlock))
			return false;
		return blockState.c(PumpBlock.FACING)
			.getAxis() == side.getAxis();
	}

	public boolean isPullingOnSide(boolean front) {
		return front == reversed;
	}

	class PumpFluidTransferBehaviour extends FluidTransportBehaviour {

		public PumpFluidTransferBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public void tick() {
			super.tick();
			for (Entry<Direction, PipeConnection> entry : interfaces.entrySet()) {
				boolean pull = isPullingOnSide(isFront(entry.getKey()));
				Couple<Float> pressure = entry.getValue().pressure;
				pressure.set(pull, Math.abs(getSpeed()));
				pressure.set(!pull, 0f);
			}
		}

		@Override
		public boolean canHaveFlowToward(PistonHandler state, Direction direction) {
			return isSideAccessible(direction);
		}

		@Override
		public AttachmentTypes getRenderedRimAttachment(bqx world, BlockPos pos, PistonHandler state,
			Direction direction) {
			AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
			if (attachment == AttachmentTypes.RIM)
				return AttachmentTypes.NONE;
			return attachment;
		}

	}

}
