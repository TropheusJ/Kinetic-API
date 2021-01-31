package com.simibubi.kinetic_api.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.compat.jei.ConversionRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingOutput;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.render.BufferVertexConsumer;

public class MysteriousItemConversionCategory extends CreateRecipeCategory<ConversionRecipe> {

	public static List<ConversionRecipe> getRecipes() {
		List<ConversionRecipe> recipes = new ArrayList<>();
		recipes.add(ConversionRecipe.create(AllItems.EMPTY_BLAZE_BURNER.asStack(), AllBlocks.BLAZE_BURNER.asStack()));
		recipes.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.SHADOW_STEEL.asStack()));
		recipes.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.REFINED_RADIANCE.asStack()));
		return recipes;
	}
	
	public MysteriousItemConversionCategory() {
		super(itemIcon(AllItems.CHROMATIC_COMPOUND.get()), emptyBackground(177, 50));
	}

	@Override
	public Class<? extends ConversionRecipe> getRecipeClass() {
		return ConversionRecipe.class;
	}

	@Override
	public void setIngredients(ConversionRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.a());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getRollableResultsAsItemStacks());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ConversionRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		List<ProcessingOutput> results = recipe.getRollableResults();
		itemStacks.init(0, true, 26, 16);
		itemStacks.set(0, Arrays.asList(recipe.a().get(0).a()));
		itemStacks.init(1, false, 131, 16);
		itemStacks.set(1, results.get(0).getStack());
	}

	@Override
	public void draw(ConversionRecipe recipe, BufferVertexConsumer matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 26, 16);
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 131, 16);
		AllGuiTextures.JEI_LONG_ARROW.draw(matrixStack, 52, 20);
		AllGuiTextures.JEI_QUESTION_MARK.draw(matrixStack, 77, 5);
	}

}
