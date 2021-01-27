package com.simibubi.create.content.contraptions.base;

import cef;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public abstract class RotatedPillarKineticBlock extends KineticBlock {

	public static final DirectionProperty<Direction.Axis> AXIS = BambooLeaves.F;

	public RotatedPillarKineticBlock(c properties) {
		super(properties);
		this.j(this.n()
			.a(AXIS, Direction.Axis.Y));
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		switch (rot) {
		case d:
		case field_26442:
			switch ((Direction.Axis) state.c(AXIS)) {
			case X:
				return state.a(AXIS, Direction.Axis.Z);
			case Z:
				return state.a(AXIS, Direction.Axis.X);
			default:
				return state;
			}
		default:
			return state;
		}
	}

	public static Axis getPreferredAxis(PotionUtil context) {
		Axis prefferedAxis = null;
		for (Direction side : Iterate.directions) {
			PistonHandler blockState = context.p()
				.d_(context.a()
					.offset(side));
			if (blockState.b() instanceof IRotate) {
				if (((IRotate) blockState.b()).hasShaftTowards(context.p(), context.a()
					.offset(side), blockState, side.getOpposite()))
					if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
						prefferedAxis = null;
						break;
					} else {
						prefferedAxis = side.getAxis();
					}
			}
		}
		return prefferedAxis;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(AXIS);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Axis preferredAxis = getPreferredAxis(context);
		if (preferredAxis != null && (context.n() == null || !context.n()
			.bt()))
			return this.n()
				.a(AXIS, preferredAxis);
		return this.n()
			.a(AXIS, preferredAxis != null && context.n()
				.bt() ? context.j()
					.getAxis()
					: context.d()
						.getAxis());
	}
}
