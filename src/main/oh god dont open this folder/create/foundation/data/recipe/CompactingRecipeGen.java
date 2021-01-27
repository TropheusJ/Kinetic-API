package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.GeneratedRecipe;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraftforge.common.Tags;

public class CompactingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	GRANITE = create("granite_from_flint", b -> b.require(AliasedBlockItem.lw)
		.require(AliasedBlockItem.lw)
		.require(FlowableFluid.field_15901, 100)
		.require(AliasedBlockItem.F)
		.output(BellBlock.POWERED, 1)),

		CHOCOLATE = create("chocolate", b -> b.require(AllFluids.CHOCOLATE.get(), 250)
			.output(AllItems.BAR_OF_CHOCOLATE.get(), 1)),

		DIORITE = create("diorite_from_flint", b -> b.require(AliasedBlockItem.lw)
			.require(AliasedBlockItem.lw)
			.require(FlowableFluid.field_15901, 100)
			.require(AllPaletteBlocks.LIMESAND.get())
			.output(BellBlock.EAST_WEST_SHAPE, 1)),

		ANDESITE = create("andesite_from_flint", b -> b.require(AliasedBlockItem.lw)
			.require(AliasedBlockItem.lw)
			.require(FlowableFluid.field_15901, 100)
			.require(AliasedBlockItem.G)
			.output(BellBlock.BELL_LIP_SHAPE, 1)),

		BLAZE_CAKE = create("blaze_cake", b -> b.require(Tags.Items.EGGS)
			.require(AliasedBlockItem.mM)
			.require(AllItems.CINDER_FLOUR.get())
			.output(AllItems.BLAZE_CAKE_BASE.get(), 1))

	;

	public CompactingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
