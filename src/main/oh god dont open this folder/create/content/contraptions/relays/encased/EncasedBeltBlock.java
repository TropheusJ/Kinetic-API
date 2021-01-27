package com.simibubi.create.content.contraptions.relays.encased;

import bnx;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.MobSpawnerLogic;

public class EncasedBeltBlock extends RotatedPillarKineticBlock {

	public static final IntProperty<Part> PART = DirectionProperty.a("part", Part.class);
	public static final BedPart CONNECTED_ALONG_FIRST_COORDINATE =
		DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

	public EncasedBeltBlock(c properties) {
		super(properties);
		j(n().a(PART, Part.NONE));
	}

	@Override
	public boolean shouldCheckWeakPower(PistonHandler state, ItemConvertible world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.a;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(PART, CONNECTED_ALONG_FIRST_COORDINATE));
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Axis placedAxis = context.d()
			.getAxis();
		Axis axis = context.n() != null && context.n()
			.bt() ? placedAxis : getPreferredAxis(context);
		if (axis == null)
			axis = placedAxis;

		PistonHandler state = n().a(AXIS, axis);
		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == axis)
				continue;
			BlockPos pos = context.a();
			BlockPos offset = pos.offset(facing);
			state = a(state, facing, context.p()
				.d_(offset), context.p(), pos, offset);
		}
		return state;
	}

	@Override
	public PistonHandler a(PistonHandler stateIn, Direction face, PistonHandler neighbour, GrassColors worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		Part part = stateIn.c(PART);
		Axis axis = stateIn.c(AXIS);
		boolean connectionAlongFirst = stateIn.c(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis connectionAxis =
			connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);

		Axis faceAxis = face.getAxis();
		boolean facingAlongFirst = axis == Axis.X ? faceAxis.isVertical() : faceAxis == Axis.X;
		boolean positive = face.getDirection() == AxisDirection.POSITIVE;

		if (axis == faceAxis)
			return stateIn;

		if (!(neighbour.b() instanceof EncasedBeltBlock)) {
			if (facingAlongFirst != connectionAlongFirst || part == Part.NONE)
				return stateIn;
			if (part == Part.MIDDLE)
				return stateIn.a(PART, positive ? Part.END : Part.START);
			if ((part == Part.START) == positive)
				return stateIn.a(PART, Part.NONE);
			return stateIn;
		}

		Part otherPart = neighbour.c(PART);
		Axis otherAxis = neighbour.c(AXIS);
		boolean otherConnection = neighbour.c(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis otherConnectionAxis =
			otherConnection ? (otherAxis == Axis.X ? Axis.Y : Axis.X) : (otherAxis == Axis.Z ? Axis.Y : Axis.Z);

		if (neighbour.c(AXIS) == faceAxis)
			return stateIn;
		if (otherPart != Part.NONE && otherConnectionAxis != faceAxis)
			return stateIn;

		if (part == Part.NONE) {
			part = positive ? Part.START : Part.END;
			connectionAlongFirst = axis == Axis.X ? faceAxis.isVertical() : faceAxis == Axis.X;
		} else if (connectionAxis != faceAxis) {
			return stateIn;
		}

		if ((part == Part.START) != positive)
			part = Part.MIDDLE;

		return stateIn.a(PART, part)
			.a(CONNECTED_ALONG_FIRST_COORDINATE, connectionAlongFirst);
	}

	@Override
	public PistonHandler updateAfterWrenched(PistonHandler newState, bnx context) {
		BellBlock.FACING.n()
			.a(context.p(), context.a(), 1);
		Axis axis = newState.c(AXIS);
		newState = n().a(AXIS, axis);
		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == axis)
				continue;
			BlockPos pos = context.a();
			BlockPos offset = pos.offset(facing);
			newState = a(newState, facing, context.p()
				.d_(offset), context.p(), pos, offset);
		}
		newState.a(context.p(), context.a(), 1 | 2);
		return newState;
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() == state.c(AXIS);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(AXIS);
	}

	public static boolean areBlocksConnected(PistonHandler state, PistonHandler other, Direction facing) {
		Part part = state.c(PART);
		Axis axis = state.c(AXIS);
		boolean connectionAlongFirst = state.c(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis connectionAxis =
			connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);

		Axis otherAxis = other.c(AXIS);
		boolean otherConnection = other.c(CONNECTED_ALONG_FIRST_COORDINATE);
		Axis otherConnectionAxis =
			otherConnection ? (otherAxis == Axis.X ? Axis.Y : Axis.X) : (otherAxis == Axis.Z ? Axis.Y : Axis.Z);

		if (otherConnectionAxis != connectionAxis)
			return false;
		if (facing.getAxis() != connectionAxis)
			return false;
		if (facing.getDirection() == AxisDirection.POSITIVE && (part == Part.MIDDLE || part == Part.START))
			return true;
		if (facing.getDirection() == AxisDirection.NEGATIVE && (part == Part.MIDDLE || part == Part.END))
			return true;

		return false;
	}

	public static float getRotationSpeedModifier(KineticTileEntity from, KineticTileEntity to) {
		float fromMod = 1;
		float toMod = 1;
		if (from instanceof AdjustablePulleyTileEntity)
			fromMod = ((AdjustablePulleyTileEntity) from).getModifier();
		if (to instanceof AdjustablePulleyTileEntity)
			toMod = ((AdjustablePulleyTileEntity) to).getModifier();
		return fromMod / toMod;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	public enum Part implements SmoothUtil {
		START, MIDDLE, END, NONE;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

}
