package com.simibubi.kinetic_api.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.foundation.block.ITE;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AnalogLeverBlock extends EndRodBlock implements ITE<AnalogLeverTileEntity> {

	public AnalogLeverBlock(c p_i48402_1_) {
		super(p_i48402_1_);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ANALOG_LEVER.create();
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
			dcg hit) {
		if (worldIn.v) {
			addParticles(state, worldIn, pos, 1.0F);
			return Difficulty.SUCCESS;
		}

		try {
			boolean sneak = player.bt();
			AnalogLeverTileEntity te = getTileEntity(worldIn, pos);
			te.changeState(sneak);
			float f = .25f + ((te.state + 5) / 15f) * .5f;
			worldIn.a(null, pos, MusicType.hb, SoundEvent.e, 0.2F, f);
		} catch (TileEntityException e) {}

		return Difficulty.SUCCESS;
	}

	@Override
	public int a(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		try {
			return getTileEntity(blockAccess, pos).state;
		} catch (TileEntityException e) {
			return 0;
		}
	}

	@Override
	public boolean b_(PistonHandler state) {
		return true;
	}

	@Override
	public int b(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		return h(blockState) == side ? a(blockState, blockAccess, pos, side) : 0;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(PistonHandler stateIn, GameMode worldIn, BlockPos pos, Random rand) {
		try {
			AnalogLeverTileEntity tileEntity = getTileEntity(worldIn, pos);
			if (tileEntity.state != 0 && rand.nextFloat() < 0.25F)
				addParticles(stateIn, worldIn, pos, 0.5F);
		} catch (TileEntityException e) {}
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		try {
			AnalogLeverTileEntity tileEntity = getTileEntity(worldIn, pos);
			if (!isMoving && state.b() != newState.b()) {
				if (tileEntity.state != 0)
					updateNeighbors(state, worldIn, pos);
				worldIn.o(pos);
			}
		} catch (TileEntityException e) {}
	}

	private static void addParticles(PistonHandler state, GrassColors worldIn, BlockPos pos, float alpha) {
		Direction direction = state.c(aq).getOpposite();
		Direction direction1 = h(state).getOpposite();
		double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getOffsetX()
				+ 0.2D * (double) direction1.getOffsetX();
		double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getOffsetY()
				+ 0.2D * (double) direction1.getOffsetY();
		double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getOffsetZ()
				+ 0.2D * (double) direction1.getOffsetZ();
		worldIn.addParticle(new DustParticleEffect(1.0F, 0.0F, 0.0F, alpha), d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}

	static void updateNeighbors(PistonHandler state, GameMode world, BlockPos pos) {
		world.b(pos, state.b());
		world.b(pos.offset(h(state).getOpposite()), state.b());
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return BellBlock.cp.b(state, worldIn, pos, context);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(aq, u));
	}

	@Override
	public Class<AnalogLeverTileEntity> getTileEntityClass() {
		return AnalogLeverTileEntity.class;
	}

}
