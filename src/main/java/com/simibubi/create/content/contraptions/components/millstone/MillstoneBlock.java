package com.simibubi.create.content.contraptions.components.millstone;

import apx;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class MillstoneBlock extends KineticBlock implements ITE<MillstoneTileEntity> {

	public MillstoneBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MILLSTONE.create();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.MILLSTONE;
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
			dcg hit) {
		if (!player.b(handIn).a())
			return Difficulty.PASS;
		if (worldIn.v)
			return Difficulty.SUCCESS;

		withTileEntityDo(worldIn, pos, millstone -> {
			boolean emptyOutput = true;
			IItemHandlerModifiable inv = millstone.outputInv;
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemCooldownManager stackInSlot = inv.getStackInSlot(slot);
				if (!stackInSlot.a())
					emptyOutput = false;
				player.bm.a(worldIn, stackInSlot);
				inv.setStackInSlot(slot, ItemCooldownManager.tick);
			}

			if (emptyOutput) {
				inv = millstone.inputInv;
				for (int slot = 0; slot < inv.getSlots(); slot++) {
					player.bm.a(worldIn, inv.getStackInSlot(slot));
					inv.setStackInSlot(slot, ItemCooldownManager.tick);
				}
			}

			millstone.X_();
			millstone.sendData();
		});

		return Difficulty.SUCCESS;
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);

		if (entityIn.l.v)
			return;
		if (!(entityIn instanceof PaintingEntity))
			return;
		if (!entityIn.aW())
			return;

		MillstoneTileEntity millstone = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.cA())) {
			try {
				millstone = getTileEntity(worldIn, pos);
			} catch (TileEntityException e) {
				continue;
			}
		}
		if (millstone == null)
			return;

		PaintingEntity itemEntity = (PaintingEntity) entityIn;
		LazyOptional<IItemHandler> capability = millstone.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!capability.isPresent())
			return;

		ItemCooldownManager remainder = capability.orElse(new ItemStackHandler()).insertItem(0, itemEntity.g(), false);
		if (remainder.a())
			itemEntity.ac();
		if (remainder.E() < itemEntity.g().E())
			itemEntity.b(remainder);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.hasTileEntity() && state.b() != newState.b()) {
			withTileEntityDo(worldIn, pos, te -> {
				ItemHelper.dropContents(worldIn, pos, te.inputInv);
				ItemHelper.dropContents(worldIn, pos, te.outputInv);
			});

			worldIn.o(pos);
		}
	}

	@Override
	public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
		return true;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return Axis.Y;
	}

	@Override
	public Class<MillstoneTileEntity> getTileEntityClass() {
		return MillstoneTileEntity.class;
	}

}
