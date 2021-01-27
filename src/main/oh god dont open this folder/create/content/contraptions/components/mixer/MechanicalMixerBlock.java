package com.simibubi.create.content.contraptions.components.mixer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.MobSpawnerLogic;

public class MechanicalMixerBlock extends KineticBlock implements ITE<MechanicalMixerTileEntity> {

	public MechanicalMixerBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MECHANICAL_MIXER.create();
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		return !AllBlocks.BASIN.has(worldIn.d_(pos.down()));
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		if (context.getEntity() instanceof PlayerAbilities)
			return AllShapes.CASING_14PX.get(Direction.DOWN);

		return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return Axis.Y;
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return false;
	}

	@Override
	public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
		return true;
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	@Override
	public SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.MEDIUM;
	}

	@Override
	public Class<MechanicalMixerTileEntity> getTileEntityClass() {
		return MechanicalMixerTileEntity.class;
	}

}
