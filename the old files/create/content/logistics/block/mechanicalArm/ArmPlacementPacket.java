package com.simibubi.kinetic_api.content.logistics.block.mechanicalArm;

import java.util.Collection;
import java.util.function.Supplier;

import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ArmPlacementPacket extends SimplePacketBase {

	private Collection<ArmInteractionPoint> points;
	private ListTag receivedTag;
	private BlockPos pos;

	public ArmPlacementPacket(Collection<ArmInteractionPoint> points, BlockPos pos) {
		this.points = points;
		this.pos = pos;
	}

	public ArmPlacementPacket(PacketByteBuf buffer) {
		CompoundTag nbt = buffer.readCompoundTag();
		receivedTag = nbt.getList("Points", NBT.TAG_COMPOUND);
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		CompoundTag nbt = new CompoundTag();
		ListTag pointsNBT = new ListTag();
		points.stream()
			.map(aip -> aip.serialize(pos))
			.forEach(pointsNBT::add);
		nbt.put("Points", pointsNBT);
		buffer.writeCompoundTag(nbt);
		buffer.writeBlockPos(pos);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				ServerPlayerEntity player = context.get()
					.getSender();
				if (player == null)
					return;
				GameMode world = player.l;
				if (world == null || !world.p(pos))
					return;
				BeehiveBlockEntity tileEntity = world.c(pos);
				if (!(tileEntity instanceof ArmTileEntity))
					return;

				ArmTileEntity arm = (ArmTileEntity) tileEntity;
				arm.interactionPointTag = receivedTag;
			});
		context.get()
			.setPacketHandled(true);

	}

}
