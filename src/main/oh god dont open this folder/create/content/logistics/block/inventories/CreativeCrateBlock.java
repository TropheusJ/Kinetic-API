package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class CreativeCrateBlock extends CrateBlock implements ITE<CreativeCrateTileEntity> {

	public CreativeCrateBlock(c p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CREATIVE_CRATE.create();
	}
	
	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		withTileEntityDo(worldIn, pos, CreativeCrateTileEntity::onPlaced);
	}

	@Override
	public Class<CreativeCrateTileEntity> getTileEntityClass() {
		return CreativeCrateTileEntity.class;
	}
}
