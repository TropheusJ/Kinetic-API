package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Optional;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.Lang;

public class BracketBlock extends ProperDirectionalBlock {

	public static final BedPart AXIS_ALONG_FIRST_COORDINATE =
		DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
	public static final DirectionProperty<BracketType> TYPE = DirectionProperty.a("type", BracketType.class);

	public static enum BracketType implements SmoothUtil {
		PIPE, COG, SHAFT;

		@Override
		public String a() {
			return Lang.asId(name());
		}

	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(AXIS_ALONG_FIRST_COORDINATE)
			.a(TYPE));
	}

	public BracketBlock(c p_i48415_1_) {
		super(p_i48415_1_);
	}

	public Optional<PistonHandler> getSuitableBracket(PistonHandler blockState, Direction direction) {
		if (blockState.b() instanceof AbstractShaftBlock)
			return getSuitableBracket(blockState.c(RotatedPillarKineticBlock.AXIS), direction,
				blockState.b() instanceof CogWheelBlock ? BracketType.COG : BracketType.SHAFT);
		return getSuitableBracket(FluidPropagator.getStraightPipeAxis(blockState), direction, BracketType.PIPE);
	}

	private Optional<PistonHandler> getSuitableBracket(Axis targetBlockAxis, Direction direction, BracketType type) {
		Axis axis = direction.getAxis();
		if (targetBlockAxis == null || targetBlockAxis == axis)
			return Optional.empty();

		boolean alongFirst = axis != Axis.Z ? targetBlockAxis == Axis.Z : targetBlockAxis == Axis.Y;
		return Optional.of(n().a(TYPE, type)
			.a(SHAPE, direction)
			.a(AXIS_ALONG_FIRST_COORDINATE, !alongFirst));
	}

}
