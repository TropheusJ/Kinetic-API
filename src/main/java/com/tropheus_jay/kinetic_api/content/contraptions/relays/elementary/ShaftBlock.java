package com.tropheus_jay.kinetic_api.content.contraptions.relays.elementary;

import com.tropheus_jay.kinetic_api.AllShapes;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ShaftBlock extends AbstractShaftBlock {
//todo: placementhelpers
	//private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public ShaftBlock(Settings properties) {
		super(properties);
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return null;
	}
//todo: has
	/*public static boolean isShaft(BlockState state) {
		return AllBlocks.SHAFT.has(state);
	}*/

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		//todo: AllShapes
		return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS));
		
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockHitResult ray) {
		if (player.isSneaking() || !player.canModifyBlocks())
			return ActionResult.PASS;
		/* todo: encased shafts
		ItemStack heldItem = player.getStackInHand(hand);
		for (EncasedShaftBlock encasedShaft : new EncasedShaftBlock[] { AllBlocks.ANDESITE_ENCASED_SHAFT.get(),
			AllBlocks.BRASS_ENCASED_SHAFT.get() }) {

			if (!encasedShaft.getCasing()
				.isIn(heldItem))
				continue;

			if (world.v)
				return ActionResult.SUCCESS;
			
			AllTriggers.triggerFor(AllTriggers.CASING_SHAFT, player);
			KineticTileEntity.switchToBlockState(world, pos, encasedShaft.n()
				.a(AXIS, state.c(AXIS)));
			return ActionResult.SUCCESS;
		}*/
/*todo: placementhelpers
		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.getItemPredicate().test(heldItem)) {
			PlacementOffset offset = helper.getOffset(world, state, pos, ray);

			if (!offset.isReplaceable(world))
				return Difficulty.PASS;

			offset.placeInWorld(world, (BannerItem) heldItem.b(), player, heldItem);

			//BlockPos newPos = new BlockPos(offset.getPos());

			//if (world.isRemote)
			//	return ActionResultType.SUCCESS;

			//Block block = ((BlockItem) heldItem.getItem()).getBlock();
			//world.setBlockState(newPos, offset.getTransform().apply(block.getDefaultState()));
			//if (!player.isCreative())
			//	heldItem.shrink(1);

			//return ActionResult.SUCCESS;
		}
*/
		return ActionResult.PASS;
	}
	//hopefully not important
	//@MethodsReturnNonnullByDefault
	/* todo: polehelpers
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
	}*/
}
