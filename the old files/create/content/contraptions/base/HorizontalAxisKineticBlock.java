package com.simibubi.kinetic_api.content.contraptions.base;

import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public abstract class HorizontalAxisKineticBlock extends KineticBlock {

	public static final IntProperty<Axis> HORIZONTAL_AXIS = BambooLeaves.E;

	public HorizontalAxisKineticBlock(c properties) {
		super(properties);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(HORIZONTAL_AXIS);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Axis preferredAxis = getPreferredHorizontalAxis(context);
		if (preferredAxis != null)
			return this.n().a(HORIZONTAL_AXIS, preferredAxis);
		return this.n().a(HORIZONTAL_AXIS, context.f().rotateYClockwise().getAxis());
	}

	public static Axis getPreferredHorizontalAxis(PotionUtil context) {
		Direction prefferedSide = null;
		for (Direction side : Iterate.horizontalDirections) {
			PistonHandler blockState = context.p().d_(context.a().offset(side));
			if (blockState.b() instanceof IRotate) {
				if (((IRotate) blockState.b()).hasShaftTowards(context.p(), context.a().offset(side),
						blockState, side.getOpposite()))
					if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
						prefferedSide = null;
						break;
					} else {
						prefferedSide = side;
					}
			}
		}
		return prefferedSide == null ? null : prefferedSide.getAxis();
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_AXIS);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() == state.c(HORIZONTAL_AXIS);
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		Axis axis = state.c(HORIZONTAL_AXIS);
		return state.a(HORIZONTAL_AXIS,
				rot.a(Direction.get(AxisDirection.POSITIVE, axis)).getAxis());
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirrorIn) {
		return state;
	}

}
