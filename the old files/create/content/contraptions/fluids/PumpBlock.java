package com.simibubi.kinetic_api.content.contraptions.fluids;

import java.util.Random;

import org.apache.commons.lang3.mutable.MutableBoolean;
import bnx;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalKineticBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
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

public class PumpBlock extends DirectionalKineticBlock implements SeagrassBlock {

	public PumpBlock(c p_i48415_1_) {
		super(p_i48415_1_);
		j(super.n().a(BambooLeaves.C, false));
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MECHANICAL_PUMP.create();
	}

	@Override
	public PistonHandler getRotatedBlockState(PistonHandler originalState, Direction targetedFace) {
		return originalState.a(FACING, originalState.c(FACING)
			.getOpposite());
	}

	@Override
	public PistonHandler updateAfterWrenched(PistonHandler newState, bnx context) {
		PistonHandler state = super.updateAfterWrenched(newState, context);
		GameMode world = context.p();
		BlockPos pos = context.a();
		if (world.v)
			return state;
		BeehiveBlockEntity tileEntity = world.c(pos);
		if (!(tileEntity instanceof PumpTileEntity))
			return state;
		PumpTileEntity pump = (PumpTileEntity) tileEntity;
		pump.sidesToUpdate.forEach(MutableBoolean::setTrue);
		pump.reversed = !pump.reversed;
		return state;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(FACING)
			.getAxis();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.PUMP.get(state.c(FACING));
	}

	@Override
	public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
		return true;
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
//		if (world.isRemote)
//			return;
//		if (otherBlock instanceof FluidPipeBlock)
//			return;
//		TileEntity tileEntity = world.getTileEntity(pos);
//		if (!(tileEntity instanceof PumpTileEntity))
//			return;
//		PumpTileEntity pump = (PumpTileEntity) tileEntity;
//		Direction facing = state.get(FACING);
//		for (boolean front : Iterate.trueAndFalse) {
//			Direction side = front ? facing : facing.getOpposite();
//			if (!pos.offset(side)
//				.equals(neighborPos))
//				continue;
//			pump.updatePipesOnSide(side);
//		}
	}

	@Override
	public EmptyFluid d(PistonHandler state) {
		return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false)
			: FlowableFluid.FALLING.h();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(BambooLeaves.C);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbourState,
		GrassColors world, BlockPos pos, BlockPos neighbourPos) {
		if (state.c(BambooLeaves.C)) {
			world.H()
				.a(pos, FlowableFluid.c, FlowableFluid.c.a(world));
		}
		return state;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		EmptyFluid FluidState = context.p()
			.b(context.a());
		return super.a(context).a(BambooLeaves.C,
			Boolean.valueOf(FluidState.a() == FlowableFluid.c));
	}

	public static boolean isPump(PistonHandler state) {
		return state.b() instanceof PumpBlock;
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (world.v)
			return;
		if (state != oldState)
			world.I()
				.a(pos, this, 1, StructureAccessor.c);
	}

	public static boolean isOpenAt(PistonHandler state, Direction d) {
		return d.getAxis() == state.c(FACING)
			.getAxis();
	}

	@Override
	public void a(PistonHandler state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		boolean blockTypeChanged = state.b() != newState.b();
		if (blockTypeChanged && !world.v)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.o(pos);
	}
	
}
