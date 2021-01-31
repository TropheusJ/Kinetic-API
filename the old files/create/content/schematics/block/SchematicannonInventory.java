package com.simibubi.kinetic_api.content.schematics.block;

import com.simibubi.kinetic_api.AllItems;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicannonInventory extends ItemStackHandler {
	/**
	 * 
	 */
	private final SchematicannonTileEntity te;

	public SchematicannonInventory(SchematicannonTileEntity schematicannonTileEntity) {
		super(5);
		te = schematicannonTileEntity;
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		te.X_();
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		switch (slot) {
		case 0: // Blueprint Slot
			return AllItems.SCHEMATIC.isIn(stack);
		case 1: // Blueprint output
			return false;
		case 2: // Book input
			return stack.a(new ItemCooldownManager(AliasedBlockItem.mc)) || stack.a(new ItemCooldownManager(AliasedBlockItem.oU));
		case 3: // Material List output
			return false;
		case 4: // Gunpowder
			return stack.a(new ItemCooldownManager(AliasedBlockItem.kU));
		default:
			return super.isItemValid(slot, stack);
		}
	}
}