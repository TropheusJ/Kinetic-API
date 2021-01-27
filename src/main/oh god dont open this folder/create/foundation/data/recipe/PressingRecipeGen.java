package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.recipe.FireworkRocketRecipe;

public class PressingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	SUGAR_CANE = create(() -> AliasedBlockItem.bD, b -> b.output(AliasedBlockItem.mb)),

		PATH = create("path", b -> b.require(FireworkRocketRecipe.a(AliasedBlockItem.i, AliasedBlockItem.j, AliasedBlockItem.l))
			.output(AliasedBlockItem.gi)),

		IRON = create("iron_ingot", b -> b.require(I.iron())
			.output(AllItems.IRON_SHEET.get())),
		GOLD = create("gold_ingot", b -> b.require(I.gold())
			.output(AllItems.GOLDEN_SHEET.get())),
		COPPER = create("copper_ingot", b -> b.require(I.copper())
			.output(AllItems.COPPER_SHEET.get())),
		LAPIS = create("lapis_block", b -> b.require(BellBlock.ar)
			.output(AllItems.LAPIS_SHEET.get())),
		BRASS = create("brass_ingot", b -> b.require(I.brass())
			.output(AllItems.BRASS_SHEET.get()))

	;

	public PressingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.PRESSING;
	}

}
