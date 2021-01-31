package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering;

import com.simibubi.kinetic_api.foundation.networking.TileEntityConfigurationPacket;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class FilteringCountUpdatePacket extends TileEntityConfigurationPacket<SmartTileEntity> {

	int amount;
	
	public FilteringCountUpdatePacket(PacketByteBuf buffer) {
		super(buffer);
	}
	
	public FilteringCountUpdatePacket(BlockPos pos, int amount) {
		super(pos);
		this.amount = amount;
	}

	@Override
	protected void writeSettings(PacketByteBuf buffer) {
		buffer.writeInt(amount);
	}

	@Override
	protected void readSettings(PacketByteBuf buffer) {
		amount = buffer.readInt();
	}

	@Override
	protected void applySettings(SmartTileEntity te) {
		FilteringBehaviour behaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		behaviour.forceClientState = true;
		behaviour.count = amount;
		te.X_();
		te.sendData();
	}

}
