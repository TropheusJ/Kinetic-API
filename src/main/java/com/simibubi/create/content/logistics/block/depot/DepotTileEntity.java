package com.simibubi.create.content.logistics.block.depot;

import java.util.List;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class DepotTileEntity extends SmartTileEntity {

	TransportedItemStack heldItem;
	ItemStackHandler processingOutputBuffer;

	DepotItemHandler itemHandler;
	LazyOptional<DepotItemHandler> lazyItemHandler;
	private TransportedItemStackHandlerBehaviour transportedHandler;

	public DepotTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		itemHandler = new DepotItemHandler(this);
		lazyItemHandler = LazyOptional.of(() -> itemHandler);
		processingOutputBuffer = new ItemStackHandler(8) {
			protected void onContentsChanged(int slot) {
				X_();
				sendData();
			};
		};
	}

	@Override
	public void aj_() {
		super.aj_();
		if (heldItem == null)
			return;

		heldItem.prevBeltPosition = heldItem.beltPosition;
		heldItem.prevSideOffset = heldItem.sideOffset;
		float diff = .5f - heldItem.beltPosition;
		if (diff > 1 / 512f) {
			if (diff > 1 / 32f && !BeltHelper.isItemUpright(heldItem.stack))
				heldItem.angle += 1;
			heldItem.beltPosition += diff / 4f;
		}

		if (diff > 1 / 16f)
			return;
		if (d.v)
			return;
		if (handleBeltFunnelOutput())
			return;

		BeltProcessingBehaviour processingBehaviour =
			TileEntityBehaviour.get(d, e.up(2), BeltProcessingBehaviour.TYPE);
		if (processingBehaviour == null)
			return;
		if (!heldItem.locked && BeltProcessingBehaviour.isBlocked(d, e))
			return;

		ItemCooldownManager previousItem = heldItem.stack;
		boolean wasLocked = heldItem.locked;
		ProcessingResult result = wasLocked ? processingBehaviour.handleHeldItem(heldItem, transportedHandler)
			: processingBehaviour.handleReceivedItem(heldItem, transportedHandler);
		if (result == ProcessingResult.REMOVE) {
			heldItem = null;
			sendData();
			return;
		}

		heldItem.locked = result == ProcessingResult.HOLD;
		if (heldItem.locked != wasLocked || !previousItem.equals(heldItem.stack, false))
			sendData();
	}

	private boolean handleBeltFunnelOutput() {
		for (int slot = 0; slot < processingOutputBuffer.getSlots(); slot++) {
			ItemCooldownManager previousItem = processingOutputBuffer.getStackInSlot(slot);
			if (previousItem.a())
				continue;
			ItemCooldownManager afterInsert =
				getBehaviour(DirectBeltInputBehaviour.TYPE).tryExportingToBeltFunnel(previousItem, null);
			if (previousItem.E() != afterInsert.E()) {
				processingOutputBuffer.setStackInSlot(slot, afterInsert);
				notifyUpdate();
				return true;
			}
		}

		ItemCooldownManager previousItem = heldItem.stack;
		ItemCooldownManager afterInsert =
			getBehaviour(DirectBeltInputBehaviour.TYPE).tryExportingToBeltFunnel(previousItem, null);
		if (previousItem.E() != afterInsert.E()) {
			if (afterInsert.a())
				heldItem = null;
			else
				heldItem.stack = afterInsert;
			notifyUpdate();
			return true;
		}

		return false;
	}

	@Override
	public void al_() {
		super.al_();
		if (lazyItemHandler != null)
			lazyItemHandler.invalidate();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (heldItem != null)
			compound.put("HeldItem", heldItem.serializeNBT());
		compound.put("OutputBuffer", processingOutputBuffer.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		heldItem = null;
		if (compound.contains("HeldItem"))
			heldItem = TransportedItemStack.read(compound.getCompound("HeldItem"));
		processingOutputBuffer.deserializeNBT(compound.getCompound("OutputBuffer"));
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels()
			.setInsertionHandler(this::tryInsertingFromSide));
		transportedHandler = new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
			.withStackPlacement(this::getWorldPositionOf);
		behaviours.add(transportedHandler);
	}

	public ItemCooldownManager getHeldItemStack() {
		return heldItem == null ? ItemCooldownManager.tick : heldItem.stack;
	}

	public void setHeldItem(TransportedItemStack heldItem) {
		this.heldItem = heldItem;
	}

	public void setCenteredHeldItem(TransportedItemStack heldItem) {
		this.heldItem = heldItem;
		this.heldItem.beltPosition = 0.5f;
		this.heldItem.prevBeltPosition = 0.5f;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return lazyItemHandler.cast();
		return super.getCapability(cap, side);
	}

	private ItemCooldownManager tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		ItemCooldownManager inserted = transportedStack.stack;
		ItemCooldownManager empty = ItemCooldownManager.tick;

		if (!getHeldItemStack().a())
			return inserted;
		if (!isOutputEmpty())
			return inserted;
		if (simulate)
			return empty;

		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = side.getAxis()
			.isVertical() ? .5f : 0;
		transportedStack.insertedFrom = side;
		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;
		setHeldItem(transportedStack);
		X_();
		sendData();

		return empty;
	}

	private void applyToAllItems(float maxDistanceFromCentre,
		Function<TransportedItemStack, TransportedResult> processFunction) {
		if (heldItem == null)
			return;
		if (.5f - heldItem.beltPosition > maxDistanceFromCentre)
			return;

		boolean dirty = false;
		TransportedItemStack transportedItemStack = heldItem;
		ItemCooldownManager stackBefore = transportedItemStack.stack.i();
		TransportedResult result = processFunction.apply(transportedItemStack);
		if (result == null || result.didntChangeFrom(stackBefore))
			return;

		dirty = true;
		heldItem = null;
		if (result.hasHeldOutput())
			setCenteredHeldItem(result.getHeldOutput());

		for (TransportedItemStack added : result.getOutputs()) {
			if (getHeldItemStack().a()) {
				setCenteredHeldItem(added);
				continue;
			}
			ItemCooldownManager remainder = ItemHandlerHelper.insertItemStacked(processingOutputBuffer, added.stack, false);
			EntityHitResult vec = VecHelper.getCenterOf(e);
			Inventory.a(d, vec.entity, vec.c + .5f, vec.d, remainder);
		}

		if (dirty) {
			X_();
			sendData();
		}
	}

	public boolean isOutputEmpty() {
		for (int i = 0; i < processingOutputBuffer.getSlots(); i++)
			if (!processingOutputBuffer.getStackInSlot(i)
				.a())
				return false;
		return true;
	}

	private EntityHitResult getWorldPositionOf(TransportedItemStack transported) {
		EntityHitResult offsetVec = new EntityHitResult(.5f, 14 / 16f, .5f);
		return offsetVec.e(EntityHitResult.b(e));
	}

}
