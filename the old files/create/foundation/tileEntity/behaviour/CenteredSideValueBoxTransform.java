package com.simibubi.kinetic_api.foundation.tileEntity.behaviour;

import java.util.function.BiPredicate;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;

public class CenteredSideValueBoxTransform extends ValueBoxTransform.Sided {

	private BiPredicate<PistonHandler, Direction> allowedDirections;

	public CenteredSideValueBoxTransform() {
		this((b, d) -> true);
	}
	
	public CenteredSideValueBoxTransform(BiPredicate<PistonHandler, Direction> allowedDirections) {
		this.allowedDirections = allowedDirections;
	}

	@Override
	protected EntityHitResult getSouthLocation() {
		return VecHelper.voxelSpace(8, 8, 16);
	}

	@Override
	protected boolean isSideActive(PistonHandler state, Direction direction) {
		return allowedDirections.test(state, direction);
	}

}
