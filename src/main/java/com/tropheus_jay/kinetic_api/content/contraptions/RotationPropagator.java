package com.tropheus_jay.kinetic_api.content.contraptions;

import com.tropheus_jay.kinetic_api.content.contraptions.base.IRotate;
import com.tropheus_jay.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.tropheus_jay.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

public class RotationPropagator {

	private static final int MAX_FLICKER_SCORE = 128;
	/**
	 * Determines the change in rotation between two attached kinetic entities. For
	 * instance, an axis connection returns 1 while a 1-to-1 gear connection
	 * reverses the rotation and therefore returns -1.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private static float getRotationSpeedModifier(KineticTileEntity from, KineticTileEntity to) {
		final BlockState stateFrom = from.getCachedState();
		final BlockState stateTo = to.getCachedState();

		Block fromBlock = stateFrom.getBlock();
		Block toBlock = stateTo.getBlock();
		if (!(fromBlock instanceof IRotate && toBlock instanceof IRotate))
			return 0;

		final IRotate definitionFrom = (IRotate) fromBlock;
		final IRotate definitionTo = (IRotate) toBlock;
		final BlockPos diff = to.getPos()
			.subtract(from.getPos());
		final Direction direction = Direction.getFacing(diff.getX(), diff.getY(), diff.getZ());
		final World world = from.getWorld();

		boolean alignedAxes = true;
		for (Axis axis : Axis.values())
			if (axis != direction.getAxis())
				if (axis.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
					alignedAxes = false;

		boolean connectedByAxis =
			alignedAxes && definitionFrom.hasShaftTowards(world, from.getPos(), stateFrom, direction)
				&& definitionTo.hasShaftTowards(world, to.getPos(), stateTo, direction.getOpposite());

		boolean connectedByGears = definitionFrom.hasIntegratedCogwheel(world, from.getPos(), stateFrom)
			&& definitionTo.hasIntegratedCogwheel(world, to.getPos(), stateTo);
/* todo: belts
		// Belt <-> Belt
		if (from instanceof BeltTileEntity && to instanceof BeltTileEntity && !connectedByAxis) {
			return ((BeltTileEntity) from).getController()
				.equals(((BeltTileEntity) to).getController()) ? 1 : 0;
		}

		// Axis <-> Axis
		if (connectedByAxis) {
			float axisModifier = getAxisModifier(to, direction.getOpposite());
			if (axisModifier != 0)
				axisModifier = 1 / axisModifier;
			return getAxisModifier(from, direction) * axisModifier;
		} */
/* todo: encased belts
		// Attached Encased Belts
		if (fromBlock instanceof EncasedBeltBlock && toBlock instanceof EncasedBeltBlock) {
			boolean connected = EncasedBeltBlock.areBlocksConnected(stateFrom, stateTo, direction);
			return connected ? EncasedBeltBlock.getRotationSpeedModifier(from, to) : 0;
		}
todo: gears

		// Large Gear <-> Large Gear
		if (isLargeToLargeGear(stateFrom, stateTo, diff)) {
			Axis sourceAxis = stateFrom.c(F);
			Axis targetAxis = stateTo.c(F);
			int sourceAxisDiff = sourceAxis.choose(diff.getX(), diff.getY(), diff.getZ());
			int targetAxisDiff = targetAxis.choose(diff.getX(), diff.getY(), diff.getZ());

			return sourceAxisDiff > 0 ^ targetAxisDiff > 0 ? -1 : 1;
		}

		// Gear <-> Large Gear
		if (isLargeCog(stateFrom) && definitionTo.hasIntegratedCogwheel(world, to.o(), stateTo))
			if (isLargeToSmallCog(stateFrom, stateTo, definitionTo, diff))
				return -2f;
		if (isLargeCog(stateTo) && definitionFrom.hasIntegratedCogwheel(world, from.o(), stateFrom))
			if (isLargeToSmallCog(stateTo, stateFrom, definitionFrom, diff))
				return -.5f;

		// Gear <-> Gear
		if (connectedByGears) {
			if (diff.getManhattanDistance(BlockPos.ORIGIN) != 1)
				return 0;
			if (isLargeCog(stateTo))
				return 0;
			if (direction.getAxis() == definitionFrom.getRotationAxis(stateFrom))
				return 0;
			if (definitionFrom.getRotationAxis(stateFrom) == definitionTo.getRotationAxis(stateTo))
				return -1;
		}*/

		return 0;
	}

	private static float getConveyedSpeed(KineticTileEntity from, KineticTileEntity to) {
		final BlockState stateFrom = from.getCachedState();
		final BlockState stateTo = to.getCachedState();
 //todo: rotation speed controller and more
/*
		// Rotation Speed Controller <-> Large Gear
		if (isLargeCogToSpeedController(stateFrom, stateTo, to.o()
			.subtract(from.o())))
			return SpeedControllerTileEntity.getConveyedSpeed(from, to, true);
		if (isLargeCogToSpeedController(stateTo, stateFrom, from.o()
			.subtract(to.o())))
			return SpeedControllerTileEntity.getConveyedSpeed(to, from, false);

		float rotationSpeedModifier = getRotationSpeedModifier(from, to);
		return from.getTheoreticalSpeed() * rotationSpeedModifier;
	}

	private static boolean isLargeToLargeGear(PistonHandler from, PistonHandler to, BlockPos diff) {
		if (!isLargeCog(from) || !isLargeCog(to))
			return false;
		Axis fromAxis = from.c(F);
		Axis toAxis = to.c(F);
		if (fromAxis == toAxis)
			return false;
		for (Axis axis : Axis.values()) {
			int axisDiff = axis.choose(diff.getX(), diff.getY(), diff.getZ());
			if (axis == fromAxis || axis == toAxis) {
				if (axisDiff == 0)
					return false;

			} else if (axisDiff != 0)
				return false;
		}
		return true;
	}

	private static float getAxisModifier(KineticTileEntity te, Direction direction) {
		if (!te.hasSource() || !(te instanceof DirectionalShaftHalvesTileEntity))
			return 1;
		Direction source = ((DirectionalShaftHalvesTileEntity) te).getSourceFacing();

		if (te instanceof GearboxTileEntity)
			return direction.getAxis() == source.getAxis() ? direction == source ? 1 : -1
				: direction.getDirection() == source.getDirection() ? -1 : 1;

		if (te instanceof SplitShaftTileEntity)
			return ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

		return 1;
	}

	private static boolean isLargeToSmallCog(PistonHandler from, PistonHandler to, IRotate defTo, BlockPos diff) {
		Axis axisFrom = from.c(F);
		if (axisFrom != defTo.getRotationAxis(to))
			return false;
		if (axisFrom.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return false;
		for (Axis axis : Axis.values()) {
			if (axis == axisFrom)
				continue;
			if (Math.abs(axis.choose(diff.getX(), diff.getY(), diff.getZ())) != 1)
				return false;
		}
		return true;
	}

	private static boolean isLargeCogToSpeedController(PistonHandler from, PistonHandler to, BlockPos diff) {
		if (!isLargeCog(from) || !AllBlocks.ROTATION_SPEED_CONTROLLER.has(to))
			return false;
		if (!diff.equals(BlockPos.ORIGIN.down()))
			return false;
		Axis axis = from.c(CogWheelBlock.AXIS);
		if (axis.isVertical())
			return false;
		if (to.c(SpeedControllerBlock.HORIZONTAL_AXIS) == axis)
			return false;
		return true;
	todo: this is a bad idea */return 1.0F;}

	/**
	 * Insert the added position to the kinetic network.
	 * 
	 * @param worldIn
	 * @param pos
	 */
	public static void handleAdded(World worldIn, BlockPos pos, KineticTileEntity addedTE) {
		if (worldIn.isClient)
			return;
		if (!worldIn.canSetBlock(pos))
			return;
		propagateNewSource(addedTE);
	}

	/**
	 * Search for sourceless networks attached to the given entity and update them.
	 * 
	 * @param currentTE
	 */
	private static void propagateNewSource(KineticTileEntity currentTE) {
		BlockPos pos = currentTE.getPos();
		World world = currentTE.getWorld();

		for (KineticTileEntity neighbourTE : getConnectedNeighbours(currentTE)) {
			float speedOfCurrent = currentTE.getTheoreticalSpeed();
			float speedOfNeighbour = neighbourTE.getTheoreticalSpeed();
			float newSpeed = getConveyedSpeed(currentTE, neighbourTE);
			float oppositeSpeed = getConveyedSpeed(neighbourTE, currentTE);

			boolean incompatible =
				Math.signum(newSpeed) != Math.signum(speedOfNeighbour) && (newSpeed != 0 && speedOfNeighbour != 0);

			boolean tooFast = true; //Math.abs(newSpeed) > AllConfigs.SERVER.kinetics.maxRotationSpeed.get(); todo: configs
			boolean speedChangedTooOften = currentTE.getFlickerScore() > MAX_FLICKER_SCORE;
			if (tooFast || speedChangedTooOften) {
				world.breakBlock(pos, true);
				return;
			}

			// Opposite directions
			if (incompatible) {
				world.breakBlock(pos, true);
				return;

				// Same direction: overpower the slower speed
			} else {

				// Neighbour faster, overpower the incoming tree
				if (Math.abs(oppositeSpeed) > Math.abs(speedOfCurrent)) {
					float prevSpeed = currentTE.getSpeed();
					currentTE.setSource(neighbourTE.getPos());
					currentTE.setSpeed(getConveyedSpeed(neighbourTE, currentTE));
					currentTE.onSpeedChanged(prevSpeed);
					currentTE.sendData();

					propagateNewSource(currentTE);
					return;
				}

				// Current faster, overpower the neighbours' tree
				if (Math.abs(newSpeed) >= Math.abs(speedOfNeighbour)) {

					// Do not overpower you own network -> cycle
					if (!currentTE.hasNetwork() || currentTE.network.equals(neighbourTE.network)) {
						float epsilon = Math.abs(speedOfNeighbour) / 256f / 256f;
						if (Math.abs(newSpeed) > Math.abs(speedOfNeighbour) + epsilon)
							world.breakBlock(pos, true);
						continue;
					}

					if (currentTE.hasSource() && currentTE.source.equals(neighbourTE.getPos()))
						currentTE.removeSource();

					float prevSpeed = neighbourTE.getSpeed();
					neighbourTE.setSource(currentTE.getPos());
					neighbourTE.setSpeed(getConveyedSpeed(currentTE, neighbourTE));
					neighbourTE.onSpeedChanged(prevSpeed);
					neighbourTE.sendData();
					propagateNewSource(neighbourTE);
					continue;
				}
			}

			if (neighbourTE.getTheoreticalSpeed() == newSpeed)
				continue;

			float prevSpeed = neighbourTE.getSpeed();
			neighbourTE.setSpeed(newSpeed);
			neighbourTE.setSource(currentTE.getPos());
			neighbourTE.onSpeedChanged(prevSpeed);
			neighbourTE.sendData();
			propagateNewSource(neighbourTE);

		}
	}

	/**
	 * Remove the given entity from the network.
	 * 
	 * @param worldIn
	 * @param pos
	 * @param removedTE
	 */
	public static void handleRemoved(World worldIn, BlockPos pos, KineticTileEntity removedTE) {
		if (worldIn.isClient)
			return;
		if (removedTE == null)
			return;
		if (removedTE.getTheoreticalSpeed() == 0)
			return;

		for (BlockPos neighbourPos : getPotentialNeighbourLocations(removedTE)) {
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!(neighbourState.getBlock() instanceof IRotate))
				continue;
			BlockEntity tileEntity = worldIn.getBlockEntity(neighbourPos);
			if (!(tileEntity instanceof KineticTileEntity))
				continue;

			final KineticTileEntity neighbourTE = (KineticTileEntity) tileEntity;
			if (!neighbourTE.hasSource() || !neighbourTE.source.equals(pos))
				continue;

			propagateMissingSource(neighbourTE);
		}

	}

	/**
	 * Clear the entire subnetwork depending on the given entity and find a new
	 * source
	 * 
	 * @param updateTE
	 */
	private static void propagateMissingSource(KineticTileEntity updateTE) {
		final World world = updateTE.getWorld();

		List<KineticTileEntity> potentialNewSources = new LinkedList<>();
		List<BlockPos> frontier = new LinkedList<>();
		frontier.add(updateTE.getPos());
		BlockPos missingSource = updateTE.hasSource() ? updateTE.source : null;

		while (!frontier.isEmpty()) {
			final BlockPos pos = frontier.remove(0);
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (!(tileEntity instanceof KineticTileEntity))
				continue;
			final KineticTileEntity currentTE = (KineticTileEntity) tileEntity;

			currentTE.removeSource();
			currentTE.sendData();

			for (KineticTileEntity neighbourTE : getConnectedNeighbours(currentTE)) {
				if (neighbourTE.getPos()
					.equals(missingSource))
					continue;
				if (!neighbourTE.hasSource())
					continue;

				if (!neighbourTE.source.equals(pos)) {
					potentialNewSources.add(neighbourTE);
					continue;
				}

				if (neighbourTE.isSource())
					potentialNewSources.add(neighbourTE);

				frontier.add(neighbourTE.getPos());
			}
		}

		for (KineticTileEntity newSource : potentialNewSources) {
			if (newSource.hasSource() || newSource.isSource()) {
				propagateNewSource(newSource);
				return;
			}
		}
	}

	private static KineticTileEntity findConnectedNeighbour(KineticTileEntity currentTE, BlockPos neighbourPos) {
		BlockState neighbourState = currentTE.getWorld()
			.getBlockState(neighbourPos);
		if (!(neighbourState.getBlock() instanceof IRotate))
			return null;
		/* todo: nononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononononono
		if (!neighbourState.hasBlockEntity())
			return null;*/
		BlockEntity neighbourTE = currentTE.getWorld()
			.getBlockEntity(neighbourPos);
		if (!(neighbourTE instanceof KineticTileEntity))
			return null;
		KineticTileEntity neighbourKTE = (KineticTileEntity) neighbourTE;
		if (!(neighbourKTE.getCachedState()
			.getBlock() instanceof IRotate))
			return null;
		if (!isConnected(currentTE, neighbourKTE))
			return null;
		return neighbourKTE;
	}

	public static boolean isConnected(KineticTileEntity from, KineticTileEntity to) {
		final BlockState stateFrom = from.getCachedState();
		final BlockState stateTo = to.getCachedState();
/*todo: cogs and RSC
		if (isLargeCogToSpeedController(stateFrom, stateTo, to.o()
			.subtract(from.o())))
			return true;
		if (isLargeCogToSpeedController(stateTo, stateFrom, from.o()
			.subtract(to.o())))
			return true;*/
		return getRotationSpeedModifier(from, to) != 0;
	}

	private static List<KineticTileEntity> getConnectedNeighbours(KineticTileEntity te) {
		List<KineticTileEntity> neighbours = new LinkedList<>();
		for (BlockPos neighbourPos : getPotentialNeighbourLocations(te)) {
			final KineticTileEntity neighbourTE = findConnectedNeighbour(te, neighbourPos);
			if (neighbourTE == null)
				continue;

			neighbours.add(neighbourTE);
		}
		return neighbours;
	}

	private static List<BlockPos> getPotentialNeighbourLocations(KineticTileEntity te) {
		List<BlockPos> neighbours = new LinkedList<>();


		// isRegionLoaded requires a range
		// Forge's version requires a center and a radius
		// this is a cursed child of the 2
		if (!te.getWorld().isRegionLoaded(te.getPos().add(-1, -1, -1), te.getPos().add(1, 1, 1)))
			return neighbours;

		for (Direction facing : Iterate.directions)
			neighbours.add(te.getPos()
				.offset(facing));

		// Some Blocks can interface diagonally
		BlockState blockState = te.getCachedState();
		//boolean isLargeWheel = isLargeCog(blockState); todo: cogwheels

		if (!(blockState.getBlock() instanceof IRotate))
			return neighbours;
		IRotate block = (IRotate) blockState.getBlock();
/* todo: cogwheels
		if (block.hasIntegratedCogwheel(te.v(), te.o(), blockState) || isLargeWheel
			|| AllBlocks.BELT.has(blockState)) {
			Axis axis = block.getRotationAxis(blockState);

			BlockPos.stream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
				.forEach(offset -> {
					if (!isLargeWheel && axis.choose(offset.getX(), offset.getY(), offset.getZ()) != 0)
						return;
					if (offset.getSquaredDistance(0, 0, 0, false) != BlockPos.ORIGIN.getSquaredDistance(1, 1, 0, false))
						return;
					neighbours.add(te.o()
						.add(offset));
				});
		}*/

		return neighbours;
	}

}
