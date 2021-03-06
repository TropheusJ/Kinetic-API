package com.tropheus_jay.kinetic_api.foundation.utility;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.Properties;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.StatHandler;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.Consumer;

public class BlockHelper {

	@Environment(EnvType.CLIENT)
	public static void addReducedDestroyEffects(BlockState state, World worldIn, BlockPos pos, ParticleManager manager) {
		if (!(worldIn instanceof ClientWorld))
			return;
		ClientWorld world = (ClientWorld) worldIn;
		VoxelShape voxelshape = state.getOutlineShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			double d1 = Math.min(1.0D, x2 - x1);
			double d2 = Math.min(1.0D, y2 - y1);
			double d3 = Math.min(1.0D, z2 - z1);
			int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
			int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
			int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.random.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + x1;
						double d8 = d5 * d2 + y1;
						double d9 = d6 * d3 + z1;
						manager
							.addParticle((new BlockDustParticle(world, (double) pos.getX() + d7, (double) pos.getY() + d8,
								(double) pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state)).setBlockPos(pos));
					}
				}
			}

		});
	}
	// this was hell to manually fix
	public static BlockState setZeroAge(BlockState blockState) {
		if (hasBlockStateProperty(blockState, Properties.AGE_1))
			return blockState.with(Properties.AGE_1, 0);
		if (hasBlockStateProperty(blockState, Properties.AGE_2))
			return blockState.with(Properties.AGE_2, 0);
		if (hasBlockStateProperty(blockState, Properties.AGE_3))
			return blockState.with(Properties.AGE_3, 0);
		if (hasBlockStateProperty(blockState, Properties.AGE_5))
			return blockState.with(Properties.AGE_5, 0);
		if (hasBlockStateProperty(blockState, Properties.AGE_7))
			return blockState.with(Properties.AGE_7, 0);
		if (hasBlockStateProperty(blockState, Properties.AGE_15))
			return blockState.with(Properties.AGE_15, 0);
		if (hasBlockStateProperty(blockState, Properties.AGE_25))
			return blockState.with(Properties.AGE_25, 0);
		if (hasBlockStateProperty(blockState, Properties.HONEY_LEVEL))
			return blockState.with(Properties.HONEY_LEVEL, 0);
		if (hasBlockStateProperty(blockState, Properties.HATCH))
			return blockState.with(Properties.HATCH, 0);
		if (hasBlockStateProperty(blockState, Properties.STAGE))
			return blockState.with(Properties.STAGE, 0);
		if (hasBlockStateProperty(blockState, Properties.LEVEL_3))
			return blockState.with(Properties.LEVEL_3, 0);
		if (hasBlockStateProperty(blockState, Properties.LEVEL_8))
			return blockState.with(Properties.LEVEL_8, 0);
		if (hasBlockStateProperty(blockState, Properties.EXTENDED))
			return blockState.with(Properties.EXTENDED, false);
		return blockState;
	}

	public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
		int amountFound = 0;
		Item required = getRequiredItem(block).getItem();

		boolean needsTwo =
			hasBlockStateProperty(block, Properties.aK) && block.c(Properties.aK) == Property.hashCodeCache;

		if (needsTwo)
			amount *= 2;

		if (hasBlockStateProperty(block, Properties.ao))
			amount *= block.c(Properties.ao);

		if (hasBlockStateProperty(block, Properties.ay))
			amount *= block.c(Properties.ay);

		{
			// Try held Item first
			int preferredSlot = player.bm.d;
			ItemCooldownManager itemstack = player.bm.a(preferredSlot);
			int count = itemstack.E();
			if (itemstack.b() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.bm.a(preferredSlot,
					new ItemCooldownManager(itemstack.b(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.bm.Z_(); ++i) {
			if (amountFound == amount)
				break;

			ItemCooldownManager itemstack = player.bm.a(i);
			int count = itemstack.E();
			if (itemstack.b() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.bm.a(i, new ItemCooldownManager(itemstack.b(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.bm.e(new ItemCooldownManager(required));
			amountFound /= 2;
		}

		return amountFound;
	}

	public static ItemCooldownManager getRequiredItem(BlockState state) {
		ItemCooldownManager itemStack = new ItemCooldownManager(state.b());
		if (itemStack.b() == AliasedBlockItem.cC)
			itemStack = new ItemCooldownManager(AliasedBlockItem.j);
		else if (itemStack.b() == AliasedBlockItem.gi)
			itemStack = new ItemCooldownManager(AliasedBlockItem.i);
		return itemStack;
	}

	public static void destroyBlock(GameMode world, BlockPos pos, float effectChance) {
		destroyBlock(world, pos, effectChance, stack -> BeetrootsBlock.a(world, pos, stack));
	}

	public static void destroyBlock(GameMode world, BlockPos pos, float effectChance,
		Consumer<ItemCooldownManager> droppedItemCallback) {
		EmptyFluid FluidState = world.b(pos);
		BlockState state = world.d_(pos);
		if (world.t.nextFloat() < effectChance)
			world.syncWorldEvent(2001, pos, BeetrootsBlock.i(state));
		BeehiveBlockEntity tileentity = state.hasTileEntity() ? world.c(pos) : null;

		if (world.U()
			.b(ExplosionBehavior.f) && !world.restoringBlockSnapshots && world instanceof ServerWorld) {
			for (ItemCooldownManager itemStack : BeetrootsBlock.a(state, (ServerWorld) world, pos, tileentity))
				droppedItemCallback.accept(itemStack);
			state.a((ServerWorld) world, pos, ItemCooldownManager.tick);
		}

		world.a(pos, FluidState.g());
	}

	public static boolean isSolidWall(MobSpawnerLogic reader, BlockPos fromPos, Direction toDirection) {
		return hasBlockSolidSide(reader.d_(fromPos.offset(toDirection)), reader,
			fromPos.offset(toDirection), toDirection.getOpposite());
	}
	
	public static boolean noCollisionInSpace(MobSpawnerLogic reader, BlockPos pos) {
		return reader.d_(pos).k(reader, pos).b();
	}

	public static boolean hasBlockStateProperty(BlockState state, IntProperty<?> p) {
		return state.d(p).isPresent();
	}

	public static boolean hasBlockSolidSide(BlockState p_220056_0_, MobSpawnerLogic p_220056_1_, BlockPos p_220056_2_, Direction p_220056_3_) {
		return !p_220056_0_.a(StatHandler.I) && BeetrootsBlock.a(p_220056_0_.k(p_220056_1_, p_220056_2_), p_220056_3_);
	}

	public static boolean extinguishFire(GameMode world, @Nullable PlayerAbilities p_175719_1_, BlockPos p_175719_2_, Direction p_175719_3_) {
		p_175719_2_ = p_175719_2_.offset(p_175719_3_);
		if (world.d_(p_175719_2_).b() == BellBlock.bN) {
			world.a(p_175719_1_, 1009, p_175719_2_, 0);
			world.a(p_175719_2_, false);
			return true;
		} else {
			return false;
		}
	}
}
