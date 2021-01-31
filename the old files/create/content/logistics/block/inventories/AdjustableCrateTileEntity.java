package com.simibubi.kinetic_api.content.logistics.block.inventories;

import bfs;
import com.simibubi.kinetic_api.AllBlocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class AdjustableCrateTileEntity extends CrateTileEntity implements ActionResult {

	public class Inv extends ItemStackHandler {
		public Inv() {
			super(32);
		}

		@Override
		public int getSlotLimit(int slot) {
			if (slot < allowedAmount / 64)
				return super.getSlotLimit(slot);
			else if (slot == allowedAmount / 64)
				return allowedAmount % 64;
			return 0;
		}

		@Override
		public boolean isItemValid(int slot, ItemCooldownManager stack) {
			if (slot > allowedAmount / 64)
				return false;
			return super.isItemValid(slot, stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			X_();

			itemCount = 0;
			for (int i = 0; i < getSlots(); i++) {
				itemCount += getStackInSlot(i).E();
			}
		}
	}

	public Inv inventory;
	public int allowedAmount;
	public int itemCount;
	protected LazyOptional<IItemHandler> invHandler;

	public AdjustableCrateTileEntity(BellBlockEntity<?> type) {
		super(type);
		allowedAmount = 512;
		itemCount = 10;
		inventory = new Inv();
		invHandler = LazyOptional.of(() -> inventory);
	}

	@Override
	public FoodComponent createMenu(int id, bfs inventory, PlayerAbilities player) {
		return new AdjustableCrateContainer(id, inventory, this);
	}

	public AdjustableCrateTileEntity getOtherCrate() {
		if (!AllBlocks.ADJUSTABLE_CRATE.has(p()))
			return null;
		BeehiveBlockEntity tileEntity = d.c(e.offset(getFacing()));
		if (tileEntity instanceof AdjustableCrateTileEntity)
			return (AdjustableCrateTileEntity) tileEntity;
		return null;
	}

	public AdjustableCrateTileEntity getMainCrate() {
		if (isSecondaryCrate())
			return getOtherCrate();
		return this;
	}

	public void onSplit() {
		AdjustableCrateTileEntity other = getOtherCrate();
		if (other == null)
			return;
		if (other == getMainCrate()) {
			other.onSplit();
			return;
		}

		other.allowedAmount = Math.max(1, allowedAmount - 1024);
		for (int slot = 0; slot < other.inventory.getSlots(); slot++)
			other.inventory.setStackInSlot(slot, ItemCooldownManager.tick);
		for (int slot = 16; slot < inventory.getSlots(); slot++) {
			other.inventory.setStackInSlot(slot - 16, inventory.getStackInSlot(slot));
			inventory.setStackInSlot(slot, ItemCooldownManager.tick);
		}
		allowedAmount = Math.min(1024, allowedAmount);

		invHandler.invalidate();
		invHandler = LazyOptional.of(() -> inventory);
		other.invHandler.invalidate();
		other.invHandler = LazyOptional.of(() -> other.inventory);
	}

	public void onDestroyed() {
		AdjustableCrateTileEntity other = getOtherCrate();
		if (other == null) {
			for (int slot = 0; slot < inventory.getSlots(); slot++)
				drop(slot);
			return;
		}

		AdjustableCrateTileEntity main = getMainCrate();
		if (this == main) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				other.inventory.setStackInSlot(slot, inventory.getStackInSlot(slot));
				inventory.setStackInSlot(slot, ItemCooldownManager.tick);
			}
			other.allowedAmount = Math.min(1024, allowedAmount);
		}

		for (int slot = 16; slot < other.inventory.getSlots(); slot++)
			other.drop(slot);

		other.invHandler.invalidate();
		other.invHandler = LazyOptional.of(() -> other.inventory);
	}

	private void drop(int slot) {
		Inventory.a(d, e.getX(), e.getY(), e.getZ(), inventory.getStackInSlot(slot));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Main", true);
		compound.putInt("AllowedAmount", allowedAmount);
		compound.put("Inventory", inventory.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		allowedAmount = compound.getInt("AllowedAmount");
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public Text d() {
		return new LiteralText(u().getRegistryName()
			.toString());
	}

	public void sendToContainer(PacketByteBuf buffer) {
		buffer.writeBlockPos(o());
		buffer.writeCompoundTag(b());
	}

	@Override
	public void al_() {
		super.al_();
		invHandler.invalidate();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			AdjustableCrateTileEntity mainCrate = getMainCrate();
			if (mainCrate != null && mainCrate.invHandler != null && mainCrate.invHandler.isPresent())
				return mainCrate.invHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

}
