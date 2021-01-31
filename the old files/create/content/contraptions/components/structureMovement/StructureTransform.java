package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import static net.minecraft.block.EndRodBlock.u;
import static net.minecraft.block.enums.BambooLeaves.F;
import static net.minecraft.block.enums.BambooLeaves.M;
import static net.minecraft.block.enums.BambooLeaves.O;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.DirectionHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class StructureTransform {

	// Assuming structures cannot be rotated around multiple axes at once
	RespawnAnchorBlock rotation;
	int angle;
	Axis rotationAxis;
	BlockPos offset;

	private StructureTransform(BlockPos offset, int angle, Axis axis, RespawnAnchorBlock rotation) {
		this.offset = offset;
		this.angle = angle;
		rotationAxis = axis;
		this.rotation = rotation;
	}

	public StructureTransform(BlockPos offset, float xRotation, float yRotation, float zRotation) {
		this.offset = offset;
		if (xRotation != 0) {
			rotationAxis = Axis.X;
			angle = (int) (Math.round(xRotation / 90) * 90);
		}
		if (yRotation != 0) {
			rotationAxis = Axis.Y;
			angle = (int) (Math.round(yRotation / 90) * 90);
		}
		if (zRotation != 0) {
			rotationAxis = Axis.Z;
			angle = (int) (Math.round(zRotation / 90) * 90);
		}

		angle %= 360;
		if (angle < -90)
			angle += 360;

		this.rotation = RespawnAnchorBlock.CHARGES;
		if (angle == -90 || angle == 270)
			this.rotation = RespawnAnchorBlock.field_26442;
		if (angle == 90)
			this.rotation = RespawnAnchorBlock.d;
		if (angle == 180)
			this.rotation = RespawnAnchorBlock.field_26443;

	}

	public EntityHitResult apply(EntityHitResult localVec) {
		EntityHitResult vec = localVec;
		if (rotationAxis != null)
			vec = VecHelper.rotateCentered(vec, angle, rotationAxis);
		vec = vec.e(EntityHitResult.b(offset));
		return vec;
	}

	public BlockPos apply(BlockPos localPos) {
		EntityHitResult vec = VecHelper.getCenterOf(localPos);
		if (rotationAxis != null)
			vec = VecHelper.rotateCentered(vec, angle, rotationAxis);
		localPos = new BlockPos(vec);
		return localPos.add(offset);
	}

	/**
	 * Minecraft does not support blockstate rotation around axes other than y. Add
	 * specific cases here for blockstates, that should react to rotations around
	 * horizontal axes
	 */
	public PistonHandler apply(PistonHandler state) {
		BeetrootsBlock block = state.b();

		if (rotationAxis == Axis.Y) {
			if (block instanceof BedBlock) {
				if (state.c(BambooLeaves.R) == WallMountLocation.name) {
					state = state.a(BambooLeaves.R, WallMountLocation.SINGLE_WALL);
				}
				return state.a(EndRodBlock.aq,
					rotation.a(state.c(EndRodBlock.aq)));
			}
			return state.a(rotation);
		}

		if (block instanceof AbstractChassisBlock)
			return rotateChassis(state);

		if (block instanceof EndRodBlock) {
			Direction stateFacing = state.c(EndRodBlock.aq);
			BlockPredicate stateFace = state.c(u);
			Direction forcedAxis = rotationAxis == Axis.Z ? Direction.EAST : Direction.SOUTH;

			if (stateFacing.getAxis() == rotationAxis && stateFace == BlockPredicate.b)
				return state;

			for (int i = 0; i < rotation.ordinal(); i++) {
				stateFace = state.c(u);
				stateFacing = state.c(EndRodBlock.aq);

				boolean b = state.c(u) == BlockPredicate.c;
				state = state.a(O, b ? forcedAxis : forcedAxis.getOpposite());

				if (stateFace != BlockPredicate.b) {
					state = state.a(u, BlockPredicate.b);
					continue;
				}

				if (stateFacing.getDirection() == AxisDirection.POSITIVE) {
					state = state.a(u, BlockPredicate.block);
					continue;
				}
				state = state.a(u, BlockPredicate.c);
			}

			return state;
		}

		boolean halfTurn = rotation == RespawnAnchorBlock.field_26443;
		if (block instanceof SpreadableBlock) {
			state = transformStairs(state, halfTurn);
			return state;
		}

		if (AllBlocks.BELT.has(state)) {
			state = transformBelt(state, halfTurn);
			return state;
		}

		if (BlockHelper.hasBlockStateProperty(state, M)) {
			Direction newFacing = transformFacing(state.c(M));
			if (BlockHelper.hasBlockStateProperty(state, DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE)) {
				if (rotationAxis == newFacing.getAxis() && rotation.ordinal() % 2 == 1)
					state = state.a(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			}
			state = state.a(M, newFacing);

		} else if (BlockHelper.hasBlockStateProperty(state, F)) {
			state = state.a(F, transformAxis(state.c(F)));

		} else if (halfTurn) {

			if (BlockHelper.hasBlockStateProperty(state, M)) {
				Direction stateFacing = state.c(M);
				if (stateFacing.getAxis() == rotationAxis)
					return state;
			}

			if (BlockHelper.hasBlockStateProperty(state, O)) {
				Direction stateFacing = state.c(O);
				if (stateFacing.getAxis() == rotationAxis)
					return state;
			}

			state = state.a(rotation);
			if (BlockHelper.hasBlockStateProperty(state, AbstractSignBlock.WATERLOGGED) && state.c(AbstractSignBlock.WATERLOGGED) != Property.hashCodeCache)
				state = state.a(AbstractSignBlock.WATERLOGGED,
					state.c(AbstractSignBlock.WATERLOGGED) == Property.name ? Property.type : Property.name);
		}

		return state;
	}

	protected PistonHandler transformStairs(PistonHandler state, boolean halfTurn) {
		if (state.c(SpreadableBlock.a)
			.getAxis() != rotationAxis) {
			for (int i = 0; i < rotation.ordinal(); i++) {
				Direction direction = state.c(SpreadableBlock.a);
				DoorHinge half = state.c(SpreadableBlock.b);
				if (direction.getDirection() == AxisDirection.POSITIVE ^ half == DoorHinge.BOTTOM
					^ direction.getAxis() == Axis.Z)
					state = state.a(SpreadableBlock.b);
				else
					state = state.a(SpreadableBlock.a, direction.getOpposite());
			}
		} else {
			if (halfTurn) {
				state = state.a(SpreadableBlock.b);
			}
		}
		return state;
	}

	protected PistonHandler transformBelt(PistonHandler state, boolean halfTurn) {
		Direction initialDirection = state.c(BeltBlock.HORIZONTAL_FACING);
		boolean diagonal =
			state.c(BeltBlock.SLOPE) == BeltSlope.DOWNWARD || state.c(BeltBlock.SLOPE) == BeltSlope.UPWARD;

		if (!diagonal) {
			for (int i = 0; i < rotation.ordinal(); i++) {
				Direction direction = state.c(BeltBlock.HORIZONTAL_FACING);
				BeltSlope slope = state.c(BeltBlock.SLOPE);
				boolean vertical = slope == BeltSlope.VERTICAL;
				boolean horizontal = slope == BeltSlope.HORIZONTAL;
				boolean sideways = slope == BeltSlope.SIDEWAYS;

				Direction newDirection = direction.getOpposite();
				BeltSlope newSlope = BeltSlope.VERTICAL;

				if (vertical) {
					if (direction.getAxis() == rotationAxis) {
						newDirection = direction.rotateYCounterclockwise();
						newSlope = BeltSlope.SIDEWAYS;
					} else {
						newSlope = BeltSlope.HORIZONTAL;
						newDirection = direction;
						if (direction.getAxis() == Axis.Z)
							newDirection = direction.getOpposite();
					}
				}

				if (sideways) {
					newDirection = direction;
					if (direction.getAxis() == rotationAxis)
						newSlope = BeltSlope.HORIZONTAL;
					else
						newDirection = direction.rotateYCounterclockwise();
				}

				if (horizontal) {
					newDirection = direction;
					if (direction.getAxis() == rotationAxis)
						newSlope = BeltSlope.SIDEWAYS;
					else if (direction.getAxis() != Axis.Z)
						newDirection = direction.getOpposite();
				}

				state = state.a(BeltBlock.HORIZONTAL_FACING, newDirection);
				state = state.a(BeltBlock.SLOPE, newSlope);
			}

		} else if (initialDirection.getAxis() != rotationAxis) {
			for (int i = 0; i < rotation.ordinal(); i++) {
				Direction direction = state.c(BeltBlock.HORIZONTAL_FACING);
				Direction newDirection = direction.getOpposite();
				BeltSlope slope = state.c(BeltBlock.SLOPE);
				boolean upward = slope == BeltSlope.UPWARD;
				boolean downward = slope == BeltSlope.DOWNWARD;

				// Rotate diagonal
				if (direction.getDirection() == AxisDirection.POSITIVE ^ downward ^ direction.getAxis() == Axis.Z) {
					state = state.a(BeltBlock.SLOPE, upward ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
				} else {
					state = state.a(BeltBlock.HORIZONTAL_FACING, newDirection);
				}
			}

		} else if (halfTurn) {
			Direction direction = state.c(BeltBlock.HORIZONTAL_FACING);
			Direction newDirection = direction.getOpposite();
			BeltSlope slope = state.c(BeltBlock.SLOPE);
			boolean vertical = slope == BeltSlope.VERTICAL;

			if (diagonal) {
				state = state.a(BeltBlock.SLOPE, slope == BeltSlope.UPWARD ? BeltSlope.DOWNWARD
					: slope == BeltSlope.DOWNWARD ? BeltSlope.UPWARD : slope);
			} else if (vertical) {
				state = state.a(BeltBlock.HORIZONTAL_FACING, newDirection);
			}
		}
		return state;
	}

	public Axis transformAxis(Axis axisIn) {
		Direction facing = Direction.get(AxisDirection.POSITIVE, axisIn);
		facing = transformFacing(facing);
		Axis axis = facing.getAxis();
		return axis;
	}

	public Direction transformFacing(Direction facing) {
		for (int i = 0; i < rotation.ordinal(); i++)
			facing = DirectionHelper.rotateAround(facing, rotationAxis);
		return facing;
	}

	private PistonHandler rotateChassis(PistonHandler state) {
		if (rotation == RespawnAnchorBlock.CHARGES)
			return state;

		PistonHandler rotated = state.a(F, transformAxis(state.c(F)));
		AbstractChassisBlock block = (AbstractChassisBlock) state.b();

		for (Direction face : Iterate.directions) {
			BedPart glueableSide = block.getGlueableSide(rotated, face);
			if (glueableSide != null)
				rotated = rotated.a(glueableSide, false);
		}

		for (Direction face : Iterate.directions) {
			BedPart glueableSide = block.getGlueableSide(state, face);
			if (glueableSide == null || !state.c(glueableSide))
				continue;
			Direction rotatedFacing = transformFacing(face);
			BedPart rotatedGlueableSide = block.getGlueableSide(rotated, rotatedFacing);
			if (rotatedGlueableSide != null)
				rotated = rotated.a(rotatedGlueableSide, true);
		}

		return rotated;
	}

	public static StructureTransform fromBuffer(PacketByteBuf buffer) {
		BlockPos readBlockPos = buffer.readBlockPos();
		int readAngle = buffer.readInt();
		int axisIndex = buffer.readVarInt();
		int rotationIndex = buffer.readVarInt();
		return new StructureTransform(readBlockPos, readAngle, axisIndex == -1 ? null : Axis.values()[axisIndex],
			rotationIndex == -1 ? null : RespawnAnchorBlock.values()[rotationIndex]);
	}

	public void writeToBuffer(PacketByteBuf buffer) {
		buffer.writeBlockPos(offset);
		buffer.writeInt(angle);
		buffer.writeVarInt(rotationAxis == null ? -1 : rotationAxis.ordinal());
		buffer.writeVarInt(rotation == null ? -1 : rotation.ordinal());
	}

}
