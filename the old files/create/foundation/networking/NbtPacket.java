package com.simibubi.kinetic_api.foundation.networking;

import java.util.function.Supplier;

import com.simibubi.kinetic_api.content.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.kinetic_api.content.curiosities.zapper.ZapperItem;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

@Deprecated
public class NbtPacket extends SimplePacketBase {

	public ItemCooldownManager stack;
	public int slot;
	public ItemScatterer hand;

	public NbtPacket(ItemCooldownManager stack, ItemScatterer hand) {
		this(stack, -1);
		this.hand = hand;
	}
	
	public NbtPacket(ItemCooldownManager stack, int slot) {
		this.stack = stack;
		this.slot = slot;
		this.hand = ItemScatterer.RANDOM;
	}

	public NbtPacket(PacketByteBuf buffer) {
		stack = buffer.n();
		slot = buffer.readInt();
		hand = ItemScatterer.values()[buffer.readInt()];
	}
	
	public void write(PacketByteBuf buffer) {
		buffer.a(stack);
		buffer.writeInt(slot);
		buffer.writeInt(hand.ordinal());
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;
			if (!(stack.b() instanceof SymmetryWandItem || stack.b() instanceof ZapperItem)) {
				return;
			}
			stack.c("AttributeModifiers");
			if (slot == -1) {
				ItemCooldownManager heldItem = player.b(hand);
				if (heldItem.b() == stack.b()) {
					heldItem.c(stack.o());
				}
				return;
			}
			
			ItemCooldownManager heldInSlot = player.bm.a(slot);
			if (heldInSlot.b() == stack.b()) {
				heldInSlot.c(stack.o());
			}
			
		});
		context.get().setPacketHandled(true);
	}

}
