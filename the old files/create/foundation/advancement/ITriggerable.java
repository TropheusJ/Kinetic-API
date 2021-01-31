package com.simibubi.kinetic_api.foundation.advancement;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ITriggerable {
	
	public void trigger(ServerPlayerEntity player);

}
