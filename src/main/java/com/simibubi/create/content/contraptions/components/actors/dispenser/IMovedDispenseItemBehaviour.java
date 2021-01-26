package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.util.Random;
import apx;
import bct;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import cus;
import cut;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.SignItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.potion.Potion;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public interface IMovedDispenseItemBehaviour {

	static void initSpawneggs() {
		final IMovedDispenseItemBehaviour spawnEggDispenseBehaviour = new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos,
				EntityHitResult facing) {
				if (!(itemStack.b() instanceof SignItem))
					return super.dispenseStack(itemStack, context, pos, facing);
				if (context.world instanceof ServerWorld) {
					EntityDimensions<?> entityType = ((SignItem) itemStack.b()).a(itemStack.o());
					apx spawnedEntity = entityType.a((ServerWorld) context.world, itemStack, null,
						pos.add(facing.entity + .7, facing.c + .7, facing.d + .7), LivingEntity.o, facing.c < .5,
						false);
					if (spawnedEntity != null)
						spawnedEntity.f(context.motion.a(2));
				}
				itemStack.g(1);
				return itemStack;
			}
		};

		for (SignItem spawneggitem : SignItem.f())
			DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(spawneggitem, spawnEggDispenseBehaviour);
	}

	static void init() {
		MovedProjectileDispenserBehaviour movedPotionDispenseItemBehaviour = new MovedProjectileDispenserBehaviour() {
			@Override
			protected FlyingItemEntity getProjectileEntity(GameMode world, double x, double y, double z,
				ItemCooldownManager itemStack) {
				return Util.make(new EggEntity(world, x, y, z), (p_218411_1_) -> p_218411_1_.b(itemStack));
			}

			protected float getProjectileInaccuracy() {
				return super.getProjectileInaccuracy() * 0.5F;
			}

			protected float getProjectileVelocity() {
				return super.getProjectileVelocity() * .5F;
			}
		};

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.qj,
			movedPotionDispenseItemBehaviour);
		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.qm,
			movedPotionDispenseItemBehaviour);

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.cl,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos,
					EntityHitResult facing) {
					double x = pos.getX() + facing.entity * .7 + .5;
					double y = pos.getY() + facing.c * .7 + .5;
					double z = pos.getZ() + facing.d * .7 + .5;
					bct tntentity = new bct(context.world, x, y, z, null);
					tntentity.i(context.motion.entity, context.motion.c, context.motion.d);
					context.world.c(tntentity);
					context.world.a(null, tntentity.cC(), tntentity.cD(), tntentity.cG(),
						MusicType.pb, SoundEvent.e, 1.0F, 1.0F);
					itemStack.g(1);
					return itemStack;
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.po,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos,
					EntityHitResult facing) {
					double x = pos.getX() + facing.entity * .7 + .5;
					double y = pos.getY() + facing.c * .7 + .5;
					double z = pos.getZ() + facing.d * .7 + .5;
					EvokerFangsEntity fireworkrocketentity =
						new EvokerFangsEntity(context.world, itemStack, x, y, z, true);
					fireworkrocketentity.c(facing.entity, facing.c, facing.d, 0.5F, 1.0F);
					context.world.c(fireworkrocketentity);
					itemStack.g(1);
					return itemStack;
				}

				@Override
				protected void playDispenseSound(GrassColors world, BlockPos pos) {
					world.syncWorldEvent(1004, pos, 0);
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.oS,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected void playDispenseSound(GrassColors world, BlockPos pos) {
					world.syncWorldEvent(1018, pos, 0);
				}

				@Override
				protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos,
					EntityHitResult facing) {
					Random random = context.world.t;
					double x = pos.getX() + facing.entity * .7 + .5;
					double y = pos.getY() + facing.c * .7 + .5;
					double z = pos.getZ() + facing.d * .7 + .5;
					context.world.c(Util.make(
						new ProjectileEntity(context.world, x, y, z,
							random.nextGaussian() * 0.05D + facing.entity + context.motion.entity,
							random.nextGaussian() * 0.05D + facing.c + context.motion.c,
							random.nextGaussian() * 0.05D + facing.d + context.motion.d),
						(p_229425_1_) -> p_229425_1_.b(itemStack)));
					itemStack.g(1);
					return itemStack;
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.nw,
			new MovedOptionalDispenseBehaviour() {
				@Override
				protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos,
					EntityHitResult facing) {
					this.successful = false;
					BlockPos interactAt = pos.offset(getClosestFacingDirection(facing));
					PistonHandler state = context.world.d_(interactAt);
					BeetrootsBlock block = state.b();

					if (block.a(StatHandler.aj) && state.c(Stainable.b) >= 5) { 
						((Stainable) block).a(context.world, state, interactAt, null,
							LockableContainerBlockEntity.b.b);
						this.successful = true;
						return placeItemInInventory(itemStack, new ItemCooldownManager(AliasedBlockItem.rt), context, pos,
							facing);
					} else if (context.world.b(interactAt)
						.a(BlockTags.field_15481)) {
						this.successful = true;
						return placeItemInInventory(itemStack,
							WrittenBookItem.a(new ItemCooldownManager(AliasedBlockItem.nv), Potion.effects), context, pos,
							facing);
					} else {
						return super.dispenseStack(itemStack, context, pos, facing);
					}
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(AliasedBlockItem.lK,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected ItemCooldownManager dispenseStack(ItemCooldownManager itemStack, MovementContext context, BlockPos pos,
					EntityHitResult facing) {
					BlockPos interactAt = pos.offset(getClosestFacingDirection(facing));
					PistonHandler state = context.world.d_(interactAt);
					BeetrootsBlock block = state.b();
					if (block instanceof Fertilizable) {
						cut fluid = ((Fertilizable) block).b(context.world, interactAt, state);
						if (fluid instanceof cus)
							return placeItemInInventory(itemStack, new ItemCooldownManager(fluid.a()), context, pos,
								facing);
					}
					return super.dispenseStack(itemStack, context, pos, facing);
				}
			});
	}

	ItemCooldownManager dispense(ItemCooldownManager itemStack, MovementContext context, BlockPos pos);
}
