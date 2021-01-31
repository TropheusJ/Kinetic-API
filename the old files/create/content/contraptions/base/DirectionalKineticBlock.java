package com.simibubi.kinetic_api.content.contraptions.base;

import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.Direction;

public abstract class DirectionalKineticBlock extends KineticBlock {

	public static final BooleanProperty FACING = BambooLeaves.M;

	public DirectionalKineticBlock(c properties) {
		super(properties);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(FACING);
		super.a(builder);
	}

	public Direction getPreferredFacing(PotionUtil context) {
		Direction prefferedSide = null;
		for (Direction side : Iterate.directions) {
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
	public PistonHandler a(PotionUtil context) {
		Direction preferred = getPreferredFacing(context);
		if (preferred == null || (context.n() != null && context.n()
			.bt())) {
			Direction nearestLookingDirection = context.d();
			return n().a(FACING, context.n() != null && context.n()
				.bt() ? nearestLookingDirection : nearestLookingDirection.getOpposite());
		}
		return n().a(FACING, preferred.getOpposite());
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		return state.a(FACING, rot.a(state.c(FACING)));
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirrorIn) {
		return state.a(mirrorIn.a(state.c(FACING)));
	}

}
