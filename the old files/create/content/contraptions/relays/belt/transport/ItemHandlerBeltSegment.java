package com.simibubi.kinetic_api.content.contraptions.relays.belt.transport;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerBeltSegment implements IItemHandler {

	private final BeltInventory beltInventory;
	int offset;

	public ItemHandlerBeltSegment(BeltInventory beltInventory, int offset) {
		this.beltInventory = beltInventory;
		this.offset = offset;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		TransportedItemStack stackAtOffset = this.beltInventory.getStackAtOffset(offset);
		if (stackAtOffset == null)
			return ItemCooldownManager.tick;
		return stackAtOffset.stack;
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		if (this.beltInventory.canInsertAt(offset)) {
			if (!simulate) {
				TransportedItemStack newStack = new TransportedItemStack(stack);
				newStack.insertedAt = offset;
				newStack.beltPosition = offset + .5f + (beltInventory.beltMovementPositive ? -1 : 1) / 16f;
				newStack.prevBeltPosition = newStack.beltPosition;
				this.beltInventory.addItem(newStack);
				this.beltInventory.belt.X_();
				this.beltInventory.belt.sendData();
			}
			return ItemCooldownManager.tick;
		}
		return stack;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
		if (transported == null)
			return ItemCooldownManager.tick;

		amount = Math.min(amount, transported.stack.E());
		ItemCooldownManager extracted = simulate ? transported.stack.i().a(amount) : transported.stack.a(amount);
		if (!simulate) {
			this.beltInventory.belt.X_();
			this.beltInventory.belt.sendData();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getStackInSlot(slot).c(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return true;
	}

}