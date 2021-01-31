package com.simibubi.kinetic_api.content.contraptions.processing;

import apx;
import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class BasinBlock extends BeetrootsBlock implements ITE<BasinTileEntity>, IWrenchable {

	public static final BooleanProperty FACING = BambooLeaves.N;

	public BasinBlock(c p_i48440_1_) {
		super(p_i48440_1_);
		j(n().a(FACING, Direction.DOWN));
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		super.a(p_206840_1_.a(FACING));
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible world, BlockPos pos) {
		BeehiveBlockEntity tileEntity = world.c(pos.up());
		if (tileEntity instanceof BasinOperatingTileEntity)
			return false;
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.BASIN.create();
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (!context.p().v)
			withTileEntityDo(context.p(), context.a(), bte -> bte.onWrenched(context.j()));
		return Difficulty.SUCCESS;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		ItemCooldownManager heldItem = player.b(handIn);

		try {
			BasinTileEntity te = getTileEntity(worldIn, pos);
			if (!heldItem.a()) {
				if (FluidHelper.tryEmptyItemIntoTE(worldIn, player, handIn, heldItem, te))
					return Difficulty.SUCCESS;
				if (FluidHelper.tryFillItemFromTE(worldIn, player, handIn, heldItem, te))
					return Difficulty.SUCCESS;

				if (EmptyingByBasin.canItemBeEmptied(worldIn, heldItem)
					|| GenericItemFilling.canItemBeFilled(worldIn, heldItem))
					return Difficulty.SUCCESS;
				return Difficulty.PASS;
			}

			IItemHandlerModifiable inv = te.itemCapability.orElse(new ItemStackHandler(1));
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				player.bm.a(worldIn, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemCooldownManager.tick);
			}
			te.onEmptied();
		} catch (TileEntityException e) {
		}

		return Difficulty.SUCCESS;
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);
		if (!AllBlocks.BASIN.has(worldIn.d_(entityIn.cA())))
			return;
		if (!(entityIn instanceof PaintingEntity))
			return;
		if (!entityIn.aW())
			return;
		PaintingEntity itemEntity = (PaintingEntity) entityIn;
		withTileEntityDo(worldIn, entityIn.cA(), te -> {
			ItemCooldownManager insertItem = ItemHandlerHelper.insertItem(te.inputInventory, itemEntity.g()
				.i(), false);
			if (insertItem.a()) {
				itemEntity.ac();
				if (!itemEntity.l.v)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.BASIN_THROW, itemEntity.l,
						itemEntity.cA(), 3);
				return;
			}

			itemEntity.b(insertItem);
		});
	}

	@Override
	public VoxelShapes a_(PistonHandler p_199600_1_, MobSpawnerLogic p_199600_2_, BlockPos p_199600_3_) {
		return AllShapes.BASIN_RAYTRACE_SHAPE;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.BASIN_BLOCK_SHAPE;
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic reader, BlockPos pos, ArrayVoxelShape ctx) {
		if (ctx.getEntity() instanceof PaintingEntity)
			return AllShapes.BASIN_COLLISION_SHAPE;
		return b(state, reader, pos, ctx);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.b() == newState.b())
			return;
		TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
		withTileEntityDo(worldIn, pos, te -> {
			ItemHelper.dropContents(worldIn, pos, te.inputInventory);
			ItemHelper.dropContents(worldIn, pos, te.outputInventory);
			te.spoutputBuffer.forEach(is -> BeetrootsBlock.a(worldIn, pos, is));
		});
		worldIn.o(pos);
	}

	@Override
	public boolean a(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler blockState, GameMode worldIn, BlockPos pos) {
		try {
			return ItemHelper.calcRedstoneFromInventory(getTileEntity(worldIn, pos).inputInventory);
		} catch (TileEntityException e) {
		}
		return 0;
	}

	@Override
	public Class<BasinTileEntity> getTileEntityClass() {
		return BasinTileEntity.class;
	}

	public static boolean canOutputTo(MobSpawnerLogic world, BlockPos basinPos, Direction direction) {
		BlockPos neighbour = basinPos.offset(direction);
		if (!world.d_(neighbour)
			.k(world, neighbour)
			.b())
			return false;

		BlockPos offset = basinPos.down()
			.offset(direction);
		DirectBeltInputBehaviour directBeltInputBehaviour =
			TileEntityBehaviour.get(world, offset, DirectBeltInputBehaviour.TYPE);
		if (directBeltInputBehaviour != null)
			return directBeltInputBehaviour.canInsertFromSide(direction);
		return false;
	}

}
