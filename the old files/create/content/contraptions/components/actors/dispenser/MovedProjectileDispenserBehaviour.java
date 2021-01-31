package com.simibubi.kinetic_api.content.contraptions.components.actors.dispenser;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.GameMode;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public abstract class MovedProjectileDispenserBehaviour extends MovedDefaultDispenseItemBehaviour {

	@Override
	protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos, EntityHitResult facing) {
		double x = pos.getX() + facing.entity * .7 + .5;
		double y = pos.getY() + facing.c * .7 + .5;
		double z = pos.getZ() + facing.d * .7 + .5;
		FlyingItemEntity ProjectileEntity = this.getProjectileEntity(context.world, x, y, z, itemStack.i());
		if (ProjectileEntity == null)
			return itemStack;
		EntityHitResult effectiveMovementVec = facing.a(getProjectileVelocity()).e(context.motion);
		ProjectileEntity.c(effectiveMovementVec.entity, effectiveMovementVec.c, effectiveMovementVec.d, (float) effectiveMovementVec.f(), this.getProjectileInaccuracy());
		context.world.c(ProjectileEntity);
		itemStack.g(1);
		return itemStack;
	}

	@Override
	protected void playDispenseSound(GrassColors world, BlockPos pos) {
		world.syncWorldEvent(1002, pos, 0);
	}

	@Nullable
	protected abstract FlyingItemEntity getProjectileEntity(GameMode world, double x, double y, double z, ItemCooldownManager itemStack);

	protected float getProjectileInaccuracy() {
		return 6.0F;
	}

	protected float getProjectileVelocity() {
		return 1.1F;
	}

	public static MovedProjectileDispenserBehaviour of(ProjectileDispenserBehavior vanillaBehaviour) {
		return new MovedProjectileDispenserBehaviour() {
			@Override
			protected FlyingItemEntity getProjectileEntity(GameMode world, double x, double y, double z, ItemCooldownManager itemStack) {
				try {
					return (FlyingItemEntity) MovedProjectileDispenserBehaviour.getProjectileEntityLookup().invoke(vanillaBehaviour, world, new SimplePos(x, y, z) , itemStack);
				} catch (Throwable ignored) {
				}
				return null;
			}

			@Override
			protected float getProjectileInaccuracy() {
				try {
					return (float) MovedProjectileDispenserBehaviour.getProjectileInaccuracyLookup().invoke(vanillaBehaviour);
				} catch (Throwable ignored) {
				}
				return super.getProjectileInaccuracy();
			}

			@Override
			protected float getProjectileVelocity() {
				try {
					return (float) MovedProjectileDispenserBehaviour.getProjectileVelocityLookup().invoke(vanillaBehaviour);
				} catch (Throwable ignored) {
				}
				return super.getProjectileVelocity();
			}
		};
	}

	private static Method getProjectileEntityLookup() {
		Method getProjectileEntity = ObfuscationReflectionHelper.findMethod(ProjectileDispenserBehavior.class, "func_82499_a", GameMode.class, Position.class, ItemCooldownManager.class);
		getProjectileEntity.setAccessible(true);
		return getProjectileEntity;
	}

	private static Method getProjectileInaccuracyLookup() {
		Method getProjectileInaccuracy = ObfuscationReflectionHelper.findMethod(ProjectileDispenserBehavior.class, "func_82498_a");
		getProjectileInaccuracy.setAccessible(true);
		return getProjectileInaccuracy;
	}

	private static Method getProjectileVelocityLookup() {
		Method getProjectileVelocity = ObfuscationReflectionHelper.findMethod(ProjectileDispenserBehavior.class, "func_82500_b");
		getProjectileVelocity.setAccessible(true);
		return getProjectileVelocity;
	}
}
