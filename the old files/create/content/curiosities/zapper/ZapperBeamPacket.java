package com.simibubi.kinetic_api.content.curiosities.zapper;

import java.util.function.Supplier;

import com.simibubi.kinetic_api.content.curiosities.zapper.ZapperRenderHandler.LaserBeam;
import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ZapperBeamPacket extends SimplePacketBase {

	public EntityHitResult start;
	public EntityHitResult target;
	public ItemScatterer hand;
	public boolean self;

	public ZapperBeamPacket(EntityHitResult start, EntityHitResult target, ItemScatterer hand, boolean self) {
		this.start = start;
		this.target = target;
		this.hand = hand;
		this.self = self;
	}
	
	public ZapperBeamPacket(PacketByteBuf buffer) {
		start = new EntityHitResult(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		target = new EntityHitResult(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		hand = buffer.readBoolean()? ItemScatterer.RANDOM : ItemScatterer.b;
		self = buffer.readBoolean();
	}

	public void write(PacketByteBuf buffer) {
		buffer.writeDouble(start.entity);
		buffer.writeDouble(start.c);
		buffer.writeDouble(start.d);
		buffer.writeDouble(target.entity);
		buffer.writeDouble(target.c);
		buffer.writeDouble(target.d);
		
		buffer.writeBoolean(hand == ItemScatterer.RANDOM);
		buffer.writeBoolean(self);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			if (KeyBinding.B().s.cz().f(start) > 100)
				return;
			ZapperRenderHandler.addBeam(new LaserBeam(start, target).followPlayer(self, hand == ItemScatterer.RANDOM));
			
			if (self)
				ZapperRenderHandler.shoot(hand);
			else
				ZapperRenderHandler.playSound(hand, new BlockPos(start));
		}));
		context.get().setPacketHandled(true);
	}

}
