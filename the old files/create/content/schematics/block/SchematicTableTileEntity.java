package com.simibubi.kinetic_api.content.schematics.block;

import bfs;
import com.simibubi.kinetic_api.foundation.tileEntity.SyncedTileEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicTableTileEntity extends SyncedTileEntity implements StructureBlockBlockEntity, ActionResult {

	public SchematicTableInventory inventory;
	public boolean isUploading;
	public String uploadingSchematic;
	public float uploadingProgress;
	public boolean sendUpdate;

	public class SchematicTableInventory extends ItemStackHandler {
		public SchematicTableInventory() {
			super(2);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			X_();
		}
	}

	public SchematicTableTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		inventory = new SchematicTableInventory();
		uploadingSchematic = null;
		uploadingProgress = 0;
	}

	public void sendToContainer(PacketByteBuf buffer) {
		buffer.writeBlockPos(o());
		buffer.writeCompoundTag(b());
	}

	@Override
	public void a(PistonHandler state, CompoundTag compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		readClientUpdate(state, compound);
		super.a(state, compound);
	}

	@Override
	public void readClientUpdate(PistonHandler state, CompoundTag compound) {
		if (compound.contains("Uploading")) {
			isUploading = true;
			uploadingSchematic = compound.getString("Schematic");
			uploadingProgress = compound.getFloat("Progress");
		} else {
			isUploading = false;
			uploadingSchematic = null;
			uploadingProgress = 0;
		}
	}

	@Override
	public CompoundTag a(CompoundTag compound) {
		compound.put("Inventory", inventory.serializeNBT());
		writeToClient(compound);
		return super.a(compound);
	}

	@Override
	public CompoundTag writeToClient(CompoundTag compound) {
		if (isUploading) {
			compound.putBoolean("Uploading", true);
			compound.putString("Schematic", uploadingSchematic);
			compound.putFloat("Progress", uploadingProgress);
		}
		return compound;
	}

	@Override
	public void aj_() {
		// Update Client Tile
		if (sendUpdate) {
			sendUpdate = false;
			d.a(e, p(), p(), 6);
		}
	}
	
	public void startUpload(String schematic) {
		isUploading = true;
		uploadingProgress = 0;
		uploadingSchematic = schematic;
		sendUpdate = true;
		inventory.setStackInSlot(0, ItemCooldownManager.tick);
	}
	
	public void finishUpload() {
		isUploading = false;
		uploadingProgress = 0;
		uploadingSchematic = null;
		sendUpdate = true;
	}

	@Override
	public FoodComponent createMenu(int p_createMenu_1_, bfs p_createMenu_2_, PlayerAbilities p_createMenu_3_) {
		return new SchematicTableContainer(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public Text d() {
		return new LiteralText(u().getRegistryName().toString());
	}

}
