package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.IItemHandler;

public class DepotItemHandler implements IItemHandler {

	private static final int MAIN_SLOT = 0;
	private DepotTileEntity te;

	public DepotItemHandler(DepotTileEntity te) {
		this.te = te;
	}

	@Override
	public int getSlots() {
		return 9;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return slot == MAIN_SLOT ? te.getHeldItemStack() : te.processingOutputBuffer.getStackInSlot(slot - 1);
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		if (slot != MAIN_SLOT)
			return stack;
		if (!te.getHeldItemStack()
			.a())
			return stack;
		if (!te.isOutputEmpty())
			return stack;
		if (!simulate) {
			te.setHeldItem(new TransportedItemStack(stack));
			te.X_();
			te.sendData();
		}
		return ItemCooldownManager.tick;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		if (slot != MAIN_SLOT)
			return te.processingOutputBuffer.extractItem(slot - 1, amount, simulate);

		TransportedItemStack held = te.heldItem;
		if (held == null)
			return ItemCooldownManager.tick;
		ItemCooldownManager stack = held.stack.i();
		ItemCooldownManager extracted = stack.a(amount);
		if (!simulate) {
			te.heldItem.stack = stack;
			if (stack.a())
				te.heldItem = null;
			te.X_();
			te.sendData();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return slot == MAIN_SLOT;
	}

}
