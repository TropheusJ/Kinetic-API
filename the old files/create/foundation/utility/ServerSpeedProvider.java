package com.simibubi.kinetic_api.foundation.utility;

import java.util.function.Supplier;

import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.networking.SimplePacketBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

public class ServerSpeedProvider {

	static int clientTimer = 0;
	static int serverTimer = 0;
	static boolean initialized = false;
	static InterpolatedChasingValue modifier = new InterpolatedChasingValue().withSpeed(.25f);

	public static void serverTick() {
		serverTimer++;
		if (serverTimer > getSyncInterval()) {
			AllPackets.channel.send(PacketDistributor.ALL.noArg(), new Packet());
			serverTimer = 0;
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static void clientTick() {
		if (KeyBinding.B().F() && KeyBinding.B().S())
			return;
		modifier.tick();
		clientTimer++;
	}

	public static Integer getSyncInterval() {
		return AllConfigs.SERVER.tickrateSyncTimer.get();
	}

	public static float get() {
		return modifier.value;
	}

	public static class Packet extends SimplePacketBase {

		public Packet() {
		}

		public Packet(PacketByteBuf buffer) {
		}

		@Override
		public void write(PacketByteBuf buffer) {
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get().enqueueWork(() -> {
				if (!initialized) {
					initialized = true;
					clientTimer = 0;
					return;
				}
				float target = ((float) getSyncInterval()) / Math.max(clientTimer, 1);
				modifier.target(Math.min(target, 1));
				clientTimer = 0;

			});
			context.get().setPacketHandled(true);
		}

	}

}
