package com.simibubi.kinetic_api.foundation.data.recipe;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllRecipeTypes;

import net.minecraft.data.DataGenerator;

public class PolishingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	ROSE_QUARTZ = create(AllItems.ROSE_QUARTZ::get, b -> b.output(AllItems.POLISHED_ROSE_QUARTZ.get()))

	;

	public PolishingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SANDPAPER_POLISHING;
	}

}
