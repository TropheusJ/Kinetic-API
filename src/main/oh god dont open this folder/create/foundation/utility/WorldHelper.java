package com.simibubi.create.foundation.utility;

import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class WorldHelper {
	public static Identifier getDimensionID(GrassColors world) {
		return world.r().get(Registry.DIMENSION_TYPE_KEY).getId(world.k());
	}
}
