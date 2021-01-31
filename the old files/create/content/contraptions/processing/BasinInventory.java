package com.simibubi.kinetic_api.content.contraptions.processing;

import com.simibubi.kinetic_api.foundation.item.SmartInventory;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasinInventory extends SmartInventory {

	public BasinInventory(int slots, BasinTileEntity te) {
		super(slots, te, 16, true);
	}
	
	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		// Only insert if no other slot already has a stack of this item
		for (int i = 0; i < getSlots(); i++) 
			if (i != slot && ItemHandlerHelper.canItemStacksStack(stack, inv.getStackInSlot(i)))
				return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
