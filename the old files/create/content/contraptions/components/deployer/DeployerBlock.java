package com.simibubi.kinetic_api.content.contraptions.components.deployer;

import javax.annotation.ParametersAreNonnullByDefault;
import bnx;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeployerBlock extends DirectionalAxisKineticBlock implements ITE<DeployerTileEntity> {

	public DeployerBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.DEPLOYER.create();
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.a;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.CASING_12PX.get(state.c(FACING));
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (context.j() == state.c(FACING)) {
			if (!context.p().v)
				withTileEntityDo(context.p(), context.a(), DeployerTileEntity::changeMode);
			return Difficulty.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.hasTileEntity() && state.b() != newState.b()) {
			withTileEntityDo(worldIn, pos, te -> {
				if (te.player != null && !isMoving) {
					te.player.bm.k();
					te.overflowItems.forEach(itemstack -> te.player.a(itemstack, true, false));
					te.player.ac();
					te.player = null;
				}
			});

			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.o(pos);
		}
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		ItemCooldownManager heldByPlayer = player.b(handIn)
			.i();
		if (AllItems.WRENCH.isIn(heldByPlayer))
			return Difficulty.PASS;

		if (hit.b() != state.c(FACING))
			return Difficulty.PASS;
		if (worldIn.v)
			return Difficulty.SUCCESS;

		withTileEntityDo(worldIn, pos, te -> {
			ItemCooldownManager heldByDeployer = te.player.dC()
				.i();
			if (heldByDeployer.a() && heldByPlayer.a())
				return;

			player.a(handIn, heldByDeployer);
			te.player.a(ItemScatterer.RANDOM, heldByPlayer);
			te.sendData();
		});

		return Difficulty.SUCCESS;
	}

	@Override
	public Class<DeployerTileEntity> getTileEntityClass() {
		return DeployerTileEntity.class;
	}
	
	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		super.b(state, world, pos, oldState, isMoving);
		withTileEntityDo(world, pos, DeployerTileEntity::redstoneUpdate);
	}
	
	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		withTileEntityDo(world, pos, DeployerTileEntity::redstoneUpdate);
	}

}
