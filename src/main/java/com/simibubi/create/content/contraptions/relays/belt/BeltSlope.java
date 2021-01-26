package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.client.util.SmoothUtil;

public enum BeltSlope implements SmoothUtil {
	HORIZONTAL, UPWARD, DOWNWARD, VERTICAL, SIDEWAYS;

	@Override
	public String a() {
		return Lang.asId(name());
	}
}