package com.simibubi.kinetic_api.foundation.utility;

import net.minecraft.client.options.KeyBinding;

public class AnimationTickHolder {

	public static int ticks;

	public static void tick() {
		ticks++;
	}

	public static float getRenderTick() {
		return ticks + KeyBinding.B().ai();
	}

}
