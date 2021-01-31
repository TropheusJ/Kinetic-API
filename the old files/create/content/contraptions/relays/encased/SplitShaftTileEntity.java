package com.simibubi.kinetic_api.content.contraptions.relays.encased;

import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.util.math.Direction;

public abstract class SplitShaftTileEntity extends DirectionalShaftHalvesTileEntity {

	public SplitShaftTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
