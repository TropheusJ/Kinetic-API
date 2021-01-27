package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.util.math.Direction;

public class ClutchTileEntity extends SplitShaftTileEntity {

	public ClutchTileEntity(BellBlockEntity<? extends ClutchTileEntity> type) {
		super(type);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && p().c(BambooLeaves.w))
				return 0;
		}
		return 1;
	}

}
