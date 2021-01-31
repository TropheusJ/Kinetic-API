package com.simibubi.kinetic_api.content.contraptions.components.actors;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DrillMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.c(DrillBlock.FACING)
			.getOpposite());
	}

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(DrillBlock.FACING)
			.getVector()).a(.65f);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		DrillRenderer.renderInContraption(context, ms, msLocal, buffer);
	}

	@Override
	protected DamageRecord getDamageSource() {
		return DrillBlock.damageSourceDrill;
	}

	@Override
	public boolean canBreak(GameMode world, BlockPos breakingPos, PistonHandler state) {
		return super.canBreak(world, breakingPos, state) && !state.k(world, breakingPos)
			.b();
	}

}
