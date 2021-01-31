package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import java.util.List;

import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.foundation.fluid.FluidIngredient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FillingBySpout {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeFilled(GameMode world, ItemCooldownManager stack) {
		wrapper.a(0, stack);
		if (AllRecipeTypes.FILLING.find(wrapper, world)
			.isPresent())
			return true;
		return GenericItemFilling.canItemBeFilled(world, stack);
	}

	public static int getRequiredAmountForItem(GameMode world, ItemCooldownManager stack, FluidStack availableFluid) {
		wrapper.a(0, stack);
		for (Ingredient<RecipeWrapper> recipe : world.o()
			.b(AllRecipeTypes.FILLING.getType(), wrapper, world)) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe;
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}
		return GenericItemFilling.getRequiredAmountForItem(world, stack, availableFluid);
	}

	public static ItemCooldownManager fillItem(GameMode world, int requiredAmount, ItemCooldownManager stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);

		wrapper.a(0, stack);
		for (Ingredient<RecipeWrapper> recipe : world.o()
			.b(AllRecipeTypes.FILLING.getType(), wrapper, world)) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe;
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(toFill)) {
				List<ItemCooldownManager> results = fillingRecipe.rollResults();
				availableFluid.shrink(requiredAmount);
				stack.g(1);
				return results.isEmpty() ? ItemCooldownManager.tick : results.get(0);
			}
		}
		
		return GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
	}

}
