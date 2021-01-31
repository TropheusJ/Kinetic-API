package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class HosePulleyBlock extends HorizontalKineticBlock implements ITE<HosePulleyTileEntity> {

	public HosePulleyBlock(c properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_FACING)
			.rotateYClockwise()
			.getAxis();
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction preferredHorizontalFacing = getPreferredHorizontalFacing(context);
		return this.n()
			.a(HORIZONTAL_FACING,
				preferredHorizontalFacing != null ? preferredHorizontalFacing.rotateYCounterclockwise()
					: context.f()
						.getOpposite());
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return state.c(HORIZONTAL_FACING)
			.rotateYClockwise() == face;
	}

	public static boolean hasPipeTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return state.c(HORIZONTAL_FACING)
			.rotateYCounterclockwise() == face;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.HOSE_PULLEY.create();
	}

	@Override
	public Direction getPreferredHorizontalFacing(PotionUtil context) {
		Direction fromParent = super.getPreferredHorizontalFacing(context);
		if (fromParent != null)
			return fromParent;

		Direction prefferedSide = null;
		for (Direction facing : Iterate.horizontalDirections) {
			BlockPos pos = context.a()
				.offset(facing);
			PistonHandler blockState = context.p()
				.d_(pos);
			if (FluidPipeBlock.canConnectTo(context.p(), pos, blockState, facing))
				if (prefferedSide != null && prefferedSide.getAxis() != facing.getAxis()) {
					prefferedSide = null;
					break;
				} else
					prefferedSide = facing;
		}
		return prefferedSide == null ? null : prefferedSide.getOpposite();
	}

	@Override
	public void a(PistonHandler p_196243_1_, GameMode world, BlockPos pos, PistonHandler p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.b() != p_196243_4_.b() || !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(world, pos, FluidDrainingBehaviour.TYPE);
			TileEntityBehaviour.destroy(world, pos, FluidFillingBehaviour.TYPE);
			world.o(pos);
		}
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.PULLEY.get(state.c(HORIZONTAL_FACING)
			.rotateYClockwise()
			.getAxis());
	}

	@Override
	public Class<HosePulleyTileEntity> getTileEntityClass() {
		return HosePulleyTileEntity.class;
	}

}
