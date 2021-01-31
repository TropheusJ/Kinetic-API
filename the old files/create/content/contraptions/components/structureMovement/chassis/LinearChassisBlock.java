package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis;

import bqx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllSpriteShifts;
import com.simibubi.kinetic_api.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.kinetic_api.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class LinearChassisBlock extends AbstractChassisBlock {

	public static final BedPart STICKY_TOP = BedPart.a("sticky_top");
	public static final BedPart STICKY_BOTTOM = BedPart.a("sticky_bottom");

	public LinearChassisBlock(c properties) {
		super(properties);
		j(n().a(STICKY_TOP, false)
			.a(STICKY_BOTTOM, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(STICKY_TOP, STICKY_BOTTOM);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		BlockPos placedOnPos = context.a()
			.offset(context.j()
				.getOpposite());
		PistonHandler blockState = context.p()
			.d_(placedOnPos);

		if (context.n() == null || !context.n()
			.bt()) {
			if (isChassis(blockState))
				return n().a(e, blockState.c(e));
			return n().a(e, context.d()
				.getAxis());
		}
		return super.a(context);
	}

	@Override
	public BedPart getGlueableSide(PistonHandler state, Direction face) {
		if (face.getAxis() != state.c(e))
			return null;
		return face.getDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
	}

	public static boolean isChassis(PistonHandler state) {
		return AllBlocks.LINEAR_CHASSIS.has(state) || AllBlocks.SECONDARY_LINEAR_CHASSIS.has(state);
	}

	public static boolean sameKind(PistonHandler state1, PistonHandler state2) {
		return state1.b() == state2.b();
	}

	public static class ChassisCTBehaviour extends ConnectedTextureBehaviour {

		@Override
		public CTSpriteShiftEntry get(PistonHandler state, Direction direction) {
			BeetrootsBlock block = state.b();
			BedPart glueableSide = ((LinearChassisBlock) block).getGlueableSide(state, direction);
			if (glueableSide == null)
				return null;
			return state.c(glueableSide) ? AllSpriteShifts.CHASSIS_STICKY : AllSpriteShifts.CHASSIS;
		}

		@Override
		public boolean reverseUVs(PistonHandler state, Direction face) {
			Axis axis = state.c(e);
			if (axis.isHorizontal() && (face.getDirection() == AxisDirection.POSITIVE))
				return true;
			return super.reverseUVs(state, face);
		}

		@Override
		public boolean connectsTo(PistonHandler state, PistonHandler other, bqx reader, BlockPos pos,
			BlockPos otherPos, Direction face) {
			return sameKind(state, other) && state.c(e) == other.c(e);
		}

	}

}
