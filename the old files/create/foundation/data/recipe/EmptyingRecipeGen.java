package com.simibubi.kinetic_api.foundation.data.recipe;

import com.simibubi.kinetic_api.AllFluids;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllRecipeTypes;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.AliasedBlockItem;

public class EmptyingRecipeGen extends ProcessingRecipeGen {

	/*
	 * potion/water bottles are handled internally
	 */
	
	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b
		.require(AliasedBlockItem.rt)
		.output(AllFluids.HONEY.get(), 250)
		.output(AliasedBlockItem.nw)),
	
	BUILDERS_TEA = create("builders_tea", b -> b
		.require(AllItems.BUILDERS_TEA.get())
		.output(AllFluids.TEA.get(), 250)
		.output(AliasedBlockItem.nw)),
	
	MILK_BUCKET = create("milk_bucket", b -> b
		.require(AliasedBlockItem.lT)
		.output(AllFluids.MILK.get(), 1000)
		.output(AliasedBlockItem.lK))

	;

	public EmptyingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.EMPTYING;
	}

}
