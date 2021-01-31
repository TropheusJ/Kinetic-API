package com.simibubi.kinetic_api.compat.jei;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * Helper recipe type for displaying an item relationship in JEI
 */
@ParametersAreNonnullByDefault
public class ConversionRecipe extends ProcessingRecipe<RecipeWrapper> {

	static int counter = 0;

	public static ConversionRecipe create(ItemCooldownManager from, ItemCooldownManager to) {
		Identifier recipeId = Create.asResource("conversion_" + counter++);
		return new ProcessingRecipeBuilder<>(ConversionRecipe::new, recipeId)
			.withItemIngredients(FireworkRocketRecipe.a(from))
			.withSingleItemOutput(to)
			.build();
	}

	public ConversionRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.CONVERSION, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, GameMode worldIn) {
		return false;
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

}
