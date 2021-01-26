package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class InstantSchematicPacket extends SimplePacketBase {

	private String name;
	private BlockPos origin;
	private BlockPos bounds;

	public InstantSchematicPacket(String name, BlockPos origin, BlockPos bounds) {
		this.name = name;
		this.origin = origin;
		this.bounds = bounds;
	}

	public InstantSchematicPacket(PacketByteBuf buffer) {
		name = buffer.readString(32767);
		origin = buffer.readBlockPos();
		bounds = buffer.readBlockPos();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeString(name);
		buffer.writeBlockPos(origin);
		buffer.writeBlockPos(bounds);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
					.getSender();
				if (player == null)
					return;
				Create.schematicReceiver.handleInstantSchematic(player, name, player.l, origin, bounds);
			});
		context.get()
			.setPacketHandled(true);
	}

}