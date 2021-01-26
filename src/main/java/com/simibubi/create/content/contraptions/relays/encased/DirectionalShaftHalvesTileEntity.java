package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DirectionalShaftHalvesTileEntity extends KineticTileEntity {

	public DirectionalShaftHalvesTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
	}

	public Direction getSourceFacing() {
		BlockPos localSource = source.subtract(o());
		return Direction.getFacing(localSource.getX(), localSource.getY(), localSource.getZ());
	}

}
