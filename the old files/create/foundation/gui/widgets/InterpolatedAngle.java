package com.simibubi.kinetic_api.foundation.gui.widgets;

import com.simibubi.kinetic_api.foundation.utility.AngleHelper;

public class InterpolatedAngle extends InterpolatedValue {
	
	public float get(float partialTicks) {
		return AngleHelper.angleLerp(partialTicks, lastValue, value);
	}

}
