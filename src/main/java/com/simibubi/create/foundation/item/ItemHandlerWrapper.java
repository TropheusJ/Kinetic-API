package com.simibubi.create.foundation.item;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemHandlerWrapper implements IItemHandlerModifiable {

	private IItemHandlerModifiable wrapped;

	public ItemHandlerWrapper(IItemHandlerModifiable wrapped) {
		this.wrapped = wrapped;
	}
	
	@Override
	public int getSlots() {
		return wrapped.getSlots();
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return wrapped.getStackInSlot(slot);
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		return wrapped.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		return wrapped.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return wrapped.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return wrapped.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemCooldownManager stack) {
		wrapped.setStackInSlot(slot, stack);
	}

}
