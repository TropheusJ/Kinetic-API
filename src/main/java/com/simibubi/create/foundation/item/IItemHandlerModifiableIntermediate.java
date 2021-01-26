package com.simibubi.create.foundation.item;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.IItemHandlerModifiable;

interface IItemHandlerModifiableIntermediate extends IItemHandlerModifiable {
	
	@Override
	public default ItemCooldownManager getStackInSlot(int slot) {
		return getStackInSlotIntermediate(slot);
	}
	
	public ItemCooldownManager getStackInSlotIntermediate(int slot);
	
}