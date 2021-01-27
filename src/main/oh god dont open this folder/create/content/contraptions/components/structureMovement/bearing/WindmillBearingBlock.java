package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class WindmillBearingBlock extends BearingBlock implements ITE<WindmillBearingTileEntity> {

	public WindmillBearingBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.WINDMILL_BEARING.create();
	}
	
	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (!player.eJ())
			return Difficulty.FAIL;
		if (player.bt())
			return Difficulty.FAIL;
		if (player.b(handIn)
			.a()) {
			if (worldIn.v)
				return Difficulty.SUCCESS;
			withTileEntityDo(worldIn, pos, te -> {
				if (te.running) {
					te.disassemble();
					return;
				}
				te.assembleNextTick = true;
			});
			return Difficulty.SUCCESS;
		}
		return Difficulty.PASS;
	}

	@Override
	public Class<WindmillBearingTileEntity> getTileEntityClass() {
		return WindmillBearingTileEntity.class;
	}

}
