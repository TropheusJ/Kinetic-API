package com.simibubi.kinetic_api.compat.jei.category;

import java.util.Arrays;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.compat.jei.category.PackingCategory.PackingType;
import com.simibubi.kinetic_api.compat.jei.category.animations.AnimatedPress;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinRecipe;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.BellBlock;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameRules;

public class PackingCategory extends BasinCategory {

	private AnimatedPress press = new AnimatedPress(true);
	private PackingType type;

	enum PackingType {
		AUTO_SQUARE, COMPACTING;
	}

	public static PackingCategory standard() {
		return new PackingCategory(PackingType.COMPACTING, AllBlocks.BASIN.get(), 103);
	}

	public static PackingCategory autoSquare() {
		return new PackingCategory(PackingType.AUTO_SQUARE, BellBlock.bV, 85);
	}

	protected PackingCategory(PackingType type, GameRules icon, int height) {
		super(type != PackingType.AUTO_SQUARE, doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), icon),
			emptyBackground(177, height));
		this.type = type;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BasinRecipe recipe, IIngredients ingredients) {
		if (type == PackingType.COMPACTING) {
			super.setRecipe(recipeLayout, recipe, ingredients);
			return;
		}

		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		int i = 0;

		DefaultedList<FireworkRocketRecipe> ingredients2 = recipe.a();
		int size = ingredients2.size();
		int rows = size == 4 ? 2 : 3;
		while (i < size) {
			FireworkRocketRecipe ingredient = ingredients2.get(i);
			itemStacks.init(i, true, (rows == 2 ? 26 : 17) + (i % rows) * 19, 50 - (i / rows) * 19);
			itemStacks.set(i, Arrays.asList(ingredient.a()));
			i++;
		}

		itemStacks.init(i, false, 141, 50);
		itemStacks.set(i, recipe.c());
	}

	@Override
	public void draw(BasinRecipe recipe, BufferVertexConsumer matrixStack, double mouseX, double mouseY) {
		if (type == PackingType.COMPACTING) {
			super.draw(recipe, matrixStack, mouseX, mouseY);

		} else {
			DefaultedList<FireworkRocketRecipe> ingredients2 = recipe.a();
			int size = ingredients2.size();
			int rows = size == 4 ? 2 : 3;
			for (int i = 0; i < size; i++)
				AllGuiTextures.JEI_SLOT.draw(matrixStack, (rows == 2 ? 26 : 17) + (i % rows) * 19,
					50 - (i / rows) * 19);
			AllGuiTextures.JEI_SLOT.draw(matrixStack, 141, 50);
			AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 136, 32);
			AllGuiTextures.JEI_SHADOW.draw(matrixStack, 81, 68);
		}

		press.draw(matrixStack, getBackground().getWidth() / 2 + 6, 40);
	}

}