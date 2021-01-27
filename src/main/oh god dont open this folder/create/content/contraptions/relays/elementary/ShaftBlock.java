package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import com.simibubi.create.foundation.utility.placement.util.PoleHelper;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import java.util.function.Predicate;

public class ShaftBlock extends AbstractShaftBlock {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public ShaftBlock(c properties) {
		super(properties);
	}

	public static boolean isShaft(PistonHandler state) {
		return AllBlocks.SHAFT.has(state);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.SIX_VOXEL_POLE.get(state.c(AXIS));
	}

	@Override
	public float getParticleTargetRadius() {
		return .25f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 0f;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg ray) {
		if (player.bt() || !player.eJ())
			return Difficulty.PASS;

		ItemCooldownManager heldItem = player.b(hand);
		for (EncasedShaftBlock encasedShaft : new EncasedShaftBlock[] { AllBlocks.ANDESITE_ENCASED_SHAFT.get(),
			AllBlocks.BRASS_ENCASED_SHAFT.get() }) {

			if (!encasedShaft.getCasing()
				.isIn(heldItem))
				continue;

			if (world.v)
				return Difficulty.SUCCESS;
			
			AllTriggers.triggerFor(AllTriggers.CASING_SHAFT, player);
			KineticTileEntity.switchToBlockState(world, pos, encasedShaft.n()
				.a(AXIS, state.c(AXIS)));
			return Difficulty.SUCCESS;
		}

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.getItemPredicate().test(heldItem)) {
			PlacementOffset offset = helper.getOffset(world, state, pos, ray);

			if (!offset.isReplaceable(world))
				return Difficulty.PASS;

			offset.placeInWorld(world, (BannerItem) heldItem.b(), player, heldItem);

			/*BlockPos newPos = new BlockPos(offset.getPos());

			if (world.isRemote)
				return ActionResultType.SUCCESS;

			Block block = ((BlockItem) heldItem.getItem()).getBlock();
			world.setBlockState(newPos, offset.getTransform().apply(block.getDefaultState()));
			if (!player.isCreative())
				heldItem.shrink(1);*/

			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper extends PoleHelper<Direction.Axis> {
		//used for extending a shaft in its axis, like the piston poles. works with shafts and cogs

		private PlacementHelper(){
			super(
					state -> state.b() instanceof AbstractShaftBlock,
					state -> state.c(AXIS),
					AXIS
			);
		}

		@Override
		public Predicate<ItemCooldownManager> getItemPredicate() {
			return i -> i.b() instanceof BannerItem && ((BannerItem) i.b()).e() instanceof AbstractShaftBlock;
		}

		@Override
		public Predicate<PistonHandler> getStatePredicate() {
			return AllBlocks.SHAFT::has;
		}
	}
}
