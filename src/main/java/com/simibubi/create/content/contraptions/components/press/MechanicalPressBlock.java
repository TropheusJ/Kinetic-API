package com.simibubi.create.content.contraptions.components.press;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.MobSpawnerLogic;

public class MechanicalPressBlock extends HorizontalKineticBlock implements ITE<MechanicalPressTileEntity> {

	public MechanicalPressBlock(c properties) {
		super(properties);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		if (context.getEntity() instanceof PlayerAbilities)
			return AllShapes.CASING_14PX.get(Direction.DOWN);
		return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		return !AllBlocks.BASIN.has(worldIn.d_(pos.down()));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MECHANICAL_PRESS.create();
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction prefferedSide = getPreferredHorizontalFacing(context);
		if (prefferedSide != null)
			return n().a(HORIZONTAL_FACING, prefferedSide);
		return super.a(context);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() == state.c(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public Class<MechanicalPressTileEntity> getTileEntityClass() {
		return MechanicalPressTileEntity.class;
	}

}
