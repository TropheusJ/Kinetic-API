package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.kinetic_api.content.logistics.block.inventories.BottomlessItemHandler;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.enums.Attachment;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean valid;
	private BeehiveBlockEntity te;

	public static boolean canUseAsStorage(BeehiveBlockEntity te) {
		if (te == null)
			return false;

		if (AllTileEntities.ADJUSTABLE_CRATE.is(te))
			return true;
		if (AllTileEntities.CREATIVE_CRATE.is(te))
			return true;
		if (te instanceof LecternBlockEntity)
			return true;
		if (te instanceof BlockEntityType)
			return true;
		if (te instanceof AbstractFurnaceBlockEntity)
			return true;

		LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		return capability.orElse(null) instanceof ItemStackHandler;
	}

	public MountedStorage(BeehiveBlockEntity te) {
		this.te = te;
		handler = dummyHandler;
	}

	public void removeStorageFromWorld() {
		valid = false;
		if (te == null)
			return;

		// Split double chests
		if (te.u() == BellBlockEntity.ringing || te.u() == BellBlockEntity.lastSideHit) {
			if (te.p()
				.c(CarvedPumpkinBlock.snowGolemPattern) != Attachment.SINGLE)
				te.v()
					.a(te.o(), te.p()
						.a(CarvedPumpkinBlock.snowGolemPattern, Attachment.SINGLE));
			te.s();
		}

		// Split double flexcrates
		if (AllTileEntities.ADJUSTABLE_CRATE.is(te)) {
			if (te.p()
				.c(AdjustableCrateBlock.DOUBLE))
				te.v()
					.a(te.o(), te.p()
						.a(AdjustableCrateBlock.DOUBLE, false));
			te.s();
		}

		IItemHandler teHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(dummyHandler);
		if (teHandler == dummyHandler)
			return;

		// te uses ItemStackHandler
		if (teHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) teHandler;
			valid = true;
			return;
		}

		// serialization not accessible -> fill into a serializable handler
		if (teHandler instanceof IItemHandlerModifiable) {
			IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
			handler = new ItemStackHandler(teHandler.getSlots());
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				handler.setStackInSlot(slot, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemCooldownManager.tick);
			}
			valid = true;
			return;
		}

	}

	public void addStorageToWorld(BeehiveBlockEntity te) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		IItemHandler teHandler = capability.orElse(null);
		if (!(teHandler instanceof IItemHandlerModifiable))
			return;

		IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
		for (int slot = 0; slot < Math.min(inv.getSlots(), handler.getSlots()); slot++)
			inv.setStackInSlot(slot, handler.getStackInSlot(slot));
	}

	public IItemHandlerModifiable getItemHandler() {
		return handler;
	}

	public CompoundTag serialize() {
		if (!valid)
			return null;
		CompoundTag tag = handler.serializeNBT();

		if (handler instanceof BottomlessItemHandler) {
			NBTHelper.putMarker(tag, "Bottomless");
			tag.put("ProvidedStack", handler.getStackInSlot(0)
				.serializeNBT());
		}

		return tag;
	}

	public static MountedStorage deserialize(CompoundTag nbt) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;
		storage.valid = true;

		if (nbt.contains("Bottomless")) {
			ItemCooldownManager providedStack = ItemCooldownManager.a(nbt.getCompound("ProvidedStack"));
			storage.handler = new BottomlessItemHandler(() -> providedStack);
			return storage;
		}

		storage.handler.deserializeNBT(nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

}
