package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;

import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.potion.Potion;

public class FillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b.require(AllTags.forgeFluidTag("honey"), 250)
		.require(AliasedBlockItem.nw)
		.output(AliasedBlockItem.rt)),

		BUILDERS_TEA = create("builders_tea", b -> b.require(AllFluids.TEA.get(), 250)
			.require(AliasedBlockItem.nw)
			.output(AllItems.BUILDERS_TEA.get())),

		BLAZE_CAKE = create("blaze_cake", b -> b.require(FlowableFluid.field_15901, 250)
			.require(AllItems.BLAZE_CAKE_BASE.get())
			.output(AllItems.BLAZE_CAKE.get())),

		GRASS_BLOCK = create("grass_block", b -> b.require(FlowableFluid.c, 500)
			.require(AliasedBlockItem.j)
			.output(AliasedBlockItem.i)),

		GUNPOWDER = create("gunpowder", b -> b.require(PotionFluidHandler.potionIngredient(Potion.B, 25))
			.require(AllItems.CINDER_FLOUR.get())
			.output(AliasedBlockItem.kU)),

		REDSTONE = create("redstone", b -> b.require(PotionFluidHandler.potionIngredient(Potion.J, 25))
			.require(AllItems.CINDER_FLOUR.get())
			.output(AliasedBlockItem.lP)),

		GLOWSTONE = create("glowstone", b -> b.require(PotionFluidHandler.potionIngredient(Potion.f, 25))
			.require(AllItems.CINDER_FLOUR.get())
			.output(AliasedBlockItem.mk)),

		MILK_BUCKET = create("milk_bucket", b -> b.require(AllTags.forgeFluidTag("milk"), 1000)
			.require(AliasedBlockItem.lK)
			.output(AliasedBlockItem.lT))

	;

	public FillingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
