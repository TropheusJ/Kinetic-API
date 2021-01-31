package com.simibubi.kinetic_api.content.contraptions.base;

import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.Direction;

public abstract class HorizontalKineticBlock extends KineticBlock {

	public static final IntProperty<Direction> HORIZONTAL_FACING = BambooLeaves.O;

	public HorizontalKineticBlock(c properties) {
		super(properties);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(HORIZONTAL_FACING);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return this.n()
			.a(HORIZONTAL_FACING, context.f()
				.getOpposite());
	}

	public Direction getPreferredHorizontalFacing(PotionUtil context) {
		Direction prefferedSide = null;
		for (Direction side : Iterate.horizontalDirections) {
			PistonHandler blockState = context.p()
				.d_(context.a()
					.offset(side));
			if (blockState.b() instanceof IRotate) {
				if (((IRotate) blockState.b()).hasShaftTowards(context.p(), context.a()
					.offset(side), blockState, side.getOpposite()))
					if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
						prefferedSide = null;
						break;
					} else {
						prefferedSide = side;
					}
			}
		}
		return prefferedSide;
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		return state.a(HORIZONTAL_FACING, rot.a(state.c(HORIZONTAL_FACING)));
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirrorIn) {
		return state.a(mirrorIn.a(state.c(HORIZONTAL_FACING)));
	}

}
