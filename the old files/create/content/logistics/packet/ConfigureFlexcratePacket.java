package com.simibubi.kinetic_api.content.logistics.packet;

import com.simibubi.kinetic_api.content.logistics.block.inventories.AdjustableCrateTileEntity;
import com.simibubi.kinetic_api.foundation.networking.TileEntityConfigurationPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class ConfigureFlexcratePacket extends TileEntityConfigurationPacket<AdjustableCrateTileEntity> {

	private int maxItems;
	
	public ConfigureFlexcratePacket(PacketByteBuf buffer) {
		super(buffer);
	}
	
	public ConfigureFlexcratePacket(BlockPos pos, int newMaxItems) {
		super(pos);
		this.maxItems = newMaxItems;
	}

	@Override
	protected void writeSettings(PacketByteBuf buffer) {
		buffer.writeInt(maxItems);
	}

	@Override
	protected void readSettings(PacketByteBuf buffer) {
		maxItems = buffer.readInt();
	}

	@Override
	protected void applySettings(AdjustableCrateTileEntity te) {
		te.allowedAmount = maxItems;
	}

}
