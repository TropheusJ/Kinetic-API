package com.simibubi.kinetic_api.content.logistics.block.inventories;

import java.util.List;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;

public abstract class CrateTileEntity extends SmartTileEntity {

	public CrateTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	public boolean isDoubleCrate() {
		return p().c(AdjustableCrateBlock.DOUBLE);
	}

	public boolean isSecondaryCrate() {
		if (!n())
			return false;
		if (!(p().b() instanceof CrateBlock))
			return false;
		return isDoubleCrate() && getFacing().getDirection() == AxisDirection.NEGATIVE;
	}
	
	public Direction getFacing() {
		return p().c(AdjustableCrateBlock.SHAPE);
	}

}
