package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import apx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SailBlock extends ProperDirectionalBlock {

	public static SailBlock frame(c properties) {
		return new SailBlock(properties, true);
	}

	public static SailBlock withCanvas(c properties) {
		return new SailBlock(properties, false);
	}

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	private final boolean frame;

	protected SailBlock(c p_i48415_1_, boolean frame) {
		super(p_i48415_1_);
		this.frame = frame;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler state = super.a(context);
		return state.a(SHAPE, state.c(SHAPE).getOpposite());
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand, dcg ray) {
		ItemCooldownManager heldItem = player.b(hand);

		if (AllBlocks.SAIL.isIn(heldItem) || AllBlocks.SAIL_FRAME.isIn(heldItem)) {
			IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
			PlacementOffset offset = placementHelper.getOffset(world, state, pos, ray);

			if (!offset.isReplaceable(world))
				return Difficulty.PASS;

			offset.placeInWorld(world, ((BannerItem) heldItem.b()).e().n(), player, heldItem);

			/*BlockState blockState = ((BlockItem) heldItem.getItem()).getBlock()
					.getDefaultState()
					.with(FACING, state.get(FACING));
			BlockPos offsetPos = new BlockPos(offset.getPos());
			if (!world.isRemote && world.getBlockState(offsetPos).getMaterial().isReplaceable()) {
				world.setBlockState(offsetPos, blockState);
				if (!player.isCreative())
					heldItem.shrink(1);
			}*/

			return Difficulty.SUCCESS;
		}

		if (heldItem.b() instanceof SaddleItem) {
			if (!world.v)
				applyDye(state, world, pos, null);
			return Difficulty.SUCCESS;
		}

		if (frame)
			return Difficulty.PASS;

		for (DebugStickItem color : DebugStickItem.values()) {
			if (!heldItem.b()
					.a(DyeHelper.getTagOfDye(color)))
				continue;
			if (!world.v)
				applyDye(state, world, pos, color);
			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	protected void applyDye(PistonHandler state, GameMode world, BlockPos pos, @Nullable DebugStickItem color) {
		PistonHandler newState =
				(color == null ? AllBlocks.SAIL_FRAME : AllBlocks.DYED_SAILS[color.ordinal()]).getDefaultState()
						.a(SHAPE, state.c(SHAPE));

		// Dye the block itself
		if (state != newState) {
			world.a(pos, newState);
			return;
		}

		// Dye all adjacent
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == state.c(SHAPE)
					.getAxis())
				continue;
			BlockPos offset = pos.offset(d);
			PistonHandler adjacentState = world.d_(offset);
			BeetrootsBlock block = adjacentState.b();
			if (!(block instanceof SailBlock) || ((SailBlock) block).frame)
				continue;
			if (state == adjacentState)
				continue;
			world.a(offset, newState);
			return;
		}

		// Dye all the things
		List<BlockPos> frontier = new ArrayList<>();
		frontier.add(pos);
		Set<BlockPos> visited = new HashSet<>();
		int timeout = 100;
		while (!frontier.isEmpty()) {
			if (timeout-- < 0)
				break;

			BlockPos currentPos = frontier.remove(0);
			visited.add(currentPos);

			for (Direction d : Iterate.directions) {
				if (d.getAxis() == state.c(SHAPE)
						.getAxis())
					continue;
				BlockPos offset = currentPos.offset(d);
				if (visited.contains(offset))
					continue;
				PistonHandler adjacentState = world.d_(offset);
				BeetrootsBlock block = adjacentState.b();
				if (!(block instanceof SailBlock) || ((SailBlock) block).frame && color != null)
					continue;
				if (state != adjacentState)
					world.a(offset, newState);
				frontier.add(offset);
				visited.add(offset);
			}
		}
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_, ArrayVoxelShape p_220053_4_) {
		return (frame ? AllShapes.SAIL_FRAME : AllShapes.SAIL).get(state.c(SHAPE));
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic p_220071_2_, BlockPos p_220071_3_, ArrayVoxelShape p_220071_4_) {
		if (frame)
			return AllShapes.SAIL_FRAME_COLLISION.get(state.c(SHAPE));
		return b(state, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos, PlayerAbilities player) {
		ItemCooldownManager pickBlock = super.getPickBlock(state, target, world, pos, player);
		if (pickBlock.a())
			return AllBlocks.SAIL.get()
					.getPickBlock(state, target, world, pos, player);
		return pickBlock;
	}

	public void a(GameMode p_180658_1_, BlockPos p_180658_2_, apx p_180658_3_, float p_180658_4_) {
		if (frame)
			super.a(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
		super.a(p_180658_1_, p_180658_2_, p_180658_3_, 0);
	}

	public void a(MobSpawnerLogic p_176216_1_, apx p_176216_2_) {
		if (frame || p_176216_2_.bv()) {
			super.a(p_176216_1_, p_176216_2_);
		} else {
			this.bounce(p_176216_2_);
		}
	}

	private void bounce(apx p_226860_1_) {
		EntityHitResult vec3d = p_226860_1_.cB();
		if (vec3d.c < 0.0D) {
			double d0 = p_226860_1_ instanceof SaddledComponent ? 1.0D : 0.8D;
			p_226860_1_.n(vec3d.entity, -vec3d.c * (double) 0.26F * d0, vec3d.d);
		}

	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemCooldownManager> getItemPredicate() {
			return i -> AllBlocks.SAIL.isIn(i) || AllBlocks.SAIL_FRAME.isIn(i);
		}

		@Override
		public Predicate<PistonHandler> getStatePredicate() {
			return s -> s.b() instanceof SailBlock;
		}

		@Override
		public PlacementOffset getOffset(GameMode world, PistonHandler state, BlockPos pos, dcg ray) {
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.e(), state.c(SailBlock.SHAPE).getAxis(), dir -> world.d_(pos.offset(dir)).c().e());

			if (directions.isEmpty())
				return PlacementOffset.fail();
			else {
				return PlacementOffset.success(pos.offset(directions.get(0)), s -> s.a(SHAPE, state.c(SHAPE)));
			}
		}

		@Override
		public void renderAt(BlockPos pos, PistonHandler state, dcg ray, PlacementOffset offset) {
			IPlacementHelper.renderArrow(VecHelper.getCenterOf(pos), VecHelper.getCenterOf(offset.getPos()), state.c(SHAPE));
		}
	}
}
