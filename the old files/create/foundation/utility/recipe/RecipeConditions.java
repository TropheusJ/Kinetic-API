package com.simibubi.create.foundation.utility.recipe;

import com.google.common.base.Predicate;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

/**
 * Commonly used Predicates for searching through recipe collections.
 * 
 * @author simibubi
 *
 */
public class RecipeConditions {

	public static Predicate<Recipe<?>> isOfType(RecipeType<?> type, RecipeType<?>... otherTypes) {
		return recipe -> {
			RecipeType<?> recipeType = recipe.getType();
			if (recipeType == type)
				return true;
			for (RecipeType<?> other : otherTypes)
				if (recipeType == other)
					return true;
			return false;
		};
	}

	public static Predicate<Recipe<?>> firstIngredientMatches(ItemStack stack) {
		return r -> !r.getPreviewInputs().isEmpty() && r.getPreviewInputs().get(0).test(stack);
	}

	public static Predicate<Recipe<?>> outputMatchesFilter(FilteringBehaviour filtering) {
		return r -> filtering.test(r.getOutput());

	}

}
