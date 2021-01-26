package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.client.util.SmoothUtil;

public enum BeltPart implements SmoothUtil {
	START, MIDDLE, END, PULLEY;

	@Override
	public String a() {
		return Lang.asId(name());
	}
}