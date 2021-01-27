package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class TileEntityConfigurationPacket<TE extends SyncedTileEntity> extends SimplePacketBase {

	protected BlockPos pos;

	public TileEntityConfigurationPacket(PacketByteBuf buffer) {
		pos = buffer.readBlockPos();
		readSettings(buffer);
	}
	
	public TileEntityConfigurationPacket(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeBlockPos(pos);
		writeSettings(buffer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;
			GameMode world = player.l;

			if (world == null || !world.p(pos))
				return;
			BeehiveBlockEntity tileEntity = world.c(pos);
			if (tileEntity instanceof SyncedTileEntity) {
				applySettings((TE) tileEntity);
				((SyncedTileEntity) tileEntity).sendData();
				tileEntity.X_();
			}
		});
		context.get().setPacketHandled(true);
		
	}
	
	protected abstract void writeSettings(PacketByteBuf buffer);
	protected abstract void readSettings(PacketByteBuf buffer);
	protected abstract void applySettings(TE te);

}
