package com.simibubi.kinetic_api.content.logistics.block.inventories;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ItemStackHandler {

	private Supplier<ItemCooldownManager> suppliedItemStack;

	public BottomlessItemHandler(Supplier<ItemCooldownManager> suppliedItemStack) {
		this.suppliedItemStack = suppliedItemStack;
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		ItemCooldownManager stack = suppliedItemStack.get();
		if (slot == 1)
			return ItemCooldownManager.tick;
		if (stack == null)
			return ItemCooldownManager.tick;
		if (!stack.a())
			return ItemHandlerHelper.copyStackWithSize(stack, stack.c());
		return stack;
	}

	@Override
	public void setStackInSlot(int slot, ItemCooldownManager stack) {}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		return ItemCooldownManager.tick;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		ItemCooldownManager stack = suppliedItemStack.get();
		if (slot == 1)
			return ItemCooldownManager.tick;
		if (stack == null)
			return ItemCooldownManager.tick;
		if (!stack.a())
			return ItemHandlerHelper.copyStackWithSize(stack, Math.min(stack.c(), amount));
		return ItemCooldownManager.tick;
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return true;
	}
}
