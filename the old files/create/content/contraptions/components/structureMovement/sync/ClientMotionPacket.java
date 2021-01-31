package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;

import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

public class ClientMotionPacket extends SimplePacketBase {

	private EntityHitResult motion;
	private boolean onGround;
	private float limbSwing;

	public ClientMotionPacket(EntityHitResult motion, boolean onGround, float limbSwing) {
		this.motion = motion;
		this.onGround = onGround;
		this.limbSwing = limbSwing;
	}

	public ClientMotionPacket(PacketByteBuf buffer) {
		motion = new EntityHitResult(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		onGround = buffer.readBoolean();
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeFloat((float) motion.entity);
		buffer.writeFloat((float) motion.c);
		buffer.writeFloat((float) motion.d);
		buffer.writeBoolean(onGround);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity sender = context.get()
					.getSender();
				if (sender == null)
					return;
				sender.f(motion);
				sender.c(onGround);
				if (onGround) {
					sender.b(sender.C, 1);
					sender.C = 0;
					sender.networkHandler.floatingTicks = 0;
				}
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> sender),
					new LimbSwingUpdatePacket(sender.X(), sender.cz(), limbSwing));
			});
		context.get()
			.setPacketHandled(true);
	}

}
