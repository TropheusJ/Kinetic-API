package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing;

import com.simibubi.kinetic_api.content.contraptions.base.DirectionalKineticBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public abstract class BearingBlock extends DirectionalKineticBlock {

	public BearingBlock(c properties) {
		super(properties);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(FACING).getOpposite();
	}
	
	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(FACING).getAxis();
	}

	@Override
	public boolean showCapacityWithAnnotation() {
		return true;
	}

}
