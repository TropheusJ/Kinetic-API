package com.simibubi.kinetic_api.content.contraptions.processing;

import java.util.function.Consumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;

public class ProcessingInventory extends ItemStackHandler {
	public float remainingTime;
	public float recipeDuration;
	public boolean appliedRecipe;
	public Consumer<ItemCooldownManager> callback;

	public ProcessingInventory(Consumer<ItemCooldownManager> callback) {
		super(10);
		this.callback = callback;
	}

	public void clear() {
		for (int i = 0; i < getSlots(); i++)
			setStackInSlot(i, ItemCooldownManager.tick);
		remainingTime = 0;
		recipeDuration = 0;
		appliedRecipe = false;
	}

	public boolean isEmpty() {
		for (int i = 0; i < getSlots(); i++)
			if (!getStackInSlot(i).a())
				return false;
		return true;
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		ItemCooldownManager insertItem = super.insertItem(slot, stack, simulate);
		if (slot == 0 && !insertItem.equals(stack, true))
			callback.accept(insertItem.i());
		return insertItem;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		nbt.putFloat("ProcessingTime", remainingTime);
		nbt.putFloat("RecipeTime", recipeDuration);
		nbt.putBoolean("AppliedRecipe", appliedRecipe);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		remainingTime = nbt.getFloat("ProcessingTime");
		recipeDuration = nbt.getFloat("RecipeTime");
		appliedRecipe = nbt.getBoolean("AppliedRecipe");
		super.deserializeNBT(nbt);
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		return ItemCooldownManager.tick;
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return slot == 0 && isEmpty();
	}

}