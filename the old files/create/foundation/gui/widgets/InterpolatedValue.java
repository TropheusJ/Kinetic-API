package com.simibubi.kinetic_api.foundation.gui.widgets;

import afj;

public class InterpolatedValue {

	public float value = 0;
	public float lastValue = 0;
	
	public InterpolatedValue set(float value) {
		lastValue = this.value;
		this.value = value;
		return this;
	}
	
	public float get(float partialTicks) {
		return afj.g(partialTicks, lastValue, value);
	}
	
}
