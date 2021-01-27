package com.simibubi.create.foundation.tileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends BeehiveBlockEntity {

	public SyncedTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public CompoundTag getTileData() {
		return super.getTileData();
	}

	@Override
	public CompoundTag b() {
		return a(new CompoundTag());
	}

	@Override
	public void handleUpdateTag(PistonHandler state, CompoundTag tag) {
		a(state, tag);
	}

	public void sendData() {
		if (d != null)
			d.a(o(), p(), p(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		if (d != null)
			d.a(o(), p(), p(), 1);
	}
	
	@Override
	public BlockEntityUpdateS2CPacket a() {
		return new BlockEntityUpdateS2CPacket(o(), 1, writeToClient(new CompoundTag()));
	}

	@Override
	public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
		readClientUpdate(p(), pkt.getCompoundTag());
	}

	// Special handling for client update packets
	public void readClientUpdate(PistonHandler state, CompoundTag tag) {
		a(state, tag);
	}

	// Special handling for client update packets
	public CompoundTag writeToClient(CompoundTag tag) {
		return a(tag);
	}
	
	public void notifyUpdate() {
		X_();
		sendData();
	}

}
