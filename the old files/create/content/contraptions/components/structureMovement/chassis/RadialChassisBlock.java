package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis;

import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class RadialChassisBlock extends AbstractChassisBlock {

	public static final BedPart STICKY_NORTH = BedPart.a("sticky_north");
	public static final BedPart STICKY_SOUTH = BedPart.a("sticky_south");
	public static final BedPart STICKY_EAST = BedPart.a("sticky_east");
	public static final BedPart STICKY_WEST = BedPart.a("sticky_west");

	public RadialChassisBlock(c properties) {
		super(properties);
		j(n().a(STICKY_EAST, false).a(STICKY_SOUTH, false).a(STICKY_NORTH, false)
				.a(STICKY_WEST, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(STICKY_NORTH, STICKY_EAST, STICKY_SOUTH, STICKY_WEST);
		super.a(builder);
	}

	@Override
	public BedPart getGlueableSide(PistonHandler state, Direction face) {
		Axis axis = state.c(e);

		if (axis == Axis.X) {
			if (face == Direction.NORTH)
				return STICKY_WEST;
			if (face == Direction.SOUTH)
				return STICKY_EAST;
			if (face == Direction.UP)
				return STICKY_NORTH;
			if (face == Direction.DOWN)
				return STICKY_SOUTH;
		}

		if (axis == Axis.Y) {
			if (face == Direction.NORTH)
				return STICKY_NORTH;
			if (face == Direction.SOUTH)
				return STICKY_SOUTH;
			if (face == Direction.EAST)
				return STICKY_EAST;
			if (face == Direction.WEST)
				return STICKY_WEST;
		}

		if (axis == Axis.Z) {
			if (face == Direction.UP)
				return STICKY_NORTH;
			if (face == Direction.DOWN)
				return STICKY_SOUTH;
			if (face == Direction.EAST)
				return STICKY_EAST;
			if (face == Direction.WEST)
				return STICKY_WEST;
		}

		return null;
	}

}
