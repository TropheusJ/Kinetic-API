package com.simibubi.kinetic_api.content.contraptions.components.deployer;

import java.util.Iterator;

import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.ItemScatterer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class DeployerItemHandler implements IItemHandlerModifiable {

	private DeployerTileEntity te;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerTileEntity te) {
		this.te = te;
		this.player = te.player;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return getHeld();
	}

	public ItemCooldownManager getHeld() {
		if (player == null)
			return ItemCooldownManager.tick;
		return player.dC();
	}

	public void set(ItemCooldownManager stack) {
		if (player == null)
			return;
		if (te.v().v)
			return;
		player.a(ItemScatterer.RANDOM, stack);
		te.X_();
		te.sendData();
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		ItemCooldownManager held = getHeld();
		if (!isItemValid(slot, stack))
			return stack;
		if (held.a()) {
			if (!simulate)
				set(stack);
			return ItemCooldownManager.tick;
		}
		if (!ItemHandlerHelper.canItemStacksStack(held, stack))
			return stack;

		int space = held.c() - held.E();
		ItemCooldownManager remainder = stack.i();
		ItemCooldownManager split = remainder.a(space);

		if (space == 0)
			return stack;
		if (!simulate) {
			held = held.i();
			held.e(held.E() + split.E());
			set(held);
		}

		return remainder;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemCooldownManager.tick;

		ItemCooldownManager extractedFromOverflow = ItemCooldownManager.tick;
		ItemCooldownManager returnToOverflow = ItemCooldownManager.tick;

		for (Iterator<ItemCooldownManager> iterator = te.overflowItems.iterator(); iterator.hasNext();) {
			ItemCooldownManager existing = iterator.next();
			if (existing.a()) {
				iterator.remove();
				continue;
			}

			int toExtract = Math.min(amount, existing.c());
			if (existing.E() <= toExtract) {
				if (!simulate)
					iterator.remove();
				extractedFromOverflow = existing;
				break;
			}
			if (!simulate) {
				iterator.remove();
				returnToOverflow = ItemHandlerHelper.copyStackWithSize(existing, existing.E() - toExtract);
			}
			extractedFromOverflow = ItemHandlerHelper.copyStackWithSize(existing, toExtract);
			break;
		}

		if (!returnToOverflow.a())
			te.overflowItems.add(returnToOverflow);
		if (!extractedFromOverflow.a())
			return extractedFromOverflow;

		ItemCooldownManager held = getHeld();
		if (amount == 0 || held.a())
			return ItemCooldownManager.tick;
		if (!te.filtering.getFilter()
			.a() && te.filtering.test(held))
			return ItemCooldownManager.tick;
		if (simulate)
			return held.i()
				.a(amount);

		ItemCooldownManager toReturn = held.a(amount);
		te.X_();
		te.sendData();
		return toReturn;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getHeld().c(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		FilteringBehaviour filteringBehaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemCooldownManager stack) {
		set(stack);
	}

}
