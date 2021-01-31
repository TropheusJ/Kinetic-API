package com.simibubi.kinetic_api.content.logistics.block.inventories;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.fml.network.NetworkHooks;

public class AdjustableCrateBlock extends CrateBlock {

	public AdjustableCrateBlock(c p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ADJUSTABLE_CRATE.create();
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (oldState.b() != state.b() && state.hasTileEntity() && state.c(DOUBLE)
				&& state.c(SHAPE).getDirection() == AxisDirection.POSITIVE) {
			BeehiveBlockEntity tileEntity = worldIn.c(pos);
			if (!(tileEntity instanceof AdjustableCrateTileEntity))
				return;

			AdjustableCrateTileEntity te = (AdjustableCrateTileEntity) tileEntity;
			AdjustableCrateTileEntity other = te.getOtherCrate();
			if (other == null)
				return;

			for (int slot = 0; slot < other.inventory.getSlots(); slot++) {
				te.inventory.setStackInSlot(slot, other.inventory.getStackInSlot(slot));
				other.inventory.setStackInSlot(slot, ItemCooldownManager.tick);
			}
			te.allowedAmount = other.allowedAmount;
			other.invHandler.invalidate();
		}
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
			dcg hit) {

		if (worldIn.v) {
			return Difficulty.SUCCESS;
		} else {
			BeehiveBlockEntity te = worldIn.c(pos);
			if (te instanceof AdjustableCrateTileEntity) {
				AdjustableCrateTileEntity fte = (AdjustableCrateTileEntity) te;
				fte = fte.getMainCrate();
				NetworkHooks.openGui((ServerPlayerEntity) player, fte, fte::sendToContainer);
			}
			return Difficulty.SUCCESS;
		}
	}

	public static void splitCrate(GameMode world, BlockPos pos) {
		PistonHandler state = world.d_(pos);
		if (!AllBlocks.ADJUSTABLE_CRATE.has(state))
			return;
		if (!state.c(DOUBLE))
			return;
		BeehiveBlockEntity te = world.c(pos);
		if (!(te instanceof AdjustableCrateTileEntity))
			return;
		AdjustableCrateTileEntity crateTe = (AdjustableCrateTileEntity) te;
		crateTe.onSplit();
		world.a(pos, state.a(DOUBLE, false));
		world.a(crateTe.getOtherCrate().o(), state.a(DOUBLE, false));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!(worldIn.c(pos) instanceof AdjustableCrateTileEntity))
			return;

		if (state.hasTileEntity() && state.b() != newState.b()) {
			AdjustableCrateTileEntity te = (AdjustableCrateTileEntity) worldIn.c(pos);
			if (!isMoving)
				te.onDestroyed();
			worldIn.o(pos);
		}

	}

	@Override
	public boolean a(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler blockState, GameMode worldIn, BlockPos pos) {
		BeehiveBlockEntity te = worldIn.c(pos);
		if (te instanceof AdjustableCrateTileEntity) {
			AdjustableCrateTileEntity flexcrateTileEntity = (AdjustableCrateTileEntity) te;
			return ItemHelper.calcRedstoneFromInventory(flexcrateTileEntity.inventory);
		}
		return 0;
	}

}
