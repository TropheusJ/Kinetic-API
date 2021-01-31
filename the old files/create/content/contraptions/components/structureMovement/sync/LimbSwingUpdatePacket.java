package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.sync;

import java.util.function.Supplier;
import apx;
import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class LimbSwingUpdatePacket extends SimplePacketBase {

	private int entityId;
	private EntityHitResult position;
	private float limbSwing;

	public LimbSwingUpdatePacket(int entityId, EntityHitResult position, float limbSwing) {
		this.entityId = entityId;
		this.position = position;
		this.limbSwing = limbSwing;
	}

	public LimbSwingUpdatePacket(PacketByteBuf buffer) {
		entityId = buffer.readInt();
		position = new EntityHitResult(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		limbSwing = buffer.readFloat();
	}

	@Override
	public void write(PacketByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeFloat((float) position.entity);
		buffer.writeFloat((float) position.c);
		buffer.writeFloat((float) position.d);
		buffer.writeFloat(limbSwing);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> {
				DragonHeadEntityModel world = KeyBinding.B().r;
				if (world == null)
					return;
				apx entity = world.a(entityId);
				if (entity == null)
					return;
				CompoundTag data = entity.getPersistentData();
				data.putInt("LastOverrideLimbSwingUpdate", 0);
				data.putFloat("OverrideLimbSwing", limbSwing);
				entity.a(position.entity, position.c, position.d, entity.p,
					entity.q, 2, false);
			});
		context.get()
			.setPacketHandled(true);
	}

}
