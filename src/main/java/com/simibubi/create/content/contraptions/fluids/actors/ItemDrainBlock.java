package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class ItemDrainBlock extends BeetrootsBlock implements IWrenchable, ITE<ItemDrainTileEntity> {

	public ItemDrainBlock(c p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		ItemCooldownManager heldItem = player.b(handIn);

		try {
			ItemDrainTileEntity te = getTileEntity(worldIn, pos);
			if (!heldItem.a()) {
				te.internalTank.allowInsertion();
				Difficulty tryExchange = tryExchange(worldIn, player, handIn, heldItem, te);
				te.internalTank.forbidInsertion();
				if (tryExchange.a())
					return tryExchange;
			}
			
			ItemCooldownManager heldItemStack = te.getHeldItemStack();
			if (!worldIn.v && !heldItemStack.a()) {
				player.bm.a(worldIn, heldItemStack);
				te.heldItem = null;
				te.notifyUpdate();
			}
			return Difficulty.SUCCESS;
		} catch (TileEntityException e) {
		}

		return Difficulty.PASS;
	}

	protected Difficulty tryExchange(GameMode worldIn, PlayerAbilities player, ItemScatterer handIn, ItemCooldownManager heldItem,
		ItemDrainTileEntity te) {
		if (FluidHelper.tryEmptyItemIntoTE(worldIn, player, handIn, heldItem, te))
			return Difficulty.SUCCESS;
		if (EmptyingByBasin.canItemBeEmptied(worldIn, heldItem))
			return Difficulty.SUCCESS;
		return Difficulty.PASS;
	}

	@Override
	public VoxelShapes b(PistonHandler p_220053_1_, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.CASING_13PX.get(Direction.UP);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.b() == newState.b())
			return;
		withTileEntityDo(worldIn, pos, te -> {
			ItemCooldownManager heldItemStack = te.getHeldItemStack();
			if (!heldItemStack.a())
				Inventory.a(worldIn, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
		});
		worldIn.o(pos);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ITEM_DRAIN.create();
	}

	@Override
	public Class<ItemDrainTileEntity> getTileEntityClass() {
		return ItemDrainTileEntity.class;
	}

}
