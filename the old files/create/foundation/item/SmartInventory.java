package com.simibubi.kinetic_api.foundation.item;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.kinetic_api.foundation.tileEntity.SyncedTileEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SmartInventory extends RecipeWrapper
	implements IItemHandlerModifiableIntermediate, INBTSerializable<CompoundTag> {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected int stackSize;

	public SmartInventory(int slots, SyncedTileEntity te) {
		this(slots, te, 64, false);
	}

	public SmartInventory(int slots, SyncedTileEntity te, int stackSize, boolean stackNonStackables) {
		super(new SyncedStackHandler(slots, te, stackNonStackables, stackSize));
		this.stackNonStackables = stackNonStackables;
		insertionAllowed = true;
		extractionAllowed = true;
		this.stackSize = stackSize;
	}

	public SmartInventory whenContentsChanged(Consumer<Integer> updateCallback) {
		((SyncedStackHandler) inv).whenContentsChange(updateCallback);
		return this;
	}

	public SmartInventory allowInsertion() {
		insertionAllowed = true;
		return this;
	}

	public SmartInventory allowExtraction() {
		extractionAllowed = true;
		return this;
	}

	public SmartInventory forbidInsertion() {
		insertionAllowed = false;
		return this;
	}

	public SmartInventory forbidExtraction() {
		extractionAllowed = false;
		return this;
	}

	@Override
	public int getSlots() {
		return inv.getSlots();
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		if (!insertionAllowed)
			return stack;
		return inv.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		if (!extractionAllowed)
			return ItemCooldownManager.tick;
		if (stackNonStackables) {
			ItemCooldownManager extractItem = inv.extractItem(slot, amount, true);
			if (!extractItem.a() && extractItem.c() < extractItem.E())
				amount = extractItem.c();
		}
		return inv.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(inv.getSlotLimit(slot), stackSize);
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return inv.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemCooldownManager stack) {
		inv.setStackInSlot(slot, stack);
	}

	@Override
	public ItemCooldownManager a(int slot) {
		return super.a(slot);
	}

	public int getStackLimit(int slot, @Nonnull ItemCooldownManager stack) {
		return Math.min(getSlotLimit(slot), stack.c());
	}

	@Override
	public CompoundTag serializeNBT() {
		return getInv().serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		getInv().deserializeNBT(nbt);
	}

	private SyncedStackHandler getInv() {
		return (SyncedStackHandler) inv;
	}

	private static class SyncedStackHandler extends ItemStackHandler {

		private SyncedTileEntity te;
		private boolean stackNonStackables;
		private int stackSize;
		private Consumer<Integer> updateCallback;

		public SyncedStackHandler(int slots, SyncedTileEntity te, boolean stackNonStackables, int stackSize) {
			super(slots);
			this.te = te;
			this.stackNonStackables = stackNonStackables;
			this.stackSize = stackSize;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			if (updateCallback != null)
				updateCallback.accept(slot);
			te.notifyUpdate();
		}

		@Override
		public int getSlotLimit(int slot) {
			return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
		}

		public void whenContentsChange(Consumer<Integer> updateCallback) {
			this.updateCallback = updateCallback;
		}

	}

	@Override
	public ItemCooldownManager getStackInSlotIntermediate(int slot) {
		return a(slot);
	}

}
