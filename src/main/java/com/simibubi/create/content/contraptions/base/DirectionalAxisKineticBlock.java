package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public abstract class DirectionalAxisKineticBlock extends DirectionalKineticBlock {

	public static final BedPart AXIS_ALONG_FIRST_COORDINATE = BedPart.a("axis_along_first");

	public DirectionalAxisKineticBlock(c properties) {
		super(properties);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(AXIS_ALONG_FIRST_COORDINATE);
		super.a(builder);
	}

	protected Direction getFacingForPlacement(PotionUtil context) {
		Direction facing = context.d()
			.getOpposite();
		if (context.n() != null && context.n()
			.bt())
			facing = facing.getOpposite();
		return facing;
	}

	protected boolean getAxisAlignmentForPlacement(PotionUtil context) {
		return context.f()
			.getAxis() == Axis.X;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction facing = getFacingForPlacement(context);
		BlockPos pos = context.a();
		GameMode world = context.p();
		boolean alongFirst = false;
		Axis faceAxis = facing.getAxis();

		if (faceAxis.isHorizontal()) {
			alongFirst = faceAxis == Axis.Z;
			Direction positivePerpendicular = DirectionHelper.getPositivePerpendicular(faceAxis);

			boolean shaftAbove = prefersConnectionTo(world, pos, Direction.UP, true);
			boolean shaftBelow = prefersConnectionTo(world, pos, Direction.DOWN, true);
			boolean preferLeft = prefersConnectionTo(world, pos, positivePerpendicular, false);
			boolean preferRight = prefersConnectionTo(world, pos, positivePerpendicular.getOpposite(), false);

			if (shaftAbove || shaftBelow || preferLeft || preferRight)
				alongFirst = faceAxis == Axis.X;
		}

		if (faceAxis.isVertical()) {
			alongFirst = getAxisAlignmentForPlacement(context);
			Direction prefferedSide = null;

			for (Direction side : Iterate.horizontalDirections) {
				if (!prefersConnectionTo(world, pos, side, true)
					&& !prefersConnectionTo(world, pos, side.rotateYClockwise(), false))
					continue;
				if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
					prefferedSide = null;
					break;
				}
				prefferedSide = side;
			}

			if (prefferedSide != null)
				alongFirst = prefferedSide.getAxis() == Axis.X;
		}

		return this.n()
			.a(FACING, facing)
			.a(AXIS_ALONG_FIRST_COORDINATE, alongFirst);
	}

	protected boolean prefersConnectionTo(ItemConvertible reader, BlockPos pos, Direction facing, boolean shaftAxis) {
		if (!shaftAxis)
			return false;
		BlockPos neighbourPos = pos.offset(facing);
		PistonHandler blockState = reader.d_(neighbourPos);
		BeetrootsBlock block = blockState.b();
		return block instanceof IRotate
			&& ((IRotate) block).hasShaftTowards(reader, neighbourPos, blockState, facing.getOpposite());
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		Axis pistonAxis = state.c(FACING)
			.getAxis();
		boolean alongFirst = state.c(AXIS_ALONG_FIRST_COORDINATE);

		if (pistonAxis == Axis.X)
			return alongFirst ? Axis.Y : Axis.Z;
		if (pistonAxis == Axis.Y)
			return alongFirst ? Axis.X : Axis.Z;
		if (pistonAxis == Axis.Z)
			return alongFirst ? Axis.X : Axis.Y;

		throw new IllegalStateException("Unknown axis??");
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		if (rot.ordinal() % 2 == 1)
			state = state.a(AXIS_ALONG_FIRST_COORDINATE);
		return super.a(state, rot);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

}
