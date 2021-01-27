package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllTileEntities;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.world.MobSpawnerLogic;

public class HarvesterBlock extends AttachedActorBlock {

	public HarvesterBlock(c p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return new HarvesterTileEntity(AllTileEntities.HARVESTER.get());
	}
}
