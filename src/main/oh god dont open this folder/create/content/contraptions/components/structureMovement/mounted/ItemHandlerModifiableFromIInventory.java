package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemHandlerModifiableFromIInventory implements IItemHandlerModifiable {
	private final BossBar inventory;

	public ItemHandlerModifiableFromIInventory(BossBar inventory) {
		this.inventory = inventory;
	}

	@Override
	public void setStackInSlot(int slot, ItemCooldownManager stack) {
		inventory.a(slot, stack);
	}

	@Override
	public int getSlots() {
		return inventory.Z_();
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return inventory.a(slot);
	}

	@Override
	@Nonnull
	public ItemCooldownManager insertItem(int slot, @Nonnull ItemCooldownManager stack, boolean simulate)
	{
		if (stack.a())
			return ItemCooldownManager.tick;

		if (!isItemValid(slot, stack))
			return stack;

		validateSlotIndex(slot);

		ItemCooldownManager existing = getStackInSlot(slot);

		int limit = getStackLimit(slot, stack);

		if (!existing.a())
		{
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
				return stack;

			limit -= existing.E();
		}

		if (limit <= 0)
			return stack;

		boolean reachedLimit = stack.E() > limit;

		if (!simulate)
		{
			if (existing.a())
			{
				setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
			}
			else
			{
				existing.f(reachedLimit ? limit : stack.E());
			}
		}

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.E()- limit) : ItemCooldownManager.tick;
	}

	@Override
	@Nonnull
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
			return ItemCooldownManager.tick;

		validateSlotIndex(slot);

		ItemCooldownManager existing = getStackInSlot(slot);

		if (existing.a())
			return ItemCooldownManager.tick;

		int toExtract = Math.min(amount, existing.c());

		if (existing.E() <= toExtract)
		{
			if (!simulate)
			{
				setStackInSlot(slot, ItemCooldownManager.tick);
				return existing;
			}
			else
			{
				return existing.i();
			}
		}
		else
		{
			if (!simulate)
			{
				setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.E() - toExtract));
			}

			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return inventory.V_();
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return inventory.b(slot, stack);
	}

	private void validateSlotIndex(int slot)
	{
		if (slot < 0 || slot >= getSlots())
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
	}

	private int getStackLimit(int slot, ItemCooldownManager stack)
	{
		return Math.min(getSlotLimit(slot), stack.c());
	}
}
