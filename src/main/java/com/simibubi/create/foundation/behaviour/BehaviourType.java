package com.simibubi.create.foundation.behaviour;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

public class BehaviourType<T extends TileEntityBehaviour> {
//https://i.ytimg.com/vi/CZFKWt3S2Ys/maxresdefault.jpg
	private String name;

	public BehaviourType(String name) {
		this.name = name;
	}

	public BehaviourType() {
		this("");
	}

	public String getName() {
		return name;
	}

}
