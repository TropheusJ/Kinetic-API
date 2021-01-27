package com.simibubi.create.content.contraptions.components.structureMovement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class MovementBehaviour {

	public boolean isActive(MovementContext context) {
		return true;
	}

	public void tick(MovementContext context) {}

	public void startMoving(MovementContext context) {}

	public void visitNewPosition(MovementContext context, BlockPos pos) {}

	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.a;
	}

	public void dropItem(MovementContext context, ItemCooldownManager stack) {
		ItemCooldownManager remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
		if (remainder.a())
			return;

		EntityHitResult vec = context.position;
		PaintingEntity itemEntity = new PaintingEntity(context.world, vec.entity, vec.c, vec.d, remainder);
		itemEntity.f(context.motion.b(0, 0.5f, 0)
			.a(context.world.t.nextFloat() * .3f));
		context.world.c(itemEntity);
	}

	public void stopMoving(MovementContext context) {

	}

	public void writeExtraData(MovementContext context) {

	}

	public boolean hasSpecialMovementRenderer() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {}

	public void onSpeedChanged(MovementContext context, EntityHitResult oldMotion, EntityHitResult motion) {

	}
}
