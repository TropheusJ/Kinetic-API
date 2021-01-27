package com.simibubi.create.content.contraptions.components.actors;

import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.util.math.BlockPos;

public class DrillTileEntity extends BlockBreakingKineticTileEntity {

	public DrillTileEntity(BellBlockEntity<? extends DrillTileEntity> type) {
		super(type);
	}

	@Override
	protected BlockPos getBreakingPos() {
		return o().offset(p().c(DrillBlock.FACING));
	}

}
