package com.simibubi.create.content.schematics.packet;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SchematicSyncPacket extends SimplePacketBase {

	public int slot;
	public boolean deployed;
	public BlockPos anchor;
	public RespawnAnchorBlock rotation;
	public LoomBlock mirror;

	public SchematicSyncPacket(int slot, RuleTest settings,
			BlockPos anchor, boolean deployed) {
		this.slot = slot;
		this.deployed = deployed;
		this.anchor = anchor;
		this.rotation = settings.d();
		this.mirror = settings.c();
	}

	public SchematicSyncPacket(PacketByteBuf buffer) {
		slot = buffer.readVarInt();
		deployed = buffer.readBoolean();
		anchor = buffer.readBlockPos();
		rotation = buffer.readEnumConstant(RespawnAnchorBlock.class);
		mirror = buffer.readEnumConstant(LoomBlock.class);
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeVarInt(slot);
		buffer.writeBoolean(deployed);
		buffer.writeBlockPos(anchor);
		buffer.writeEnumConstant(rotation);
		buffer.writeEnumConstant(mirror);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (player == null)
				return;
			ItemCooldownManager stack = ItemCooldownManager.tick;
			if (slot == -1) {
				stack = player.dC();
			} else {
				stack = player.bm.a(slot);
			}
			if (!AllItems.SCHEMATIC.isIn(stack)) {
				return;
			}
			CompoundTag tag = stack.p();
			tag.putBoolean("Deployed", deployed);
			tag.put("Anchor", NbtHelper.fromBlockPos(anchor));
			tag.putString("Rotation", rotation.name());
			tag.putString("Mirror", mirror.name());
		});
		context.get().setPacketHandled(true);
	}

}
