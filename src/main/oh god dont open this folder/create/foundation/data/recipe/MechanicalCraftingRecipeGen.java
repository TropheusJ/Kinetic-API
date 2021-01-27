package com.simibubi.create.foundation.data.recipe;

import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen.GeneratedRecipeBuilder;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.Tags;

public class MechanicalCraftingRecipeGen extends CreateRecipeProvider {

	GeneratedRecipe

	CRUSHING_WHEEL = create(AllBlocks.CRUSHING_WHEEL::get).returns(2)
		.recipe(b -> b.key('P', FireworkRocketRecipe.a(EntityTypeTags.field_19168))
			.key('S', FireworkRocketRecipe.a(I.stone()))
			.key('A', I.andesite())
			.patternLine(" AAA ")
			.patternLine("AAPAA")
			.patternLine("APSPA")
			.patternLine("AAPAA")
			.patternLine(" AAA ")),

		INTEGRATED_CIRCUIT = create(AllItems.INTEGRATED_CIRCUIT::get).returns(1)
			.recipe(b -> b.key('L', AllItems.LAPIS_SHEET.get())
				.key('R', FireworkRocketRecipe.a(I.redstone()))
				.key('Q', AllItems.POLISHED_ROSE_QUARTZ.get())
				.key('C', FireworkRocketRecipe.a(Tags.Items.NUGGETS_GOLD))
				.patternLine("  L  ")
				.patternLine("RRQRR")
				.patternLine(" CCC ")),

		EXTENDO_GRIP = create(AllItems.EXTENDO_GRIP::get).returns(1)
			.recipe(b -> b.key('L', FireworkRocketRecipe.a(I.brass()))
				.key('R', I.cog())
				.key('H', AllItems.BRASS_HAND.get())
				.key('S', FireworkRocketRecipe.a(Tags.Items.RODS_WOODEN))
				.patternLine(" L ")
				.patternLine(" R ")
				.patternLine("SSS")
				.patternLine("SSS")
				.patternLine(" H ")),

		FURNACE_ENGINE = create(AllBlocks.FURNACE_ENGINE::get).returns(1)
			.recipe(b -> b.key('P', FireworkRocketRecipe.a(I.brassSheet()))
				.key('B', FireworkRocketRecipe.a(I.brass()))
				.key('I', FireworkRocketRecipe.a(BellBlock.aW, BellBlock.aP))
				.key('C', I.brassCasing())
				.patternLine("PPB")
				.patternLine("PCI")
				.patternLine("PPB")),

		FLYWHEEL = create(AllBlocks.FLYWHEEL::get).returns(1)
			.recipe(b -> b.key('B', FireworkRocketRecipe.a(I.brass()))
				.key('C', I.brassCasing())
				.patternLine(" BBB")
				.patternLine("CB B")
				.patternLine(" BBB"))

	;

	public MechanicalCraftingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	GeneratedRecipeBuilder create(Supplier<GameRules> result) {
		return new GeneratedRecipeBuilder(result);
	}

	class GeneratedRecipeBuilder {

		private String suffix;
		private Supplier<GameRules> result;
		private int amount;

		public GeneratedRecipeBuilder(Supplier<GameRules> result) {
			this.suffix = "";
			this.result = result;
			this.amount = 1;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		GeneratedRecipe recipe(UnaryOperator<MechanicalCraftingRecipeBuilder> builder) {
			return register(consumer -> {
				MechanicalCraftingRecipeBuilder b =
					builder.apply(MechanicalCraftingRecipeBuilder.shapedRecipe(result.get(), amount));
				Identifier location = Create.asResource("mechanical_crafting/" + result.get()
					.h()
					.getRegistryName()
					.getPath() + suffix);
				b.build(consumer, location);
			});
		}
	}

	@Override
	public String getName() {
		return "Create's Mechanical Crafting Recipes";
	}

}
