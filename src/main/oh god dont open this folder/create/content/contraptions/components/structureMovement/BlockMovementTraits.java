package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.components.actors.AttachedActorBlock;
import com.simibubi.create.content.contraptions.components.actors.HarvesterBlock;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.components.crank.HandCrankBlock;
import com.simibubi.create.content.contraptions.components.fan.NozzleBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankConnectivityHandler;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.TallSeagrassBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class BlockMovementTraits {

	public static boolean movementNecessary(GameMode world, BlockPos pos) {
		PistonHandler state = world.d_(pos);
		if (isBrittle(state))
			return true;
		if (state.b() instanceof FallingBlock)
			return true;
		if (state.c()
			.e())
			return false;
		if (state.k(world, pos)
			.b())
			return false;
		return true;
	}

	public static boolean movementAllowed(GameMode world, BlockPos pos) {
		PistonHandler blockState = world.d_(pos);
		BeetrootsBlock block = blockState.b();
		if (block instanceof AbstractChassisBlock)
			return true;
		if (blockState.h(world, pos) == -1)
			return false;
		if (AllBlockTags.NON_MOVABLE.matches(blockState))
			return false;

		// Move controllers only when they aren't moving
		if (block instanceof MechanicalPistonBlock && blockState.c(MechanicalPistonBlock.STATE) != PistonState.MOVING)
			return true;
		if (block instanceof MechanicalBearingBlock) {
			BeehiveBlockEntity te = world.c(pos);
			if (te instanceof MechanicalBearingTileEntity)
				return !((MechanicalBearingTileEntity) te).isRunning();
		}
		if (block instanceof ClockworkBearingBlock) {
			BeehiveBlockEntity te = world.c(pos);
			if (te instanceof ClockworkBearingTileEntity)
				return !((ClockworkBearingTileEntity) te).isRunning();
		}
		if (block instanceof PulleyBlock) {
			BeehiveBlockEntity te = world.c(pos);
			if (te instanceof PulleyTileEntity)
				return !((PulleyTileEntity) te).running;
		}

		if (AllBlocks.BELT.has(blockState))
			return true;
		if (blockState.b() instanceof GrassBlock)
			return true;
		return blockState.k() != LavaFluid.c;
	}

	/**
	 * Brittle blocks will be collected first, as they may break when other blocks
	 * are removed before them
	 */
	public static boolean isBrittle(PistonHandler state) {
		BeetrootsBlock block = state.b();
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.j))
			return true;

		if (block instanceof JukeboxBlock)
			return true;
		if (block instanceof TallSeagrassBlock)
			return true;
		if (block instanceof DeadCoralWallFanBlock)
			return true;
		if (block instanceof EndRodBlock && !(block instanceof GrassBlock))
			return true;
		if (block instanceof CartAssemblerBlock)
			return false;
		if (block instanceof BlockWithEntity)
			return true;
		if (block instanceof DaylightDetectorBlock)
			return true;
		if (block instanceof RailBlock)
			return true;
		if (block instanceof WitherSkullBlock)
			return true;
		return AllBlockTags.BRITTLE.tag.a(block);
	}

	/**
	 * Attached blocks will move if blocks they are attached to are moved
	 */
	public static boolean isBlockAttachedTowards(MobSpawnerLogic world, BlockPos pos, PistonHandler state,
		Direction direction) {
		BeetrootsBlock block = state.b();
		if (block instanceof JukeboxBlock)
			return state.c(JukeboxBlock.HAS_RECORD) == direction.getOpposite();
		if (block instanceof WallBlock)
			return state.c(WallBlock.UP) == direction.getOpposite();
		if (block instanceof DeadCoralWallFanBlock)
			return direction == Direction.DOWN;
		if (block instanceof AbstractRedstoneGateBlock)
			return direction == Direction.DOWN;
		if (block instanceof RedstoneLinkBlock)
			return direction.getOpposite() == state.c(RedstoneLinkBlock.SHAPE);
		if (block instanceof FireBlock)
			return direction == Direction.DOWN;
		if (block instanceof DaylightDetectorBlock)
			return direction == Direction.DOWN;
		if (block instanceof RailBlock)
			return direction == Direction.DOWN;
		if (block instanceof WitherSkullBlock)
			return direction == Direction.DOWN;
		if (block instanceof RedstoneWireBlock)
			return state.c(RedstoneWireBlock.WIRE_CONNECTION_EAST) == direction.getOpposite();
		if (block instanceof TallSeagrassBlock)
			return direction == Direction.DOWN;
		if (block instanceof EndRodBlock) {
			BlockPredicate attachFace = state.c(EndRodBlock.u);
			if (attachFace == BlockPredicate.c)
				return direction == Direction.UP;
			if (attachFace == BlockPredicate.block)
				return direction == Direction.DOWN;
			if (attachFace == BlockPredicate.b)
				return direction.getOpposite() == state.c(EndRodBlock.aq);
		}
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.j))
			return direction == (state.c(BambooLeaves.j) ? Direction.UP : Direction.DOWN);
		if (block instanceof BlockWithEntity)
			return direction == Direction.DOWN;
		if (block instanceof AttachedActorBlock)
			return direction == state.c(HarvesterBlock.aq)
				.getOpposite();
		if (block instanceof HandCrankBlock)
			return direction == state.c(HandCrankBlock.FACING)
				.getOpposite();
		if (block instanceof NozzleBlock)
			return direction == state.c(NozzleBlock.SHAPE)
				.getOpposite();
		if (block instanceof EngineBlock)
			return direction == state.c(EngineBlock.aq)
				.getOpposite();
		if (block instanceof BedBlock) {
			WallMountLocation attachment = state.c(BambooLeaves.R);
			if (attachment == WallMountLocation.FLOOR)
				return direction == Direction.DOWN;
			if (attachment == WallMountLocation.CEILING)
				return direction == Direction.UP;
			return direction == state.c(HayBlock.aq);
		}
		if (state.b() instanceof SailBlock)
			return direction.getAxis() != state.c(SailBlock.SHAPE)
				.getAxis();
		if (state.b() instanceof FluidTankBlock)
			return FluidTankConnectivityHandler.isConnected(world, pos, pos.offset(direction));
		return false;
	}

	/**
	 * Non-Supportive blocks will not continue a chain of blocks picked up by e.g. a
	 * piston
	 */
	public static boolean notSupportive(PistonHandler state, Direction facing) {
		if (AllBlocks.MECHANICAL_DRILL.has(state))
			return state.c(BambooLeaves.M) == facing;
		if (AllBlocks.MECHANICAL_BEARING.has(state))
			return state.c(BambooLeaves.M) == facing;
		if (AllBlocks.CART_ASSEMBLER.has(state))
			return Direction.DOWN == facing;
		if (AllBlocks.MECHANICAL_SAW.has(state))
			return state.c(BambooLeaves.M) == facing;
		if (AllBlocks.PORTABLE_STORAGE_INTERFACE.has(state))
			return state.c(PortableStorageInterfaceBlock.SHAPE) == facing;
		if (state.b() instanceof AttachedActorBlock)
			return state.c(BambooLeaves.O) == facing;
		if (AllBlocks.ROPE_PULLEY.has(state))
			return facing == Direction.DOWN;
		if (state.b() instanceof WitherSkullBlock)
			return facing == Direction.UP;
		if (state.b() instanceof SailBlock)
			return facing.getAxis() == state.c(SailBlock.SHAPE)
				.getAxis();
		return isBrittle(state);
	}

}
