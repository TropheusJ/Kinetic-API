package com.simibubi.kinetic_api.content.contraptions.components.actors.dispenser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class DropperMovementBehaviour extends MovementBehaviour {
	protected static final MovedDefaultDispenseItemBehaviour defaultBehaviour = new MovedDefaultDispenseItemBehaviour();
	private static final Random RNG = new Random();

	protected void activate(MovementContext context, BlockPos pos) {
		DispenseItemLocation location = getDispenseLocation(context);
		if (location.isEmpty()) {
			context.world.syncWorldEvent(1001, pos, 0);
		} else {
			setItemStackAt(location, defaultBehaviour.dispense(getItemStackAt(location, context), context, pos), context);
		}
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.v)
			return;
		collectItems(context);
		activate(context, pos);
	}

	private void collectItems(MovementContext context) {
		getStacks(context).stream().filter(itemStack -> !itemStack.a() && itemStack.b() != AliasedBlockItem.a && itemStack.c() > itemStack.E()).forEach(itemStack -> itemStack.f(
			ItemHelper.extract(context.contraption.inventory, itemStack::a, ItemHelper.ExtractionCountMode.UPTO, itemStack.c() - itemStack.E(), false).E()));
	}

	private void updateTemporaryData(MovementContext context) {
		if (!(context.temporaryData instanceof DefaultedList) && context.world != null) {
			DefaultedList<ItemCooldownManager> stacks = DefaultedList.ofSize(getInvSize(), ItemCooldownManager.tick);
			Clearable.b(context.tileData, stacks);
			context.temporaryData = stacks;
		}
	}

	@SuppressWarnings("unchecked")
	private DefaultedList<ItemCooldownManager> getStacks(MovementContext context) {
		updateTemporaryData(context);
		return (DefaultedList<ItemCooldownManager>) context.temporaryData;
	}

	private ArrayList<DispenseItemLocation> getUseableLocations(MovementContext context) {
		ArrayList<DispenseItemLocation> useable = new ArrayList<>();
		for (int slot = 0; slot < getInvSize(); slot++) {
			DispenseItemLocation location = new DispenseItemLocation(true, slot);
			ItemCooldownManager testStack = getItemStackAt(location, context);
			if (testStack == null || testStack.a())
				continue;
			if (testStack.c() == 1) {
				location = new DispenseItemLocation(false, ItemHelper.findFirstMatchingSlotIndex(context.contraption.inventory, testStack::a));
				if (!getItemStackAt(location, context).a())
					useable.add(location);
			} else if (testStack.E() >= 2)
				useable.add(location);
		}
		return useable;
	}

	@Override
	public void writeExtraData(MovementContext context) {
		DefaultedList<ItemCooldownManager> stacks = getStacks(context);
		if (stacks == null)
			return;
		Clearable.a(context.tileData, stacks);
	}

	@Override
	public void stopMoving(MovementContext context) {
		super.stopMoving(context);
		writeExtraData(context);
	}

	protected DispenseItemLocation getDispenseLocation(MovementContext context) {
		int i = -1;
		int j = 1;
		List<DispenseItemLocation> useableLocations = getUseableLocations(context);
		for (int k = 0; k < useableLocations.size(); ++k) {
			if (RNG.nextInt(j++) == 0) {
				i = k;
			}
		}
		if (i < 0)
			return DispenseItemLocation.NONE;
		else
			return useableLocations.get(i);
	}

	protected ItemCooldownManager getItemStackAt(DispenseItemLocation location, MovementContext context) {
		if (location.isInternal()) {
			return getStacks(context).get(location.getSlot());
		} else {
			return context.contraption.inventory.getStackInSlot(location.getSlot());
		}
	}

	protected void setItemStackAt(DispenseItemLocation location, ItemCooldownManager stack, MovementContext context) {
		if (location.isInternal()) {
			getStacks(context).set(location.getSlot(), stack);
		} else {
			context.contraption.inventory.setStackInSlot(location.getSlot(), stack);
		}
	}

	private static int getInvSize() {
		return 9;
	}
}
