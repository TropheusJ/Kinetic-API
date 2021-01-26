package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemDrainTileEntity extends SmartTileEntity {

	public static final int FILLING_TIME = 20;

	SmartFluidTankBehaviour internalTank;
	TransportedItemStack heldItem;
	protected int processingTicks;
	Map<Direction, LazyOptional<ItemDrainItemHandler>> itemHandlers;

	public ItemDrainTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		itemHandlers = new IdentityHashMap<>();
		for (Direction d : Iterate.horizontalDirections) {
			ItemDrainItemHandler itemDrainItemHandler = new ItemDrainItemHandler(this, d);
			itemHandlers.put(d, LazyOptional.of(() -> itemDrainItemHandler));
		}
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels()
			.setInsertionHandler(this::tryInsertingFromSide));
		behaviours.add(internalTank = SmartFluidTankBehaviour.single(this, 1500)
			.allowExtraction()
			.forbidInsertion());
	}

	private ItemCooldownManager tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		ItemCooldownManager inserted = transportedStack.stack;
		ItemCooldownManager returned = ItemCooldownManager.tick;

		if (!getHeldItemStack().a())
			return inserted;
		
		if (inserted.E() > 1 && EmptyingByBasin.canItemBeEmptied(d, inserted)) {
			returned = ItemHandlerHelper.copyStackWithSize(inserted, inserted.E() - 1);
			inserted = ItemHandlerHelper.copyStackWithSize(inserted, 1);
		}
		
		if (simulate)
			return returned;

		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = side.getAxis()
			.isVertical() ? .5f : 0;
		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;
		setHeldItem(transportedStack, side);
		X_();
		sendData();

		return returned;
	}

	public ItemCooldownManager getHeldItemStack() {
		return heldItem == null ? ItemCooldownManager.tick : heldItem.stack;
	}

	@Override
	public void aj_() {
		super.aj_();
		if (heldItem == null) {
			processingTicks = 0;
			return;
		}

		if (processingTicks > 0) {
			heldItem.prevBeltPosition = .5f;
			boolean wasAtBeginning = processingTicks == FILLING_TIME;
			if (!d.v || processingTicks < FILLING_TIME)
				processingTicks--;
			if (!continueProcessing()) {
				processingTicks = 0;
				notifyUpdate();
				return;
			}
			if (wasAtBeginning != (processingTicks == FILLING_TIME))
				sendData();
			return;
		}

		heldItem.prevBeltPosition = heldItem.beltPosition;
		heldItem.prevSideOffset = heldItem.sideOffset;

		heldItem.beltPosition += itemMovementPerTick();
		if (heldItem.beltPosition > 1) {
			heldItem.beltPosition = 1;

			if (d.v)
				return;

			Direction side = heldItem.insertedFrom;

			ItemCooldownManager tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE)
				.tryExportingToBeltFunnel(heldItem.stack, side.getOpposite());
			if (tryExportingToBeltFunnel.E() != heldItem.stack.E()) {
				if (tryExportingToBeltFunnel.a())
					heldItem = null;
				else
					heldItem.stack = tryExportingToBeltFunnel;
				notifyUpdate();
				return;
			}

			BlockPos nextPosition = e.offset(side);
			DirectBeltInputBehaviour directBeltInputBehaviour =
				TileEntityBehaviour.get(d, nextPosition, DirectBeltInputBehaviour.TYPE);
			if (directBeltInputBehaviour == null) {
				if (!BlockHelper.hasBlockSolidSide(d.d_(nextPosition), d, nextPosition,
					side.getOpposite())) {
					ItemCooldownManager ejected = heldItem.stack;
					EntityHitResult outPos = VecHelper.getCenterOf(e)
						.e(EntityHitResult.b(side.getVector())
							.a(.75));
					float movementSpeed = itemMovementPerTick();
					EntityHitResult outMotion = EntityHitResult.b(side.getVector())
						.a(movementSpeed)
						.b(0, 1 / 8f, 0);
					outPos.e(outMotion.d());
					PaintingEntity entity = new PaintingEntity(d, outPos.entity, outPos.c + 6 / 16f, outPos.d, ejected);
					entity.f(outMotion);
					entity.m();
					entity.w = true;
					d.c(entity);

					heldItem = null;
					notifyUpdate();
				}
				return;
			}

			if (!directBeltInputBehaviour.canInsertFromSide(side))
				return;

			ItemCooldownManager returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);

			if (returned.a()) {
				if (d.c(nextPosition) instanceof ItemDrainTileEntity)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.CHAINED_ITEM_DRAIN, d, e, 5);
				heldItem = null;
				notifyUpdate();
				return;
			}

			if (returned.E() != heldItem.stack.E()) {
				heldItem.stack = returned;
				notifyUpdate();
				return;
			}

			return;
		}

		if (heldItem.prevBeltPosition < .5f && heldItem.beltPosition >= .5f) {
			if (!EmptyingByBasin.canItemBeEmptied(d, heldItem.stack))
				return;
			heldItem.beltPosition = .5f;
			if (d.v)
				return;
			processingTicks = FILLING_TIME;
			sendData();
		}

	}

	protected boolean continueProcessing() {
		if (d.v)
			return true;
		if (processingTicks < 5)
			return true;
		if (!EmptyingByBasin.canItemBeEmptied(d, heldItem.stack))
			return false;

		Pair<FluidStack, ItemCooldownManager> emptyItem = EmptyingByBasin.emptyItem(d, heldItem.stack, true);
		FluidStack fluidFromItem = emptyItem.getFirst();

		if (processingTicks > 5) {
			internalTank.allowInsertion();
			if (internalTank.getPrimaryHandler()
				.fill(fluidFromItem, FluidAction.SIMULATE) != fluidFromItem.getAmount()) {
				internalTank.forbidInsertion();
				processingTicks = FILLING_TIME;
				return true;
			}
			internalTank.forbidInsertion();
			return true;
		}

		emptyItem = EmptyingByBasin.emptyItem(d, heldItem.stack.i(), false);
		AllTriggers.triggerForNearbyPlayers(AllTriggers.ITEM_DRAIN, d, e, 5);

		// Process finished
		ItemCooldownManager out = emptyItem.getSecond();
		if (!out.a())
			heldItem.stack = out;
		else
			heldItem = null;
		internalTank.allowInsertion();
		internalTank.getPrimaryHandler()
			.fill(fluidFromItem, FluidAction.EXECUTE);
		internalTank.forbidInsertion();
		notifyUpdate();
		return true;
	}

	private float itemMovementPerTick() {
		return 1 / 8f;
	}

	@Override
	public void al_() {
		super.al_();
		for (LazyOptional<ItemDrainItemHandler> lazyOptional : itemHandlers.values())
			lazyOptional.invalidate();
	}

	public void setHeldItem(TransportedItemStack heldItem, Direction insertedFrom) {
		this.heldItem = heldItem;
		this.heldItem.insertedFrom = insertedFrom;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("ProcessingTicks", processingTicks);
		if (heldItem != null)
			compound.put("HeldItem", heldItem.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		heldItem = null;
		processingTicks = compound.getInt("ProcessingTicks");
		if (compound.contains("HeldItem"))
			heldItem = TransportedItemStack.read(compound.getCompound("HeldItem"));
		super.fromTag(state, compound, clientPacket);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (side != null && side.getAxis()
			.isHorizontal() && isItemHandlerCap(cap))
			return itemHandlers.get(side)
				.cast();

		if (side != Direction.UP && isFluidHandlerCap(cap))
			return internalTank.getCapability()
				.cast();

		return super.getCapability(cap, side);
	}

}
