package com.tropheus_jay.kinetic_api.content.contraptions.wrench;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWrenchable {

	default ActionResult onWrenched(BlockState state, ItemUsageContext context) {
		World world = context.getWorld();
		/*BlockState rotated = getRotatedBlockState(state, context.getSide());
		if (!rotated.canPlaceAt(world, context.getBlockPos()))*/
			return ActionResult.PASS;
	/*	//todo: kineticTileEntity
		//KineticTileEntity.switchToBlockState(world, context.getBlockPos(), updateAfterWrenched(rotated, context));

		BlockEntity te = context.getWorld()
			.getBlockEntity(context.getBlockPos());
		if (te != null)
			//te.updateContainingBlockInfo(); //todo: what
	//	todo: generatingKineticTileEntity
	//	if (te instanceof GeneratingKineticTileEntity) {
	//		((GeneratingKineticTileEntity) te).updateGeneratedRotation();
	//	}

		return ActionResult.SUCCESS;
	*/}

	default BlockState updateAfterWrenched(BlockState newState, ItemUsageContext context) {
		return newState;
	}

	default ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		PlayerEntity player = context.getPlayer();
		if (world instanceof ServerWorld) {
			if (player != null && !player.isCreative())
				Block.getDroppedStacks(state, (ServerWorld) world, pos, world.getBlockEntity(pos), player, context.getStack())
					.forEach(itemStack -> {
						player.inventory.offerOrDrop(world, itemStack);
					});
			state.onStacksDropped((ServerWorld) world, pos, ItemStack.EMPTY);
			world.breakBlock(pos, false);
		}
		return ActionResult.SUCCESS;
	}
/*todo: lots of stuff to add in the future
	default BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		BlockState newState = originalState;

		if (targetedFace.getAxis() == Direction.Axis.Y) {
			if (BlockHelper.hasBlockStateProperty(originalState, HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
				return originalState.a(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, DirectionHelper
					.rotateAround(VoxelShaper.axisAsFace(originalState.c(HorizontalAxisKineticBlock.HORIZONTAL_AXIS)),
						targetedFace.getAxis())
					.getAxis());
			if (BlockHelper.hasBlockStateProperty(originalState, HorizontalKineticBlock.HORIZONTAL_FACING))
				return originalState.a(HorizontalKineticBlock.HORIZONTAL_FACING, DirectionHelper
					.rotateAround(originalState.c(HorizontalKineticBlock.HORIZONTAL_FACING), targetedFace.getAxis()));
		}

		if (BlockHelper.hasBlockStateProperty(originalState, RotatedPillarKineticBlock.AXIS))
			return originalState.a(RotatedPillarKineticBlock.AXIS,
				DirectionHelper
					.rotateAround(VoxelShaper.axisAsFace(originalState.c(RotatedPillarKineticBlock.AXIS)),
						targetedFace.getAxis())
					.getAxis());

		if (!BlockHelper.hasBlockStateProperty(originalState, DirectionalKineticBlock.FACING))
			return originalState;

		Direction stateFacing = originalState.c(DirectionalKineticBlock.FACING);

		if (stateFacing.getAxis()
			.equals(targetedFace.getAxis())) {
			if (BlockHelper.hasBlockStateProperty(originalState, DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
				return originalState.a(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			else
				return originalState;
		} else {
			do {
				newState = newState.a(DirectionalKineticBlock.FACING,
					DirectionHelper.rotateAround(newState.c(DirectionalKineticBlock.FACING), targetedFace.getAxis()));
				if (targetedFace.getAxis() == Direction.Axis.Y
					&& BlockHelper.hasBlockStateProperty(newState, DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))
					newState = newState.a(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			} while (newState.c(DirectionalKineticBlock.FACING)
				.getAxis()
				.equals(targetedFace.getAxis()));
		}
		return newState;
	} */
}
