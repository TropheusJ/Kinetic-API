package com.simibubi.create.content.contraptions.wrench;

import bnx;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.VoxelShaper;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public interface IWrenchable {

	default Difficulty onWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		PistonHandler rotated = getRotatedBlockState(state, context.j());
		if (!rotated.a(world, context.a()))
			return Difficulty.PASS;

		KineticTileEntity.switchToBlockState(world, context.a(), updateAfterWrenched(rotated, context));

		BeehiveBlockEntity te = context.p()
			.c(context.a());
		if (te != null)
			te.s();
		if (te instanceof GeneratingKineticTileEntity) {
			((GeneratingKineticTileEntity) te).updateGeneratedRotation();
		}

		return Difficulty.SUCCESS;
	}

	default PistonHandler updateAfterWrenched(PistonHandler newState, bnx context) {
		return newState;
	}

	default Difficulty onSneakWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		BlockPos pos = context.a();
		PlayerAbilities player = context.n();
		if (world instanceof ServerWorld) {
			if (player != null && !player.b_())
				BeetrootsBlock.a(state, (ServerWorld) world, pos, world.c(pos), player, context.m())
					.forEach(itemStack -> {
						player.bm.a(world, itemStack);
					});
			state.a((ServerWorld) world, pos, ItemCooldownManager.tick);
			world.b(pos, false);
		}
		return Difficulty.SUCCESS;
	}

	default PistonHandler getRotatedBlockState(PistonHandler originalState, Direction targetedFace) {
		PistonHandler newState = originalState;

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
	}
}
