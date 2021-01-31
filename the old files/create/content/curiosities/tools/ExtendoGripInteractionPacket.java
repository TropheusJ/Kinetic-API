package com.simibubi.kinetic_api.content.curiosities.tools;

import java.util.function.Supplier;
import apx;
import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ExtendoGripInteractionPacket extends SimplePacketBase {

	private ItemScatterer interactionHand;
	private int target;
	private EntityHitResult specificPoint;

	public ExtendoGripInteractionPacket(apx target) {
		this(target, null);
	}

	public ExtendoGripInteractionPacket(apx target, ItemScatterer hand) {
		this(target, hand, null);
	}

	public ExtendoGripInteractionPacket(apx target, ItemScatterer hand, EntityHitResult specificPoint) {
		interactionHand = hand;
		this.specificPoint = specificPoint;
		this.target = target.X();
	}

	public ExtendoGripInteractionPacket(PacketByteBuf buffer) {
		target = buffer.readInt();
		int handId = buffer.readInt();
		interactionHand = handId == -1 ? null : ItemScatterer.values()[handId];
		if (buffer.readBoolean())
			specificPoint = new EntityHitResult(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeInt(target);
		buffer.writeInt(interactionHand == null ? -1 : interactionHand.ordinal());
		buffer.writeBoolean(specificPoint != null);
		if (specificPoint != null) {
			buffer.writeDouble(specificPoint.entity);
			buffer.writeDouble(specificPoint.c);
			buffer.writeDouble(specificPoint.d);
		}
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
				if (entityByID != null && ExtendoGripItem.isHoldingExtendoGrip(sender)) {
					if (interactionHand == null)
						sender.f(entityByID);
					else if (specificPoint == null)
						sender.a(entityByID, interactionHand);
					else
						entityByID.a(sender, specificPoint, interactionHand);
				}
			});
		context.get()
			.setPacketHandled(true);
	}

}
