package com.simibubi.kinetic_api.foundation.gui.widgets;

import com.simibubi.kinetic_api.foundation.utility.AngleHelper;

public class InterpolatedChasingAngle extends InterpolatedChasingValue {

	public float get(float partialTicks) {
		return AngleHelper.angleLerp(partialTicks, lastValue, value);
	}
	
	@Override
	protected float getCurrentDiff() {
		return AngleHelper.getShortestAngleDiff(value, getTarget());
	}

}
