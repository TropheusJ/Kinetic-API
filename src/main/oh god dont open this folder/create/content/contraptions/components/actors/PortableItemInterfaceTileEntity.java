package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.actors.PortableItemInterfaceTileEntity.InterfaceItemHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class PortableItemInterfaceTileEntity extends PortableStorageInterfaceTileEntity {

	protected LazyOptional<IItemHandlerModifiable> capability;

	public PortableItemInterfaceTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		capability = LazyOptional.empty();
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		LazyOptional<IItemHandlerModifiable> oldCap = capability;
		capability = LazyOptional.of(() -> new InterfaceItemHandler(contraption.inventory));
		oldCap.invalidate();
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		LazyOptional<IItemHandlerModifiable> oldCap = capability;
		capability = LazyOptional.of(() -> new InterfaceItemHandler(new ItemStackHandler(0)));
		oldCap.invalidate();
	}

	@Override
	protected void invalidateCapability() {
		capability.invalidate();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return capability.cast();
		return super.getCapability(cap, side);
	}

	class InterfaceItemHandler extends ItemHandlerWrapper {

		public InterfaceItemHandler(IItemHandlerModifiable wrapped) {
			super(wrapped);
		}

		@Override
		public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
			if (!isConnected())
				return ItemCooldownManager.tick;
			ItemCooldownManager extractItem = super.extractItem(slot, amount, simulate);
			if (!simulate && !extractItem.a())
				onContentTransferred();
			return extractItem;
		}

		@Override
		public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
			if (!isConnected())
				return stack;
			ItemCooldownManager insertItem = super.insertItem(slot, stack, simulate);
			if (!simulate && !insertItem.equals(stack, false))
				onContentTransferred();
			return insertItem;
		}

	}

}
