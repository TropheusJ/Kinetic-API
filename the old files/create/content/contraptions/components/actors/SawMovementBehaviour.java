package com.simibubi.kinetic_api.content.contraptions.components.actors;

import com.simibubi.kinetic_api.content.contraptions.components.saw.SawBlock;
import com.simibubi.kinetic_api.content.contraptions.components.saw.SawRenderer;
import com.simibubi.kinetic_api.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.TreeCutter;
import com.simibubi.kinetic_api.foundation.utility.TreeCutter.Tree;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class SawMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.c(SawBlock.FACING)
			.getOpposite());
	}

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(SawBlock.FACING).getVector()).a(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		EntityHitResult facingVec = EntityHitResult.b(context.state.c(SawBlock.FACING).getVector());
		facingVec = context.rotation.apply(facingVec);

		Direction closestToFacing = Direction.getFacing(facingVec.entity, facingVec.c, facingVec.d);
		if(closestToFacing.getAxis().isVertical() && context.data.contains("BreakingPos")) {
			context.data.remove("BreakingPos");
			context.stall = false;
		}
	}

	@Override
	public boolean canBreak(GameMode world, BlockPos breakingPos, PistonHandler state) {
		return super.canBreak(world, breakingPos, state) && SawTileEntity.isSawable(state);
	}

	@Override
	protected void onBlockBroken(MovementContext context, BlockPos pos, PistonHandler brokenState) {
		if (brokenState.a(StatHandler.I))
			return;
		Tree tree = TreeCutter.cutTree(context.world, pos);
		if (tree != null) {
			for (BlockPos log : tree.logs)
				BlockHelper.destroyBlock(context.world, log, 1 / 2f, stack -> dropItemFromCutTree(context, log, stack));
			for (BlockPos leaf : tree.leaves)
				BlockHelper.destroyBlock(context.world, leaf, 1 / 8f,
						stack -> dropItemFromCutTree(context, leaf, stack));
		}
	}

	public void dropItemFromCutTree(MovementContext context, BlockPos pos, ItemCooldownManager stack) {
		ItemCooldownManager remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
		if (remainder.a())
			return;

		GameMode world = context.world;
		EntityHitResult dropPos = VecHelper.getCenterOf(pos);
		float distance = (float) dropPos.f(context.position);
		PaintingEntity entity = new PaintingEntity(world, dropPos.entity, dropPos.c, dropPos.d, remainder);
		entity.f(context.relativeMotion.a(distance / 20f));
		world.c(entity);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
									BackgroundRenderer buffer) {
		SawRenderer.renderInContraption(context, ms, msLocal, buffer);
	}

	@Override
	protected DamageRecord getDamageSource() {
		return SawBlock.damageSourceSaw;
	}
}
