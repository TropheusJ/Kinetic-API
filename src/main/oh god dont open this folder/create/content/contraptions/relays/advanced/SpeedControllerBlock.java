package com.simibubi.create.content.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import java.util.function.Predicate;

public class SpeedControllerBlock extends HorizontalAxisKineticBlock {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public SpeedControllerBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ROTATION_SPEED_CONTROLLER.create();
	}
	
	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler above = context.p().d_(context.a().up());
		if (CogWheelBlock.isLargeCog(above) && above.c(CogWheelBlock.AXIS).isHorizontal())
			return n().a(HORIZONTAL_AXIS, above.c(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X);
		return super.a(context);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand, dcg ray) {

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		ItemCooldownManager heldItem = player.b(hand);
		if (helper.matchesItem(heldItem)) {
			PlacementOffset offset = helper.getOffset(world, state, pos, ray);

			if (!offset.isReplaceable(world))
				return Difficulty.PASS;

			offset.placeInWorld(world, AllBlocks.LARGE_COGWHEEL.getDefaultState(), player, heldItem);

			return Difficulty.SUCCESS;

		}

		return Difficulty.PASS;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.SPEED_CONTROLLER.get(state.c(HORIZONTAL_AXIS));
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemCooldownManager> getItemPredicate() {
			return AllBlocks.LARGE_COGWHEEL::isIn;
		}

		@Override
		public Predicate<PistonHandler> getStatePredicate() {
			return AllBlocks.ROTATION_SPEED_CONTROLLER::has;
		}

		@Override
		public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
			BlockPos newPos = pos.up();
			if (!world.d_(newPos).c().e())
				return PlacementOffset.fail();

			Axis newAxis = state.c(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

			if (CogwheelBlockItem.DiagonalCogHelper.hasLargeCogwheelNeighbor(world, newPos, newAxis) || CogwheelBlockItem.DiagonalCogHelper.hasSmallCogwheelNeighbor(world, newPos, newAxis))
				return PlacementOffset.fail();

			return PlacementOffset.success(newPos, s -> s.a(CogWheelBlock.AXIS, newAxis));
		}

		@Override
		public void renderAt(BlockPos pos, PistonHandler state, dcg ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()), Direction.get(Direction.AxisDirection.POSITIVE, state.c(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X));
		}
	}
}
