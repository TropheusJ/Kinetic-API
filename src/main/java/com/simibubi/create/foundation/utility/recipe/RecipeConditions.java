package com.simibubi.create.foundation.utility.recipe;

import com.google.common.base.Predicate;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

/**
 * Commonly used Predicates for searching through recipe collections.
 * 
 * @author simibubi
 *
 */
public class RecipeConditions {

	public static Predicate<Ingredient<?>> isOfType(Recipe<?> type, Recipe<?>... otherTypes) {
		return recipe -> {
			Recipe<?> recipeType = recipe.g();
			if (recipeType == type)
				return true;
			for (Recipe<?> other : otherTypes)
				if (recipeType == other)
					return true;
			return false;
		};
	}

	public static Predicate<Ingredient<?>> firstIngredientMatches(ItemCooldownManager stack) {
		return r -> !r.a().isEmpty() && r.a().get(0).a(stack);
	}

	public static Predicate<Ingredient<?>> outputMatchesFilter(FilteringBehaviour filtering) {
		return r -> filtering.test(r.c());

	}

}
