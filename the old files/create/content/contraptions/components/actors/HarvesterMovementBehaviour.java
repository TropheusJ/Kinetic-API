package com.simibubi.kinetic_api.content.contraptions.components.actors;

import static net.minecraft.block.HayBlock.aq;

import org.apache.commons.lang3.mutable.MutableBoolean;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.BellBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CoralBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.IPlantable;

public class HarvesterMovementBehaviour extends MovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.c(aq)
			.getOpposite());
	}

	@Override
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffers) {
		HarvesterRenderer.renderInContraption(context, ms, msLocal, buffers);
	}

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(aq)
			.getVector())
			.a(.45);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		GameMode world = context.world;
		PistonHandler stateVisited = world.d_(pos);
		boolean notCropButCuttable = false;

		if (world.v)
			return;

		if (!isValidCrop(world, pos, stateVisited)) {
			if (isValidOther(world, pos, stateVisited))
				notCropButCuttable = true;
			else
				return;
		}

		MutableBoolean seedSubtracted = new MutableBoolean(notCropButCuttable);
		PistonHandler state = stateVisited;
		BlockHelper.destroyBlock(world, pos, 1, stack -> {
			if (!seedSubtracted.getValue() && stack.a(new ItemCooldownManager(state.b()))) {
				stack.g(1);
				seedSubtracted.setTrue();
			}
			dropItem(context, stack);
		});

		world.a(pos, cutCrop(world, pos, stateVisited));
	}

	private boolean isValidCrop(GameMode world, BlockPos pos, PistonHandler state) {
		if (state.b() instanceof CoralBlock) {
			CoralBlock crop = (CoralBlock) state.b();
			if (!crop.h(state))
				return false;
			return true;
		}
		if (state.k(world, pos)
			.b() || state.b() instanceof ChestBlock) {
			for (IntProperty<?> property : state.r()) {
				if (!(property instanceof DoubleBlockHalf))
					continue;
				if (!property.f()
					.equals(BambooLeaves.ae.f()))
					continue;
				if (((DoubleBlockHalf) property).a()
					.size() - 1 != state.c((DoubleBlockHalf) property)
						.intValue())
					continue;
				return true;
			}
		}

		return false;
	}

	private boolean isValidOther(GameMode world, BlockPos pos, PistonHandler state) {
		if (state.b() instanceof CoralBlock)
			return false;
		if (state.b() instanceof StonecutterBlock)
			return true;

		if (state.k(world, pos)
			.b() || state.b() instanceof ChestBlock) {
			if (state.b() instanceof JigsawBlock)
				return true;
			if (state.b() instanceof PaneBlock)
				return true;

			for (IntProperty<?> property : state.r()) {
				if (!(property instanceof DoubleBlockHalf))
					continue;
				if (!property.f()
					.equals(BambooLeaves.ae.f()))
					continue;
				return false;
			}

			if (state.b() instanceof IPlantable)
				return true;
		}

		return false;
	}

	private PistonHandler cutCrop(GameMode world, BlockPos pos, PistonHandler state) {
		if (state.b() instanceof CoralBlock) {
			CoralBlock crop = (CoralBlock) state.b();
			return crop.b(0);
		}
		if (state.b() == BellBlock.cH || state.b() == BellBlock.kc) {
			if (state.m()
				.c())
				return BellBlock.FACING.n();
			return state.m()
				.g();
		}
		if (state.k(world, pos)
			.b() || state.b() instanceof ChestBlock) {
			for (IntProperty<?> property : state.r()) {
				if (!(property instanceof DoubleBlockHalf))
					continue;
				if (!property.f()
					.equals(BambooLeaves.ae.f()))
					continue;
				return state.a((DoubleBlockHalf) property, Integer.valueOf(0));
			}
		}

		if (state.m()
			.c())
			return BellBlock.FACING.n();
		return state.m()
			.g();
	}

}
