package com.simibubi.kinetic_api.content.schematics;

public interface ISpecialEntityItemRequirement {

	default ItemRequirement getRequiredItems() {
		return ItemRequirement.INVALID;
	}
	
}
