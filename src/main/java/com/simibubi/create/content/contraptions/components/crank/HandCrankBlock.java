package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.config.AllConfigs;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HandCrankBlock extends DirectionalKineticBlock implements ITE<HandCrankTileEntity> {

	public HandCrankBlock(c properties) {
		super(properties);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.CRANK.get(state.c(FACING));
	}
	
	@Environment(EnvType.CLIENT)
	public AllBlockPartials getRenderedHandle() {
		return AllBlockPartials.HAND_CRANK_HANDLE;
	}
	
	public int getRotationSpeed() {
		return 32;
	}

	@Override
	public RedstoneLampBlock b(PistonHandler state) {
		return RedstoneLampBlock.b;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		boolean handEmpty = player.b(handIn)
			.a();

		if (!handEmpty && player.bt())
			return Difficulty.PASS;

		withTileEntityDo(worldIn, pos, te -> te.turn(player.bt()));
		player.t(getRotationSpeed() * AllConfigs.SERVER.kinetics.crankHungerMultiplier.getF());
		return Difficulty.SUCCESS;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction preferred = getPreferredFacing(context);
		if (preferred == null || (context.n() != null && context.n()
			.bt()))
			return n().a(FACING, context.j());
		return n().a(FACING, preferred.getOpposite());
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		Direction facing = state.c(FACING)
			.getOpposite();
		BlockPos neighbourPos = pos.offset(facing);
		PistonHandler neighbour = worldIn.d_(neighbourPos);
		return !neighbour.k(worldIn, neighbourPos)
			.b();
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.v)
			return;

		Direction blockFacing = state.c(FACING);
		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!a(state, worldIn, pos)) {
				worldIn.b(pos, true);
				return;
			}
		}
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.HAND_CRANK.create();
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(FACING)
			.getOpposite();
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(FACING)
			.getAxis();
	}

	@Override
	public Class<HandCrankTileEntity> getTileEntityClass() {
		return HandCrankTileEntity.class;
	}

}
