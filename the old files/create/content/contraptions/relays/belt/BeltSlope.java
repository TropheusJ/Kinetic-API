package com.simibubi.kinetic_api.content.contraptions.relays.belt;

import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.client.util.SmoothUtil;

public enum BeltSlope implements SmoothUtil {
	HORIZONTAL, UPWARD, DOWNWARD, VERTICAL, SIDEWAYS;

	@Override
	public String a() {
		return Lang.asId(name());
	}
}