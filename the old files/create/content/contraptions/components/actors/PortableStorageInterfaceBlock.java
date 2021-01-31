package com.simibubi.kinetic_api.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.block.ProperDirectionalBlock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PortableStorageInterfaceBlock extends ProperDirectionalBlock
	implements ITE<PortableStorageInterfaceTileEntity> {

	boolean fluids;

	public static PortableStorageInterfaceBlock forItems(c p_i48415_1_) {
		return new PortableStorageInterfaceBlock(p_i48415_1_, false);
	}

	public static PortableStorageInterfaceBlock forFluids(c p_i48415_1_) {
		return new PortableStorageInterfaceBlock(p_i48415_1_, true);
	}

	private PortableStorageInterfaceBlock(c p_i48415_1_, boolean fluids) {
		super(p_i48415_1_);
		this.fluids = fluids;
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return (fluids ? AllTileEntities.PORTABLE_FLUID_INTERFACE : AllTileEntities.PORTABLE_STORAGE_INTERFACE)
			.create();
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		withTileEntityDo(world, pos, PortableStorageInterfaceTileEntity::neighbourChanged);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return n().a(SHAPE, context.d()
			.getOpposite());
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.PORTABLE_STORAGE_INTERFACE.get(state.c(SHAPE));
	}

	@Override
	public boolean a(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler blockState, GameMode worldIn, BlockPos pos) {
		return getTileEntityOptional(worldIn, pos).map(te -> te.isConnected() ? 15 : 0)
			.orElse(0);
	}

	@Override
	public Class<PortableStorageInterfaceTileEntity> getTileEntityClass() {
		return PortableStorageInterfaceTileEntity.class;
	}

}
