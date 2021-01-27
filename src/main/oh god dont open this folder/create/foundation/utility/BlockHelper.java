package com.simibubi.create.foundation.utility;

import afj;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.StatHandler;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockHelper {

	@Environment(EnvType.CLIENT)
	public static void addReducedDestroyEffects(PistonHandler state, GameMode worldIn, BlockPos pos, ItemPickupParticle manager) {
		if (!(worldIn instanceof DragonHeadEntityModel))
			return;
		DragonHeadEntityModel world = (DragonHeadEntityModel) worldIn;
		VoxelShapes voxelshape = state.j(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.b((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		voxelshape.b((x1, y1, z1, x2, y2, z2) -> {
			double d1 = Math.min(1.0D, x2 - x1);
			double d2 = Math.min(1.0D, y2 - y1);
			double d3 = Math.min(1.0D, z2 - z1);
			int i = Math.max(2, afj.f(d1 / 0.25D));
			int j = Math.max(2, afj.f(d2 / 0.25D));
			int k = Math.max(2, afj.f(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.t.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + x1;
						double d8 = d5 * d2 + y1;
						double d9 = d6 * d3 + z1;
						manager
							.a((new SoulParticle(world, (double) pos.getX() + d7, (double) pos.getY() + d8,
								(double) pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state)).a(pos));
					}
				}
			}

		});
	}

	public static PistonHandler setZeroAge(PistonHandler blockState) {
		if (hasBlockStateProperty(blockState, BambooLeaves.ae))
			return blockState.a(BambooLeaves.ae, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.af))
			return blockState.a(BambooLeaves.af, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.ag))
			return blockState.a(BambooLeaves.ag, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.ah))
			return blockState.a(BambooLeaves.ah, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.ai))
			return blockState.a(BambooLeaves.ai, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.aj))
			return blockState.a(BambooLeaves.aj, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.ak))
			return blockState.a(BambooLeaves.ak, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.au))
			return blockState.a(BambooLeaves.au, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.ap))
			return blockState.a(BambooLeaves.ap, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.aA))
			return blockState.a(BambooLeaves.aA, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.ar))
			return blockState.a(BambooLeaves.ar, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.as))
			return blockState.a(BambooLeaves.as, 0);
		if (hasBlockStateProperty(blockState, BambooLeaves.g))
			return blockState.a(BambooLeaves.g, false);
		return blockState;
	}

	public static int findAndRemoveInInventory(PistonHandler block, PlayerAbilities player, int amount) {
		int amountFound = 0;
		HoeItem required = getRequiredItem(block).b();

		boolean needsTwo =
			hasBlockStateProperty(block, BambooLeaves.aK) && block.c(BambooLeaves.aK) == Property.hashCodeCache;

		if (needsTwo)
			amount *= 2;

		if (hasBlockStateProperty(block, BambooLeaves.ao))
			amount *= block.c(BambooLeaves.ao);

		if (hasBlockStateProperty(block, BambooLeaves.ay))
			amount *= block.c(BambooLeaves.ay);

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

	public static ItemCooldownManager getRequiredItem(PistonHandler state) {
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
		PistonHandler state = world.d_(pos);
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

	public static boolean hasBlockStateProperty(PistonHandler state, IntProperty<?> p) {
		return state.d(p).isPresent();
	}

	public static boolean hasBlockSolidSide(PistonHandler p_220056_0_, MobSpawnerLogic p_220056_1_, BlockPos p_220056_2_, Direction p_220056_3_) {
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
