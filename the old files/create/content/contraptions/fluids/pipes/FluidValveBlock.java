package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import java.util.Random;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidPropagator;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class FluidValveBlock extends DirectionalAxisKineticBlock implements IAxisPipe {

	public static final BedPart ENABLED = BedPart.a("enabled");

	public FluidValveBlock(c properties) {
		super(properties);
		j(n().a(ENABLED, false));
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.FLUID_VALVE.get(getPipeAxis(state));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(ENABLED));
	}

	@Override
	protected boolean prefersConnectionTo(ItemConvertible reader, BlockPos pos, Direction facing, boolean shaftAxis) {
		if (!shaftAxis) {
			BlockPos offset = pos.offset(facing);
			PistonHandler blockState = reader.d_(offset);
			return FluidPipeBlock.canConnectTo(reader, offset, blockState, facing);
		}
		return super.prefersConnectionTo(reader, pos, facing, shaftAxis);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FLUID_VALVE.create();
	}

	public static Axis getPipeAxis(PistonHandler state) {
		if (!(state.b() instanceof FluidValveBlock))
			return null;
		Direction facing = state.c(FACING);
		boolean alongFirst = !state.c(AXIS_ALONG_FIRST_COORDINATE);
		for (Axis axis : Iterate.axes) {
			if (axis == facing.getAxis())
				continue;
			if (!alongFirst) {
				alongFirst = true;
				continue;
			}
			return axis;
		}
		return null;
	}

	@Override
	public Axis getAxis(PistonHandler state) {
		return getPipeAxis(state);
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		boolean blockTypeChanged = state.b() != newState.b();
		if (blockTypeChanged && !world.v)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.o(pos);
	}

	@Override
	public boolean a(PistonHandler p_196260_1_, ItemConvertible p_196260_2_, BlockPos p_196260_3_) {
		return true;
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (world.v)
			return;
		if (state != oldState)
			world.I()
				.a(pos, this, 1, StructureAccessor.c);
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugInfoSender.a(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.I()
			.a(pos, this, 1, StructureAccessor.c);
	}

	public static boolean isOpenAt(PistonHandler state, Direction d) {
		return d.getAxis() == getPipeAxis(state);
	}

	@Override
	public void a(PistonHandler state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

}
