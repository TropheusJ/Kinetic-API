package com.simibubi.kinetic_api.content.contraptions.relays.gearbox;

import com.simibubi.kinetic_api.content.contraptions.relays.encased.SplitShaftTileEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.util.math.Direction;

public class GearshiftTileEntity extends SplitShaftTileEntity {

	public GearshiftTileEntity(BellBlockEntity<? extends GearshiftTileEntity> type) {
		super(type);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && p().c(BambooLeaves.w))
				return -1;
		}
		return 1;
	}
	
}
