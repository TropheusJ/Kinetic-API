package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CouplingCreationPacket extends SimplePacketBase {

	int id1, id2;

	public CouplingCreationPacket(ScheduleBuilder cart1, ScheduleBuilder cart2) {
		id1 = cart1.X();
		id2 = cart2.X();
	}

	public CouplingCreationPacket(PacketByteBuf buffer) {
		id1 = buffer.readInt();
		id2 = buffer.readInt();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeInt(id1);
		buffer.writeInt(id2);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender != null)
					CouplingHandler.tryToCoupleCarts(sender, sender.l, id1, id2);
			});
		context.get()
			.setPacketHandled(true);
	}

}