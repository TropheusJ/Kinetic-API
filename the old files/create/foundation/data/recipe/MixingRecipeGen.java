package com.simibubi.kinetic_api.foundation.data.recipe;

import com.simibubi.kinetic_api.AllFluids;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.AllTags;
import com.simibubi.kinetic_api.content.contraptions.processing.HeatCondition;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.tag.EntityTypeTags;
import net.minecraftforge.common.Tags;

public class MixingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

		TEMP_LAVA = create("lava_from_cobble", b -> b.require(Tags.Items.COBBLESTONE)
			.output(FlowableFluid.field_15901, 50)
			.requiresHeat(HeatCondition.SUPERHEATED)),

		TEA = create("tea", b -> b.require(FlowableFluid.c, 250)
			.require(AllTags.forgeFluidTag("milk"), 250)
			.require(EntityTypeTags.G)
			.output(AllFluids.TEA.get(), 500)
			.requiresHeat(HeatCondition.HEATED)),

		CHOCOLATE = create("chocolate", b -> b.require(AllTags.forgeFluidTag("milk"), 250)
			.require(AliasedBlockItem.mM)
			.require(AliasedBlockItem.ms)
			.output(AllFluids.CHOCOLATE.get(), 250)
			.requiresHeat(HeatCondition.HEATED)),

		BRASS_INGOT = create("brass_ingot", b -> b.require(I.copper())
			.require(I.zinc())
			.output(AllItems.BRASS_INGOT.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		CRUSHED_BRASS = create("crushed_brass", b -> b.require(AllItems.CRUSHED_COPPER.get())
			.require(AllItems.CRUSHED_ZINC.get())
			.output(AllItems.CRUSHED_BRASS.get(), 2)
			.requiresHeat(HeatCondition.HEATED)),

		CHROMATIC_COMPOUND = create("chromatic_compound", b -> b.require(Tags.Items.DUSTS_GLOWSTONE)
			.require(Tags.Items.DUSTS_GLOWSTONE)
			.require(Tags.Items.DUSTS_GLOWSTONE)
			.require(AllItems.POWDERED_OBSIDIAN.get())
			.require(AllItems.POWDERED_OBSIDIAN.get())
			.require(AllItems.POWDERED_OBSIDIAN.get())
			.require(AllItems.POLISHED_ROSE_QUARTZ.get())
			.output(AllItems.CHROMATIC_COMPOUND.get(), 1)
			.requiresHeat(HeatCondition.SUPERHEATED)),

		ANDESITE_ALLOY = create("andesite_alloy", b -> b.require(BellBlock.BELL_LIP_SHAPE)
			.require(AllTags.forgeItemTag("nuggets/iron"))
			.output(I.andesite(), 1)),

		ANDESITE_ALLOY_FROM_ZINC = create("andesite_alloy_from_zinc", b -> b.require(BellBlock.BELL_LIP_SHAPE)
			.require(I.zincNugget())
			.output(I.andesite(), 1))

	;

	public MixingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

}
