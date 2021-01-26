package com.simibubi.create.content.logistics.block.belts.tunnel;

import static net.minecraft.block.BeetrootsBlock.a;

import com.simibubi.create.foundation.utility.VoxelShaper;
import dco;
import ddb;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShapes;

public class BeltTunnelShapes {

	private static VoxelShapes block = a(0, -5, 0, 16, 16, 16);

	private static VoxelShaper opening = VoxelShaper.forHorizontal(a(2, -5, 14, 14, 10, 16),
			Direction.SOUTH);

	private static final VoxelShaper STRAIGHT = VoxelShaper.forHorizontalAxis(ddb.a(block,
			ddb.a(opening.get(Direction.SOUTH), opening.get(Direction.NORTH)), dco.g),
			Axis.Z),

			TEE = VoxelShaper.forHorizontal(
					ddb.a(block, ddb.a(opening.get(Direction.NORTH),
							opening.get(Direction.WEST), opening.get(Direction.EAST)), dco.g),
					Direction.SOUTH);

	private static final VoxelShapes CROSS = ddb.a(block,
			ddb.a(opening.get(Direction.SOUTH), opening.get(Direction.NORTH), opening.get(Direction.WEST),
					opening.get(Direction.EAST)),
			dco.g);

	public static VoxelShapes getShape(PistonHandler state) {
		BeltTunnelBlock.Shape shape = state.c(BeltTunnelBlock.SHAPE);
		Direction.Axis axis = state.c(BeltTunnelBlock.HORIZONTAL_AXIS);

		if (shape == BeltTunnelBlock.Shape.CROSS)
			return CROSS;

		if (BeltTunnelBlock.isStraight(state))
			return STRAIGHT.get(axis);

		if (shape == BeltTunnelBlock.Shape.T_LEFT)
			return TEE.get(axis == Direction.Axis.Z ? Direction.EAST : Direction.NORTH);

		if (shape == BeltTunnelBlock.Shape.T_RIGHT)
			return TEE.get(axis == Direction.Axis.Z ? Direction.WEST : Direction.SOUTH);

		// something went wrong
		return ddb.b();
	}
}
