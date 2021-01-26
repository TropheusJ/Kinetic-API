package com.simibubi.create.content.logistics.block.depot;

import javax.annotation.ParametersAreNonnullByDefault;
import apx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
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
import net.minecraftforge.items.ItemStackHandler;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DepotBlock extends BeetrootsBlock implements ITE<DepotTileEntity>, IWrenchable {

	public DepotBlock(c p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public VoxelShapes b(PistonHandler p_220053_1_, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.DEPOT;
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.DEPOT.create();
	}

	@Override
	public Class<DepotTileEntity> getTileEntityClass() {
		return DepotTileEntity.class;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg ray) {
		if (ray.b() != Direction.UP)
			return Difficulty.PASS;
		if (world.v)
			return Difficulty.SUCCESS;

		withTileEntityDo(world, pos, te -> {
			ItemCooldownManager heldItem = player.b(hand);
			boolean wasEmptyHanded = heldItem.a();
			boolean shouldntPlaceItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem);

			ItemCooldownManager mainItemStack = te.getHeldItemStack();
			if (!mainItemStack.a()) {
				player.bm.a(world, mainItemStack);
				te.setHeldItem(null);
			}
			ItemStackHandler outputs = te.processingOutputBuffer;
			for (int i = 0; i < outputs.getSlots(); i++)
				player.bm.a(world, outputs.extractItem(i, 64, false));

			if (!wasEmptyHanded && !shouldntPlaceItem) {
				TransportedItemStack transported = new TransportedItemStack(heldItem);
				transported.insertedFrom = player.bY();
				transported.prevBeltPosition = .25f;
				transported.beltPosition = .25f;
				te.setHeldItem(transported);
				player.a(hand, ItemCooldownManager.tick);
			}

			te.X_();
			te.sendData();
		});

		return Difficulty.SUCCESS;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.b() == newState.b()) 
			return;
		withTileEntityDo(worldIn, pos, te -> {
			ItemHelper.dropContents(worldIn, pos, te.processingOutputBuffer);
			if (!te.getHeldItemStack()
				.a())
				Inventory.a(worldIn, pos.getX(), pos.getY(), pos.getZ(), te.getHeldItemStack());
		});
		worldIn.o(pos);
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);
		if (!AllBlocks.DEPOT.has(worldIn.d_(entityIn.cA())))
			return;
		if (!(entityIn instanceof PaintingEntity))
			return;
		if (!entityIn.aW())
			return;
		if (entityIn.l.v)
			return;
		PaintingEntity itemEntity = (PaintingEntity) entityIn;
		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(worldIn, entityIn.cA(), DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour == null)
			return;
		ItemCooldownManager remainder = inputBehaviour.handleInsertion(itemEntity.g(), Direction.DOWN, false);
		itemEntity.b(remainder);
		if (remainder.a())
			itemEntity.ac();
	}

	@Override
	public boolean a(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler blockState, GameMode worldIn, BlockPos pos) {
		try {
			return ItemHelper.calcRedstoneFromInventory(getTileEntity(worldIn, pos).itemHandler);
		} catch (TileEntityException ignored) {
		}
		return 0;
	}

}
