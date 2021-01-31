package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import java.util.Random;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidPropagator;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.VoxelShaper;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.block.BlockPredicate;
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

public class SmartFluidPipeBlock extends EndRodBlock implements IAxisPipe {

	public SmartFluidPipeBlock(c p_i48339_1_) {
		super(p_i48339_1_);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(u)
			.a(aq);
	}

	@Override
	public PistonHandler a(PotionUtil ctx) {
		PistonHandler stateForPlacement = super.a(ctx);
		Axis prefferedAxis = null;
		BlockPos pos = ctx.a();
		GameMode world = ctx.p();
		for (Direction side : Iterate.directions) {
			if (!prefersConnectionTo(world, pos, side))
				continue;
			if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
				prefferedAxis = null;
				break;
			}
			prefferedAxis = side.getAxis();
		}

		if (prefferedAxis == Axis.Y)
			stateForPlacement = stateForPlacement.a(u, BlockPredicate.b)
				.a(aq, stateForPlacement.c(aq)
					.getOpposite());
		else if (prefferedAxis != null) {
			if (stateForPlacement.c(u) == BlockPredicate.b)
				stateForPlacement = stateForPlacement.a(u, BlockPredicate.block);
			for (Direction direction : ctx.e()) {
				if (direction.getAxis() != prefferedAxis)
					continue;
				stateForPlacement = stateForPlacement.a(aq, direction.getOpposite());
			}
		}

		return stateForPlacement;
	}

	protected boolean prefersConnectionTo(ItemConvertible reader, BlockPos pos, Direction facing) {
		BlockPos offset = pos.offset(facing);
		PistonHandler blockState = reader.d_(offset);
		return FluidPipeBlock.canConnectTo(reader, offset, blockState, facing);
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

	protected static Axis getPipeAxis(PistonHandler state) {
		return state.c(u) == BlockPredicate.b ? Axis.Y
			: state.c(aq)
				.getAxis();
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.SMART_FLUID_PIPE.create();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		BlockPredicate face = state.c(u);
		VoxelShaper shape = face == BlockPredicate.block ? AllShapes.SMART_FLUID_PIPE_FLOOR
			: face == BlockPredicate.c ? AllShapes.SMART_FLUID_PIPE_CEILING : AllShapes.SMART_FLUID_PIPE_WALL;
		return shape.get(state.c(aq));
	}

	@Override
	public Axis getAxis(PistonHandler state) {
		return getPipeAxis(state);
	}

}
