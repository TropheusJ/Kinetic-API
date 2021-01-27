package com.simibubi.create.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;
import apx;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ContraptionInteractionPacket extends SimplePacketBase {

	private ItemScatterer interactionHand;
	private int target;
	private BlockPos localPos;
	private Direction face;

	public ContraptionInteractionPacket(AbstractContraptionEntity target, ItemScatterer hand, BlockPos localPos, Direction side) {
		this.interactionHand = hand;
		this.localPos = localPos;
		this.target = target.X();
		this.face = side;
	}

	public ContraptionInteractionPacket(PacketByteBuf buffer) {
		target = buffer.readInt();
		int handId = buffer.readInt();
		interactionHand = handId == -1 ? null : ItemScatterer.values()[handId];
		localPos = buffer.readBlockPos();
		face = Direction.byId(buffer.readShort());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeInt(target);
		buffer.writeInt(interactionHand == null ? -1 : interactionHand.ordinal());
		buffer.writeBlockPos(localPos);
		buffer.writeShort(face.getId());
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender == null)
					return;
				apx entityByID = sender.getServerWorld()
					.a(target);
				if (!(entityByID instanceof AbstractContraptionEntity))
					return;
				AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
				if (contraptionEntity.handlePlayerInteraction(sender, localPos, face, interactionHand))
					sender.a(interactionHand, true);
			});
		context.get()
			.setPacketHandled(true);
	}

}
