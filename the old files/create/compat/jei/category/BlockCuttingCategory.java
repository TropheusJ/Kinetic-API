package com.simibubi.kinetic_api.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.kinetic_api.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public class BlockCuttingCategory extends CreateRecipeCategory<CondensedBlockCuttingRecipe> {

	private AnimatedSaw saw = new AnimatedSaw();

	public BlockCuttingCategory() {
		super(doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), AliasedBlockItem.eb), emptyBackground(177, 70));
	}

	@Override
	public Class<? extends CondensedBlockCuttingRecipe> getRecipeClass() {
		return CondensedBlockCuttingRecipe.class;
	}

	@Override
	public void setIngredients(CondensedBlockCuttingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.a());
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CondensedBlockCuttingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		itemStacks.init(0, true, 4, 4);
		itemStacks.set(0, Arrays.asList(recipe.a().get(0).a()));

		List<List<ItemCooldownManager>> results = recipe.getCondensedOutputs();
		for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
			int xOffset = (outputIndex % 5) * 19;
			int yOffset = (outputIndex / 5) * -19;

			itemStacks.init(outputIndex + 1, false, 77 + xOffset, 47 + yOffset);
			itemStacks.set(outputIndex + 1, results.get(outputIndex));
		}
	}

	@Override
	public void draw(CondensedBlockCuttingRecipe recipe, BufferVertexConsumer matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 4, 4);
		int size = Math.min(recipe.getOutputs().size(), 15);
		for (int i = 0; i < size; i++) {
			int xOffset = (i % 5) * 19;
			int yOffset = (i / 5) * -19;
			AllGuiTextures.JEI_SLOT.draw(matrixStack, 77 + xOffset, 47 + yOffset);
		}
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 31, 6);
		saw.draw(matrixStack, 33, 37);
	}

	public static class CondensedBlockCuttingRecipe extends CuttingRecipe {

		List<ItemCooldownManager> outputs = new ArrayList<>();

		public CondensedBlockCuttingRecipe(FireworkRocketRecipe ingredient) {
			super(new Identifier(""), "", ingredient, ItemCooldownManager.tick);
		}

		public void addOutput(ItemCooldownManager stack) {
			outputs.add(stack);
		}

		public List<ItemCooldownManager> getOutputs() {
			return outputs;
		}
		
		public List<List<ItemCooldownManager>> getCondensedOutputs() {
			List<List<ItemCooldownManager>> result = new ArrayList<>();
			int index = 0;
			boolean firstPass = true;
			for (ItemCooldownManager itemStack : outputs) {
				if (firstPass)
					result.add(new ArrayList<>());
				result.get(index).add(itemStack);
				index++;
				if (index >= 15) {
					index = 0;
					firstPass = false;
				}
			}
			return result;
		}

		public static List<CondensedBlockCuttingRecipe> condenseRecipes(List<Ingredient<?>> stoneCuttingRecipes) {
			List<CondensedBlockCuttingRecipe> condensed = new ArrayList<>();
			Recipes: for (Ingredient<?> recipe : stoneCuttingRecipes) {
				FireworkRocketRecipe i1 = recipe.a().get(0);
				for (CondensedBlockCuttingRecipe condensedRecipe : condensed) {
					if (ItemHelper.matchIngredients(i1, condensedRecipe.a().get(0))) {
						condensedRecipe.addOutput(recipe.c());
						continue Recipes;
					}
				}
				CondensedBlockCuttingRecipe cr = new CondensedBlockCuttingRecipe(i1);
				cr.addOutput(recipe.c());
				condensed.add(cr);
			}
			return condensed;
		}

	}

}
