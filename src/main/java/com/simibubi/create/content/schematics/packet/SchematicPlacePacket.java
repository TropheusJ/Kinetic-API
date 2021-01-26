package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.rule.RuleTest;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SchematicPlacePacket extends SimplePacketBase {

	public ItemCooldownManager stack;

	public SchematicPlacePacket(ItemCooldownManager stack) {
		this.stack = stack;
	}

	public SchematicPlacePacket(PacketByteBuf buffer) {
		stack = buffer.n();
	}

	public void write(PacketByteBuf buffer) {
		buffer.a(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;
			StructureProcessor t = SchematicItem.loadSchematic(stack);
			RuleTest settings = SchematicItem.getSettings(stack);
			settings.a(false);
			t.a(player.getServerWorld(), NbtHelper.toBlockPos(stack.o().getCompound("Anchor")),
					settings, player.cX());
		});
		context.get().setPacketHandled(true);
	}

}
