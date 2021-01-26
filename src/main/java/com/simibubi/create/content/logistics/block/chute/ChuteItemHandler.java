package com.simibubi.create.content.logistics.block.chute;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.IItemHandler;

public class ChuteItemHandler implements IItemHandler {

	private ChuteTileEntity te;

	public ChuteItemHandler(ChuteTileEntity te) {
		this.te = te;
	}
	
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return te.item;
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		if (!te.item.a())
			return stack;
		if (!simulate) 
			te.setItem(stack);
		return ItemCooldownManager.tick;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		ItemCooldownManager remainder = te.item.i();
		ItemCooldownManager split = remainder.a(amount);
		if (!simulate) 
			te.setItem(remainder);
		return split;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(64, getStackInSlot(slot).c());
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return true;
	}

}
