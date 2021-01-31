package com.simibubi.kinetic_api.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.fluids.PipeConnection.Flow;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.AxisPipeBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.Pair;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class FluidPropagator {

	public static void propagateChangedPipe(GrassColors world, BlockPos pipePos, PistonHandler pipeState) {
		List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		Set<Pair<PumpTileEntity, Direction>> discoveredPumps = new HashSet<>();

		frontier.add(Pair.of(0, pipePos));

		// Visit all connected pumps to update their network
		while (!frontier.isEmpty()) {
			Pair<Integer, BlockPos> pair = frontier.remove(0);
			BlockPos currentPos = pair.getSecond();
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);
			PistonHandler currentState = currentPos.equals(pipePos) ? pipeState : world.d_(currentPos);
			FluidTransportBehaviour pipe = getPipe(world, currentPos);
			if (pipe == null)
				continue;
			pipe.wipePressure();

			for (Direction direction : getPipeConnections(currentState, pipe)) {
				BlockPos target = currentPos.offset(direction);
				if (!world.isAreaLoaded(target, 0))
					continue;

				BeehiveBlockEntity tileEntity = world.c(target);
				PistonHandler targetState = world.d_(target);
				if (tileEntity instanceof PumpTileEntity) {
					if (!AllBlocks.MECHANICAL_PUMP.has(targetState) || targetState.c(PumpBlock.FACING)
						.getAxis() != direction.getAxis())
						continue;
					discoveredPumps.add(Pair.of((PumpTileEntity) tileEntity, direction.getOpposite()));
					continue;
				}
				if (visited.contains(target))
					continue;
				FluidTransportBehaviour targetPipe = getPipe(world, target);
				if (targetPipe == null)
					continue;
				Integer distance = pair.getFirst();
				if (distance >= getPumpRange() && !targetPipe.hasAnyPressure())
					continue;
				if (targetPipe.canHaveFlowToward(targetState, direction.getOpposite()))
					frontier.add(Pair.of(distance + 1, target));
			}
		}

		discoveredPumps.forEach(pair -> pair.getFirst()
			.updatePipesOnSide(pair.getSecond()));
	}

	public static void resetAffectedFluidNetworks(GameMode world, BlockPos start, Direction side) {
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		frontier.add(start);

		while (!frontier.isEmpty()) {
			BlockPos pos = frontier.remove(0);
			if (visited.contains(pos))
				continue;
			visited.add(pos);
			FluidTransportBehaviour pipe = getPipe(world, pos);
			if (pipe == null)
				continue;

			for (Direction d : Iterate.directions) {
				if (pos.equals(start) && d != side)
					continue;
				BlockPos target = pos.offset(d);
				if (visited.contains(target))
					continue;

				PipeConnection connection = pipe.getConnection(d);
				if (connection == null)
					continue;
				if (!connection.hasFlow())
					continue;

				Flow flow = connection.flow.get();
				if (!flow.inbound)
					continue;

				connection.resetNetwork();
				frontier.add(target);
			}
		}
	}

	public static Direction validateNeighbourChange(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock otherBlock,
		BlockPos neighborPos, boolean isMoving) {
		if (world.v)
			return null;
		if (otherBlock instanceof FluidPipeBlock)
			return null;
		if (otherBlock instanceof AxisPipeBlock)
			return null;
		if (otherBlock instanceof PumpBlock)
			return null;
		if (otherBlock instanceof LecternBlock)
			return null;
		if (getStraightPipeAxis(state) == null)
			return null;
		for (Direction d : Iterate.directions) {
			if (!pos.offset(d)
				.equals(neighborPos))
				continue;
			return d;
		}
		return null;
	}

	public static FluidTransportBehaviour getPipe(MobSpawnerLogic reader, BlockPos pos) {
		return TileEntityBehaviour.get(reader, pos, FluidTransportBehaviour.TYPE);
	}

	public static boolean isOpenEnd(MobSpawnerLogic reader, BlockPos pos, Direction side) {
		BlockPos connectedPos = pos.offset(side);
		PistonHandler connectedState = reader.d_(connectedPos);
		FluidTransportBehaviour pipe = FluidPropagator.getPipe(reader, connectedPos);
		if (pipe != null && pipe.canHaveFlowToward(connectedState, side.getOpposite()))
			return false;
		if (PumpBlock.isPump(connectedState) && connectedState.c(PumpBlock.FACING)
			.getAxis() == side.getAxis())
			return false;
		if (BlockHelper.hasBlockSolidSide(connectedState, reader, connectedPos, side.getOpposite()))
			return false;
		if (!(connectedState.c()
			.e() && connectedState.h(reader, connectedPos) != -1)
			&& !BlockHelper.hasBlockStateProperty(connectedState, BambooLeaves.C))
			return false;
		return true;
	}

	public static List<Direction> getPipeConnections(PistonHandler state, FluidTransportBehaviour pipe) {
		List<Direction> list = new ArrayList<>();
		for (Direction d : Iterate.directions)
			if (pipe.canHaveFlowToward(state, d))
				list.add(d);
		return list;
	}

	public static int getPumpRange() {
		return AllConfigs.SERVER.fluids.mechanicalPumpRange.get();
	}

//	static AxisAlignedBB smallCenter = new AxisAlignedBB(BlockPos.ZERO).shrink(.25);
//	
//	@Deprecated 
//	public static OutlineParams showBlockFace(BlockFace face) {
//		MutableObject<OutlineParams> params = new MutableObject<>(new OutlineParams());
//		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
//			Vector3d directionVec = new Vector3d(face.getFace()
//				.getDirectionVec());
//			Vector3d scaleVec = directionVec.scale(-.25f * face.getFace()
//				.getAxisDirection()
//				.getOffset());
//			directionVec = directionVec.scale(.45f);
//			params.setValue(CreateClient.outliner.showAABB(face,
//				FluidPropagator.smallCenter.offset(directionVec.add(new Vector3d(face.getPos())))
//					.grow(scaleVec.x, scaleVec.y, scaleVec.z)
//					.grow(1 / 16f)));
//		});
//		return params.getValue()
//			.lineWidth(1 / 16f);
//	}

	public static boolean hasFluidCapability(MobSpawnerLogic world, BlockPos pos, Direction side) {
		BeehiveBlockEntity tileEntity = world.c(pos);
		return tileEntity != null && tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)
			.isPresent();
	}

	@Nullable
	public static Axis getStraightPipeAxis(PistonHandler state) {
		if (state.b() instanceof PumpBlock)
			return state.c(PumpBlock.FACING)
				.getAxis();
		if (state.b() instanceof AxisPipeBlock)
			return state.c(AxisPipeBlock.e);
		if (!FluidPipeBlock.isPipe(state))
			return null;
		Axis axisFound = null;
		int connections = 0;
		for (Axis axis : Iterate.axes) {
			Direction d1 = Direction.get(AxisDirection.NEGATIVE, axis);
			Direction d2 = Direction.get(AxisDirection.POSITIVE, axis);
			boolean openAt1 = FluidPipeBlock.isOpenAt(state, d1);
			boolean openAt2 = FluidPipeBlock.isOpenAt(state, d2);
			if (openAt1)
				connections++;
			if (openAt2)
				connections++;
			if (openAt1 && openAt2)
				if (axisFound != null)
					return null;
				else
					axisFound = axis;
		}
		return connections == 2 ? axisFound : null;
	}

}
