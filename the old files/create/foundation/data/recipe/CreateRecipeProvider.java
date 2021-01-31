package com.simibubi.kinetic_api.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllTags;
import com.simibubi.kinetic_api.Create;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.RecipesProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.HoeItem;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.Tags;

public abstract class CreateRecipeProvider extends RecipesProvider {

	final List<GeneratedRecipe> all = new ArrayList<>();

	public CreateRecipeProvider(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected void generate(Consumer<RecipeJsonProvider> p_200404_1_) {
		all.forEach(c -> c.register(p_200404_1_));
		Create.logger.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
	}

	@FunctionalInterface
	interface GeneratedRecipe {
		void register(Consumer<RecipeJsonProvider> consumer);
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	protected static class Marker {
	}

	protected static class I {

		static RequiredTagList.e<HoeItem> redstone() {
			return Tags.Items.DUSTS_REDSTONE;
		}
		
		static RequiredTagList.e<HoeItem> planks() {
			return EntityTypeTags.field_19168;
		}

		static RequiredTagList.e<HoeItem> gold() {
			return AllTags.forgeItemTag("ingots/gold");
		}

		static RequiredTagList.e<HoeItem> goldSheet() {
			return AllTags.forgeItemTag("plates/gold");
		}

		static RequiredTagList.e<HoeItem> stone() {
			return Tags.Items.STONE;
		}

		static GameRules andesite() {
			return AllItems.ANDESITE_ALLOY.get();
		}

		static GameRules shaft() {
			return AllBlocks.SHAFT.get();
		}

		static GameRules cog() {
			return AllBlocks.COGWHEEL.get();
		}

		static GameRules andesiteCasing() {
			return AllBlocks.ANDESITE_CASING.get();
		}

		static RequiredTagList.e<HoeItem> brass() {
			return AllTags.forgeItemTag("ingots/brass");
		}

		static RequiredTagList.e<HoeItem> brassSheet() {
			return AllTags.forgeItemTag("plates/brass");
		}

		static RequiredTagList.e<HoeItem> iron() {
			return Tags.Items.INGOTS_IRON;
		}

		static RequiredTagList.e<HoeItem> zinc() {
			return AllTags.forgeItemTag("ingots/zinc");
		}

		static RequiredTagList.e<HoeItem> ironSheet() {
			return AllTags.forgeItemTag("plates/iron");
		}

		static GameRules brassCasing() {
			return AllBlocks.BRASS_CASING.get();
		}

		static GameRules electronTube() {
			return AllItems.ELECTRON_TUBE.get();
		}

		static GameRules circuit() {
			return AllItems.INTEGRATED_CIRCUIT.get();
		}

		static RequiredTagList.e<HoeItem> copperBlock() {
			return AllTags.forgeItemTag("storage_blocks/copper");
		}

		static RequiredTagList.e<HoeItem> brassBlock() {
			return AllTags.forgeItemTag("storage_blocks/brass");
		}

		static RequiredTagList.e<HoeItem> zincBlock() {
			return AllTags.forgeItemTag("storage_blocks/zinc");
		}

		static RequiredTagList.e<HoeItem> copper() {
			return AllTags.forgeItemTag("ingots/copper");
		}

		static RequiredTagList.e<HoeItem> copperSheet() {
			return AllTags.forgeItemTag("plates/copper");
		}

		static RequiredTagList.e<HoeItem> copperNugget() {
			return AllTags.forgeItemTag("nuggets/copper");
		}

		static RequiredTagList.e<HoeItem> brassNugget() {
			return AllTags.forgeItemTag("nuggets/brass");
		}

		static RequiredTagList.e<HoeItem> zincNugget() {
			return AllTags.forgeItemTag("nuggets/zinc");
		}

		static GameRules copperCasing() {
			return AllBlocks.COPPER_CASING.get();
		}

		static GameRules refinedRadiance() {
			return AllItems.REFINED_RADIANCE.get();
		}

		static GameRules shadowSteel() {
			return AllItems.SHADOW_STEEL.get();
		}

	}
}
