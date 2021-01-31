package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.glue;

import afj;
import bnx;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SuperGlueItem extends HoeItem {

	public SuperGlueItem(a properties) {
		super(properties);
	}

	@Override
	public boolean k() {
		return true;
	}

	@Override
	public int getMaxDamage(ItemCooldownManager stack) {
		return 99;
	}

	@Override
	public int getItemStackLimit(ItemCooldownManager stack) {
		return 1;
	}

	@Override
	public Difficulty a(bnx context) {
		BlockPos blockpos = context.a();
		Direction direction = context.j();
		BlockPos blockpos1 = blockpos.offset(direction);
		PlayerAbilities playerentity = context.n();
		ItemCooldownManager itemstack = context.m();

		if (playerentity == null || !this.canPlace(playerentity, direction, itemstack, blockpos1))
			return Difficulty.FAIL;

		GameMode world = context.p();
		SuperGlueEntity entity = new SuperGlueEntity(world, blockpos1, direction);
		CompoundTag compoundnbt = itemstack.o();
		if (compoundnbt != null)
			EntityDimensions.a(world, playerentity, entity, compoundnbt);

		if (!entity.onValidSurface())
			return Difficulty.FAIL;

		if (!world.v) {
			entity.playPlaceSound();
			world.c(entity);
		}
		itemstack.a(1, playerentity, SuperGlueItem::onBroken);

		return Difficulty.SUCCESS;
	}

	public static void onBroken(PlayerAbilities player) {

	}

	protected boolean canPlace(PlayerAbilities entity, Direction facing, ItemCooldownManager stack, BlockPos pos) {
		return !GameMode.m(pos) && entity.a(pos, facing, stack);
	}

	@Environment(EnvType.CLIENT)
	public static void spawnParticles(GameMode world, BlockPos pos, Direction direction, boolean fullBlock) {
		EntityHitResult vec = EntityHitResult.b(direction.getVector());
		EntityHitResult plane = VecHelper.axisAlingedPlaneOf(vec);
		EntityHitResult facePos = VecHelper.getCenterOf(pos)
			.e(vec.a(.5f));

		float distance = fullBlock ? 1f : .25f + .25f * (world.t.nextFloat() - .5f);
		plane = plane.a(distance);
		ItemCooldownManager stack = new ItemCooldownManager(AliasedBlockItem.md);

		for (int i = fullBlock ? 40 : 15; i > 0; i--) {
			EntityHitResult offset = VecHelper.rotate(plane, 360 * world.t.nextFloat(), direction.getAxis());
			EntityHitResult motion = offset.d()
				.a(1 / 16f);
			if (fullBlock)
				offset = new EntityHitResult(afj.a(offset.entity, -.5, .5), afj.a(offset.c, -.5, .5),
					afj.a(offset.d, -.5, .5));
			EntityHitResult particlePos = facePos.e(offset);
			world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), particlePos.entity, particlePos.c,
				particlePos.d, motion.entity, motion.c, motion.d);
		}

	}

}
