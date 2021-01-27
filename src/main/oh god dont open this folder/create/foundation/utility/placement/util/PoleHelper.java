package com.simibubi.create.foundation.utility.placement.util;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public abstract class PoleHelper<T extends Comparable<T>> implements IPlacementHelper {

	protected final Predicate<PistonHandler> statePredicate;
	protected final IntProperty<T> property;
	protected final Function<PistonHandler, Direction.Axis> axisFunction;

	public PoleHelper(Predicate<PistonHandler> statePredicate, Function<PistonHandler, Direction.Axis> axisFunction, IntProperty<T> property) {
		this.statePredicate = statePredicate;
		this.axisFunction = axisFunction;
		this.property = property;
	}

	public boolean matchesAxis(PistonHandler state, Direction.Axis axis) {
		if (!statePredicate.test(state))
			return false;

		return axisFunction.apply(state) == axis;
	}

	public int attachedPoles(GameMode world, BlockPos pos, Direction direction) {
		BlockPos checkPos = pos.offset(direction);
		PistonHandler state = world.d_(checkPos);
		int count = 0;
		while (matchesAxis(state, direction.getAxis())) {
			count++;
			checkPos = checkPos.offset(direction);
			state = world.d_(checkPos);
		}
		return count;
	}

	@Override
	public Predicate<PistonHandler> getStatePredicate() {
		return this.statePredicate;
	}

	@Override
	public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
		List<Direction> directions = IPlacementHelper.orderedByDistance(pos, ray.e(), dir -> dir.getAxis() == axisFunction.apply(state));
		for (Direction dir : directions) {
			int poles = attachedPoles(world, pos, dir);
			BlockPos newPos = pos.offset(dir, poles + 1);
			PistonHandler newState = world.d_(newPos);

			if (newState.c().e())
				return PlacementOffset.success(newPos, bState -> bState.a(property, state.c(property)));

		}

		return PlacementOffset.fail();
	}

	@Override
	public void renderAt(BlockPos pos, PistonHandler state, dcg ray, PlacementOffset offset) {
		EntityHitResult centerOffset = EntityHitResult.b(ray.b().getVector()).a(.3);
		IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos).e(centerOffset), VecHelper.getCenterOf(offset.getPos()).e(centerOffset), ray.b(), 0.75D);
	}
}
