package com.simibubi.kinetic_api.content.contraptions.processing.burner;

import java.util.Random;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllItems;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LitBlazeBurnerBlock extends BeetrootsBlock {

// 	1.16: add a soul fire variant

//	public enum FlameType implements IStringSerializable {
//		REGULAR, SOULFIRE;
//
//		@Override
//		public String getName() {
//			return Lang.asId(name());
//		}
//
//	}

	public LitBlazeBurnerBlock(c p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg blockRayTraceResult) {
		ItemCooldownManager heldItem = player.b(hand);

		// Check for 'Shovels'
		if (!heldItem.b(BellBlock.cC.n()))
			return Difficulty.PASS;

		world.a(player, pos, MusicType.eM, SoundEvent.e, .5f, 2);

		if (world.v)
			return Difficulty.SUCCESS;
		if (!player.b_())
			heldItem.a(1, player, p -> p.d(hand));

		world.a(pos, AllBlocks.BLAZE_BURNER.getDefaultState());
		return Difficulty.SUCCESS;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic reader, BlockPos pos, ArrayVoxelShape context) {
		return AllBlocks.BLAZE_BURNER.get()
			.b(state, reader, pos, context);
	}

	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
		PlayerAbilities player) {
		return AllItems.EMPTY_BLAZE_BURNER.asStack();
	}

	@Environment(EnvType.CLIENT)
	public void a(PistonHandler p_180655_1_, GameMode world, BlockPos pos, Random random) {
		world.b(ParticleTypes.LARGE_SMOKE, true,
			(double) pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1),
			(double) pos.getY() + random.nextDouble() + random.nextDouble(),
			(double) pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), 0.0D,
			0.07D, 0.0D);

		if (random.nextInt(10) == 0) {
			world.a((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
				(double) ((float) pos.getZ() + 0.5F), MusicType.bp, SoundEvent.e,
				0.25F + random.nextFloat() * .25f, random.nextFloat() * 0.7F + 0.6F, false);
		}

		if (random.nextInt(5) == 0) {
			for (int i = 0; i < random.nextInt(1) + 1; ++i) {
				world.addParticle(ParticleTypes.LAVA, (double) ((float) pos.getX() + 0.5F),
					(double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F),
					(double) (random.nextFloat() / 2.0F), 5.0E-5D, (double) (random.nextFloat() / 2.0F));
			}
		}
	}
	
	@Override
	public boolean a(PistonHandler p_149740_1_) {
		return true;
	}
	
	@Override
	public int a(PistonHandler state, GameMode p_180641_2_, BlockPos p_180641_3_) {
		return 1;
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic reader, BlockPos pos,
		ArrayVoxelShape context) {
		return AllBlocks.BLAZE_BURNER.get()
			.c(state, reader, pos, context);
	}

}
