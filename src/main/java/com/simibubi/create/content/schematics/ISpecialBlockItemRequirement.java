package com.simibubi.create.content.schematics;

import net.minecraft.block.piston.PistonHandler;

public interface ISpecialBlockItemRequirement {

	default ItemRequirement getRequiredItems(PistonHandler state) {
		return ItemRequirement.INVALID;
	}
	
}
