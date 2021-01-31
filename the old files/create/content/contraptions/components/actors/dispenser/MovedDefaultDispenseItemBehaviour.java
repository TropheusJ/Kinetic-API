package com.simibubi.kinetic_api.content.contraptions.components.actors.dispenser;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.ItemHandlerHelper;

public class MovedDefaultDispenseItemBehaviour implements IMovedDispenseItemBehaviour {
	private static final MovedDefaultDispenseItemBehaviour defaultInstance = new MovedDefaultDispenseItemBehaviour();

	public static void doDispense(GameMode p_82486_0_, ItemCooldownManager p_82486_1_, int p_82486_2_, EntityHitResult facing, BlockPos p_82486_4_, MovementContext context) {
		double d0 = p_82486_4_.getX() + facing.entity + .5;
		double d1 = p_82486_4_.getY() + facing.c + .5;
		double d2 = p_82486_4_.getZ() + facing.d + .5;
		if (Direction.getFacing(facing.entity, facing.c, facing.d).getAxis() == Direction.Axis.Y) {
			d1 = d1 - 0.125D;
		} else {
			d1 = d1 - 0.15625D;
		}

		PaintingEntity itementity = new PaintingEntity(p_82486_0_, d0, d1, d2, p_82486_1_);
		double d3 = p_82486_0_.t.nextDouble() * 0.1D + 0.2D;
		itementity.n(p_82486_0_.t.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.getX() * d3 + context.motion.entity, p_82486_0_.t.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.getY() * d3 + context.motion.c, p_82486_0_.t.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.getZ() * d3 + context.motion.d);
		p_82486_0_.c(itementity);
	}

	@Override
	public ItemCooldownManager dispense(ItemCooldownManager itemStack, MovementContext context, BlockPos pos) {
		EntityHitResult facingVec = EntityHitResult.b(context.state.c(DetectorRailBlock.a).getVector());
		facingVec = context.rotation.apply(facingVec);
		facingVec.d();

		Direction closestToFacing = getClosestFacingDirection(facingVec);
		BossBar iinventory = EnderChestBlockEntity.b(context.world, pos.offset(closestToFacing));
		if (iinventory == null) {
			this.playDispenseSound(context.world, pos);
			this.spawnDispenseParticles(context.world, pos, closestToFacing);
			return this.dispenseStack(itemStack, context, pos, facingVec);
		} else {
			if (EnderChestBlockEntity.a(null, iinventory, itemStack.i().a(1), closestToFacing.getOpposite()).a())
				itemStack.g(1);
			return itemStack;
		}
	}

	/**
	 * Dispense the specified stack, play the dispense sound and spawn particles.
	 */
	protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos, EntityHitResult facing) {
		ItemCooldownManager itemstack = itemStack.a(1);
		doDispense(context.world, itemstack, 6, facing, pos, context);
		return itemStack;
	}

	/**
	 * Play the dispense sound from the specified block.
	 */
	protected void playDispenseSound(GrassColors world, BlockPos pos) {
		world.syncWorldEvent(1000, pos, 0);
	}

	/**
	 * Order clients to display dispense particles from the specified block and facing.
	 */
	protected void spawnDispenseParticles(GrassColors world, BlockPos pos, EntityHitResult facing) {
		spawnDispenseParticles(world, pos, getClosestFacingDirection(facing));
	}

	protected void spawnDispenseParticles(GrassColors world, BlockPos pos, Direction direction) {
		world.syncWorldEvent(2000, pos, direction.getId());
	}

	protected Direction getClosestFacingDirection(EntityHitResult exactFacing) {
		return Direction.getFacing(exactFacing.entity, exactFacing.c, exactFacing.d);
	}

	protected ItemCooldownManager placeItemInInventory(ItemCooldownManager consumedFrom, ItemCooldownManager output, MovementContext context, BlockPos pos, EntityHitResult facing) {
		consumedFrom.g(1);
		ItemCooldownManager remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, output.i(), false);
		if (!remainder.a())
			defaultInstance.dispenseStack(output, context, pos, facing);
		return consumedFrom;
	}
}
