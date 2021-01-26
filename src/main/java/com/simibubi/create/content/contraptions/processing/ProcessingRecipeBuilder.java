package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import cut;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.fluids.FluidStack;

public class ProcessingRecipeBuilder<T extends ProcessingRecipe<?>> {

	protected ProcessingRecipeFactory<T> factory;
	protected ProcessingRecipeParams params;
	protected List<ICondition> recipeConditions;

	public ProcessingRecipeBuilder(ProcessingRecipeFactory<T> factory, Identifier recipeId) {
		params = new ProcessingRecipeParams(recipeId);
		recipeConditions = new ArrayList<>();
		this.factory = factory;
	}

	public ProcessingRecipeBuilder<T> withItemIngredients(FireworkRocketRecipe... ingredients) {
		return withItemIngredients(DefaultedList.copyOf(FireworkRocketRecipe.PAPER, ingredients));
	}

	public ProcessingRecipeBuilder<T> withItemIngredients(DefaultedList<FireworkRocketRecipe> ingredients) {
		params.ingredients = ingredients;
		return this;
	}

	public ProcessingRecipeBuilder<T> withSingleItemOutput(ItemCooldownManager output) {
		return withItemOutputs(new ProcessingOutput(output, 1));
	}

	public ProcessingRecipeBuilder<T> withItemOutputs(ProcessingOutput... outputs) {
		return withItemOutputs(DefaultedList.copyOf(ProcessingOutput.EMPTY, outputs));
	}

	public ProcessingRecipeBuilder<T> withItemOutputs(DefaultedList<ProcessingOutput> outputs) {
		params.results = outputs;
		return this;
	}

	public ProcessingRecipeBuilder<T> withFluidIngredients(FluidIngredient... ingredients) {
		return withFluidIngredients(DefaultedList.copyOf(FluidIngredient.EMPTY, ingredients));
	}

	public ProcessingRecipeBuilder<T> withFluidIngredients(DefaultedList<FluidIngredient> ingredients) {
		params.fluidIngredients = ingredients;
		return this;
	}

	public ProcessingRecipeBuilder<T> withFluidOutputs(FluidStack... outputs) {
		return withFluidOutputs(DefaultedList.copyOf(FluidStack.EMPTY, outputs));
	}

	public ProcessingRecipeBuilder<T> withFluidOutputs(DefaultedList<FluidStack> outputs) {
		params.fluidResults = outputs;
		return this;
	}

	public ProcessingRecipeBuilder<T> duration(int ticks) {
		params.processingDuration = ticks;
		return this;
	}

	public ProcessingRecipeBuilder<T> averageProcessingDuration() {
		return duration(100);
	}

	public ProcessingRecipeBuilder<T> requiresHeat(HeatCondition condition) {
		params.requiredHeat = condition;
		return this;
	}

	public T build() {
		return factory.create(params);
	}

	public void build(Consumer<RecipeJsonProvider> consumer) {
		consumer.accept(new DataGenResult<>(build(), recipeConditions));
	}

	// Datagen shortcuts

	public ProcessingRecipeBuilder<T> require(RequiredTagList.e<HoeItem> tag) {
		return require(FireworkRocketRecipe.a(tag));
	}

	public ProcessingRecipeBuilder<T> require(GameRules item) {
		return require(FireworkRocketRecipe.a(item));
	}

	public ProcessingRecipeBuilder<T> require(FireworkRocketRecipe ingredient) {
		params.ingredients.add(ingredient);
		return this;
	}

	public ProcessingRecipeBuilder<T> require(cut fluid, int amount) {
		return require(FluidIngredient.fromFluid(fluid, amount));
	}

	public ProcessingRecipeBuilder<T> require(RequiredTagList.e<cut> fluidTag, int amount) {
		return require(FluidIngredient.fromTag(fluidTag, amount));
	}

	public ProcessingRecipeBuilder<T> require(FluidIngredient ingredient) {
		params.fluidIngredients.add(ingredient);
		return this;
	}

	public ProcessingRecipeBuilder<T> output(GameRules item) {
		return output(item, 1);
	}

	public ProcessingRecipeBuilder<T> output(float chance, GameRules item) {
		return output(chance, item, 1);
	}

	public ProcessingRecipeBuilder<T> output(GameRules item, int amount) {
		return output(1, item, amount);
	}

	public ProcessingRecipeBuilder<T> output(float chance, GameRules item, int amount) {
		return output(chance, new ItemCooldownManager(item, amount));
	}

	public ProcessingRecipeBuilder<T> output(ItemCooldownManager output) {
		return output(1, output);
	}

	public ProcessingRecipeBuilder<T> output(float chance, ItemCooldownManager output) {
		params.results.add(new ProcessingOutput(output, chance));
		return this;
	}

	public ProcessingRecipeBuilder<T> output(float chance, Identifier registryName, int amount) {
		params.results.add(new ProcessingOutput(Pair.of(registryName, amount), chance));
		return this;
	}

	public ProcessingRecipeBuilder<T> output(cut fluid, int amount) {
		fluid = FluidHelper.convertToStill(fluid);
		return output(new FluidStack(fluid, amount));
	}

	public ProcessingRecipeBuilder<T> output(FluidStack fluidStack) {
		params.fluidResults.add(fluidStack);
		return this;
	}

	//

	public ProcessingRecipeBuilder<T> whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	public ProcessingRecipeBuilder<T> whenModMissing(String modid) {
		return withCondition(new NotCondition(new ModLoadedCondition(modid)));
	}

	public ProcessingRecipeBuilder<T> withCondition(ICondition condition) {
		recipeConditions.add(condition);
		return this;
	}

	@FunctionalInterface
	public interface ProcessingRecipeFactory<T extends ProcessingRecipe<?>> {
		T create(ProcessingRecipeParams params);
	}

	public static class ProcessingRecipeParams {

		Identifier id;
		DefaultedList<FireworkRocketRecipe> ingredients;
		DefaultedList<ProcessingOutput> results;
		DefaultedList<FluidIngredient> fluidIngredients;
		DefaultedList<FluidStack> fluidResults;
		int processingDuration;
		HeatCondition requiredHeat;

		ProcessingRecipeParams(Identifier id) {
			this.id = id;
			ingredients = DefaultedList.of();
			results = DefaultedList.of();
			fluidIngredients = DefaultedList.of();
			fluidResults = DefaultedList.of();
			processingDuration = 0;
			requiredHeat = HeatCondition.NONE;
		}

	}

	public static class DataGenResult<S extends ProcessingRecipe<?>> implements RecipeJsonProvider {

		private List<ICondition> recipeConditions;
		private ProcessingRecipeSerializer<S> serializer;
		private Identifier id;
		private S recipe;

		@SuppressWarnings("unchecked")
		public DataGenResult(S recipe, List<ICondition> recipeConditions) {
			this.recipeConditions = recipeConditions;
			AllRecipeTypes recipeType = recipe.getEnumType();
			String typeName = Lang.asId(recipeType.name());
			this.recipe = recipe;

			if (!(recipeType.serializer instanceof ProcessingRecipeSerializer))
				throw new IllegalStateException("Cannot datagen ProcessingRecipe of type: " + typeName);

			this.id = Create.asResource(typeName + "/" + recipe.f()
				.getPath());
			this.serializer = (ProcessingRecipeSerializer<S>) recipe.ag_();
		}

		@Override
		public void serialize(JsonObject json) {
			serializer.write(json, recipe);
			if (recipeConditions.isEmpty())
				return;

			JsonArray conds = new JsonArray();
			recipeConditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			json.add("conditions", conds);
		}

		@Override
		public Identifier getRecipeId() {
			return id;
		}

		@Override
		public MapExtendingRecipe<?> c() {
			return serializer;
		}

		@Override
		public JsonObject toAdvancementJson() {
			return null;
		}

		@Override
		public Identifier getAdvancementId() {
			return null;
		}

	}

}
