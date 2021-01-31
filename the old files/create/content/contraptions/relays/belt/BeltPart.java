package com.simibubi.kinetic_api.content.contraptions.relays.belt;

import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.client.util.SmoothUtil;

public enum BeltPart implements SmoothUtil {
	START, MIDDLE, END, PULLEY;

	@Override
	public String a() {
		return Lang.asId(name());
	}
}