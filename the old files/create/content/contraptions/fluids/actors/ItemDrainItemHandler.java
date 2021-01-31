package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import com.simibubi.kinetic_api.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.Direction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemDrainItemHandler implements IItemHandler {

	private ItemDrainTileEntity te;
	private Direction side;

	public ItemDrainItemHandler(ItemDrainTileEntity te, Direction side) {
		this.te = te;
		this.side = side;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return te.getHeldItemStack();
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		if (!te.getHeldItemStack()
			.a())
			return stack;
		
		ItemCooldownManager returned = ItemCooldownManager.tick;
		if (stack.E() > 1 && EmptyingByBasin.canItemBeEmptied(te.v(), stack)) {
			returned = ItemHandlerHelper.copyStackWithSize(stack, stack.E() - 1);
			stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
		}
		
		if (!simulate) {
			TransportedItemStack heldItem = new TransportedItemStack(stack);
			heldItem.prevBeltPosition = 0;
			te.setHeldItem(heldItem, side.getOpposite());
			te.notifyUpdate();
		}
		
		return returned;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		TransportedItemStack held = te.heldItem;
		if (held == null)
			return ItemCooldownManager.tick;

		ItemCooldownManager stack = held.stack.i();
		ItemCooldownManager extracted = stack.a(amount);
		if (!simulate) {
			te.heldItem.stack = stack;
			if (stack.a())
				te.heldItem = null;
			te.notifyUpdate();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return true;
	}

}
