package com.simibubi.kinetic_api.content.contraptions.components.motor;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalKineticBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.MobSpawnerLogic;

public class CreativeMotorBlock extends DirectionalKineticBlock {

	public CreativeMotorBlock(c properties) {
		super(properties);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.MOTOR_BLOCK.get(state.c(FACING));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MOTOR.create();
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction preferred = getPreferredFacing(context);
		if ((context.n() != null && context.n()
			.bt()) || preferred == null)
			return super.a(context);
		return n().a(FACING, preferred);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(FACING);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(FACING)
			.getAxis();
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}
}
