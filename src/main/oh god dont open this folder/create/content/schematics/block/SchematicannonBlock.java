package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.fml.network.NetworkHooks;

public class SchematicannonBlock extends BeetrootsBlock implements ITE<SchematicannonTileEntity> {

	public SchematicannonBlock(c properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.SCHEMATICANNON.create();
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.c;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.SCHEMATICANNON_SHAPE;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
			dcg hit) {
		if (worldIn.v)
			return Difficulty.SUCCESS;
		withTileEntityDo(worldIn, pos,
				te -> NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer));
		return Difficulty.SUCCESS;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.b() == newState.b())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		worldIn.o(pos);
	}

	@Override
	public Class<SchematicannonTileEntity> getTileEntityClass() {
		return SchematicannonTileEntity.class;
	}

}
