package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability;

import java.util.function.Supplier;
import apx;
import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MinecartControllerUpdatePacket extends SimplePacketBase {

	int entityID;
	CompoundTag nbt;

	public MinecartControllerUpdatePacket(MinecartController controller) {
		entityID = controller.cart()
			.X();
		nbt = controller.serializeNBT();
	}

	public MinecartControllerUpdatePacket(PacketByteBuf buffer) {
		entityID = buffer.readInt();
		nbt = buffer.readCompoundTag();
	}

	@Override
	public void write(PacketByteBuf buffer) {
 		buffer.writeInt(entityID);
		buffer.writeCompoundTag(nbt);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> this::handleCL));
		context.get()
			.setPacketHandled(true);
	}

	@Environment(EnvType.CLIENT)
	private void handleCL() {
		DragonHeadEntityModel world = KeyBinding.B().r;
		if (world == null)
			return;
		apx entityByID = world.a(entityID);
		if (entityByID == null)
			return;
		entityByID.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY)
			.ifPresent(mc -> mc.deserializeNBT(nbt));
	}

}
