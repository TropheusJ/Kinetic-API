package com.simibubi.create.content.contraptions.relays.elementary;

import static com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock.AXIS;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class CogwheelBlockItem extends BannerItem {

	boolean large;

	private final int placementHelperId;
	private final int integratedCogHelperId;

	public CogwheelBlockItem(CogWheelBlock block, a builder) {
		super(block, builder);
		large = block.isLarge;

		placementHelperId = PlacementHelpers.register(large ? new LargeCogHelper() : new SmallCogHelper());
		integratedCogHelperId = large ? PlacementHelpers.register(new IntegratedCogHelper()) : -1;
	}

	@Override
	public Difficulty a(PotionUtil context) {
		GameMode world = context.p();
		BlockPos pos = context.a()
			.offset(context.j()
				.getOpposite());
		PistonHandler state = world.d_(pos);

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		PlayerAbilities player = context.n();

		if (helper.matchesState(state)) {
			PlacementOffset offset = helper.getOffset(world, state, pos,
				new dcg(context.k(), context.j(), pos, true));

			if (!offset.isReplaceable(world))
				return super.a(context);

			offset.placeInWorld(world, this, player, context.m());
			triggerShiftingGearsAdvancement(world, new BlockPos(offset.getPos()), offset.getTransform()
				.apply(e().n()), player);

			return Difficulty.SUCCESS;
		}

		if (integratedCogHelperId != -1) {
			helper = PlacementHelpers.get(integratedCogHelperId);

			if (helper.matchesState(state)) {
				PlacementOffset offset = helper.getOffset(world, state, pos,
					new dcg(context.k(), context.j(), pos, true));

				if (!offset.isReplaceable(world))
					return super.a(context);

				offset.placeInWorld(world, this, player, context.m());
				triggerShiftingGearsAdvancement(world, new BlockPos(offset.getPos()), offset.getTransform()
					.apply(e().n()), player);

				return Difficulty.SUCCESS;
			}
		}

		return super.a(context);
	}

	@Override
	// Trigger cogwheel criterion
	protected boolean a(PotionUtil context, PistonHandler state) {
		triggerShiftingGearsAdvancement(context.p(), context.a(), state, context.n());
		return super.a(context, state);
	}

	protected void triggerShiftingGearsAdvancement(GameMode world, BlockPos pos, PistonHandler state, PlayerAbilities player) {
		if (world.v || player == null)
			return;

		Axis axis = state.c(CogWheelBlock.AXIS);
		for (Axis perpendicular1 : Iterate.axes) {
			if (perpendicular1 == axis)
				continue;
			Direction d1 = Direction.get(AxisDirection.POSITIVE, perpendicular1);
			for (Axis perpendicular2 : Iterate.axes) {
				if (perpendicular1 == perpendicular2)
					continue;
				if (axis == perpendicular2)
					continue;
				Direction d2 = Direction.get(AxisDirection.POSITIVE, perpendicular2);
				for (int offset1 : Iterate.positiveAndNegative) {
					for (int offset2 : Iterate.positiveAndNegative) {
						BlockPos connectedPos = pos.offset(d1, offset1)
							.offset(d2, offset2);
						PistonHandler blockState = world.d_(connectedPos);
						if (!(blockState.b() instanceof CogWheelBlock))
							continue;
						if (blockState.c(CogWheelBlock.AXIS) != axis)
							continue;
						if (AllBlocks.LARGE_COGWHEEL.has(blockState) == large)
							continue;
						AllTriggers.triggerFor(AllTriggers.SHIFTING_GEARS, player);
					}
				}
			}
		}
	}

	@MethodsReturnNonnullByDefault
	private static class SmallCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemCooldownManager> getItemPredicate() {
			return AllBlocks.COGWHEEL::isIn;
		}

		@Override
		public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (!((CogWheelBlock) state.b()).isLarge) {
				List<Direction> directions =
					IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.e(), state.c(AXIS));

				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir);

					if (hasLargeCogwheelNeighbor(world, newPos, state.c(AXIS)))
						continue;

					if (!world.d_(newPos)
						.c()
						.e())
						continue;

					return PlacementOffset.success(newPos, s -> s.a(AXIS, state.c(AXIS)));

				}

				return PlacementOffset.fail();
			}

			return super.getOffset(world, state, pos, ray);
		}

		@Override
		public void renderAt(BlockPos pos, PistonHandler state, dcg ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()),
				Direction.get(Direction.AxisDirection.POSITIVE, state.c(AXIS)),
				((CogWheelBlock) state.b()).isLarge ? 1.5D : 0.75D);
		}
	}

	@MethodsReturnNonnullByDefault
	private static class LargeCogHelper extends DiagonalCogHelper {

		@Override
		public Predicate<ItemCooldownManager> getItemPredicate() {
			return AllBlocks.LARGE_COGWHEEL::isIn;
		}

		@Override
		public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
			if (hitOnShaft(state, ray))
				return PlacementOffset.fail();

			if (((CogWheelBlock) state.b()).isLarge) {
				Direction side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.e(), state.c(AXIS))
					.get(0);
				List<Direction> directions =
					IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.e(), state.c(AXIS));
				for (Direction dir : directions) {
					BlockPos newPos = pos.offset(dir)
						.offset(side);
					if (!world.d_(newPos)
						.c()
						.e())
						continue;

					return PlacementOffset.success(newPos, s -> s.a(AXIS, dir.getAxis()));
				}

				return PlacementOffset.fail();
			}

			return super.getOffset(world, state, pos, ray);
		}
	}

	@MethodsReturnNonnullByDefault
	public abstract static class DiagonalCogHelper implements IPlacementHelper {

		@Override
		public Predicate<PistonHandler> getStatePredicate() {
			return s -> s.b() instanceof CogWheelBlock;
		}

		@Override
		public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
			// diagonal gears of different size
			Direction closest = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.e(), state.c(AXIS))
				.get(0);
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.e(),
				state.c(AXIS), d -> d.getAxis() != closest.getAxis());

			for (Direction dir : directions) {
				BlockPos newPos = pos.offset(dir)
					.offset(closest);
				if (!world.d_(newPos)
					.c()
					.e())
					continue;

				if (AllBlocks.COGWHEEL.has(state) && hasSmallCogwheelNeighbor(world, newPos, state.c(AXIS)))
					continue;

				return PlacementOffset.success(newPos, s -> s.a(AXIS, state.c(AXIS)));
			}

			return PlacementOffset.fail();
		}

		@Override
		public void renderAt(BlockPos pos, PistonHandler state, dcg ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()),
				Direction.get(Direction.AxisDirection.POSITIVE, state.c(AXIS)), 1D);
		}

		protected boolean hitOnShaft(PistonHandler state, dcg ray) {
			return AllShapes.SIX_VOXEL_POLE.get(state.c(AXIS))
				.a()
				.g(0.001)
				.d(ray.e()
					.d(ray.e()
						.a(Iterate.axisSet)));
		}

		static public boolean hasLargeCogwheelNeighbor(GameMode world, BlockPos pos, Direction.Axis axis) {
			for (Direction dir : Iterate.directions) {
				if (dir.getAxis() == axis)
					continue;

				if (AllBlocks.LARGE_COGWHEEL.has(world.d_(pos.offset(dir))))
					return true;
			}

			return false;
		}

		static public boolean hasSmallCogwheelNeighbor(GameMode world, BlockPos pos, Direction.Axis axis) {
			for (Direction dir : Iterate.directions) {
				if (dir.getAxis() == axis)
					continue;

				if (AllBlocks.COGWHEEL.has(world.d_(pos.offset(dir))))
					return true;
			}

			return false;
		}
	}

	@MethodsReturnNonnullByDefault
	public static class IntegratedCogHelper implements IPlacementHelper {

		@Override
		public Predicate<ItemCooldownManager> getItemPredicate() {
			return AllBlocks.LARGE_COGWHEEL::isIn;
		}

		@Override
		public Predicate<PistonHandler> getStatePredicate() {
			return s -> !AllBlocks.COGWHEEL.has(s) && s.b() instanceof IRotate
				&& ((IRotate) s.b()).hasIntegratedCogwheel(null, null, null);
		}

		@Override
		public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
			Direction face = ray.b();
			Axis newAxis;

			if (state.b(HorizontalKineticBlock.HORIZONTAL_FACING))
				newAxis = state.c(HorizontalKineticBlock.HORIZONTAL_FACING)
					.getAxis();
			else if (state.b(DirectionalKineticBlock.FACING))
				newAxis = state.c(DirectionalKineticBlock.FACING)
					.getAxis();
			else
				newAxis = Axis.Y;

			if (face.getAxis() == newAxis)
				return PlacementOffset.fail();

			List<Direction> directions =
				IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.e(), face.getAxis(), newAxis);

			for (Direction d : directions) {
				BlockPos newPos = pos.offset(face)
					.offset(d);

				if (!world.d_(newPos)
					.c()
					.e())
					continue;

				if (DiagonalCogHelper.hasLargeCogwheelNeighbor(world, newPos, newAxis)
					|| DiagonalCogHelper.hasSmallCogwheelNeighbor(world, newPos, newAxis))
					return PlacementOffset.fail();

				return PlacementOffset.success(newPos, s -> s.a(CogWheelBlock.AXIS, newAxis));
			}

			return PlacementOffset.fail();
		}

		@Override
		public void renderAt(BlockPos pos, PistonHandler state, dcg ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()),
				Direction.get(Direction.AxisDirection.POSITIVE, offset.getTransform()
					.apply(AllBlocks.LARGE_COGWHEEL.getDefaultState())
					.c(AXIS)));
		}
	}
}
