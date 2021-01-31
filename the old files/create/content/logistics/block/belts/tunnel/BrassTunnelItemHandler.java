package com.simibubi.kinetic_api.content.logistics.block.belts.tunnel;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class BrassTunnelItemHandler implements IItemHandler {

	private BrassTunnelTileEntity te;

	public BrassTunnelItemHandler(BrassTunnelTileEntity te) {
		this.te = te;
	}
	
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemCooldownManager getStackInSlot(int slot) {
		return te.stackToDistribute;
	}

	@Override
	public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
		if (!te.hasDistributionBehaviour()) {
			LazyOptional<IItemHandler> beltCapability = te.getBeltCapability();
			if (!beltCapability.isPresent())
				return stack;
			return beltCapability.orElse(null).insertItem(slot, stack, simulate);
		}
		
		if (!te.canTakeItems())
			return stack;
		if (!simulate) 
			te.setStackToDistribute(stack);
		return ItemCooldownManager.tick;
	}

	@Override
	public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
		LazyOptional<IItemHandler> beltCapability = te.getBeltCapability();
		if (!beltCapability.isPresent())
			return ItemCooldownManager.tick;
		return beltCapability.orElse(null).extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return te.stackToDistribute.a() ? 64 : te.stackToDistribute.c();
	}

	@Override
	public boolean isItemValid(int slot, ItemCooldownManager stack) {
		return true;
	}

}
