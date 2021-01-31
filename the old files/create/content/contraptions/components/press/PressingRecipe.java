package com.simibubi.kinetic_api.content.contraptions.components.press;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.wrapper.RecipeWrapper;

@ParametersAreNonnullByDefault
public class PressingRecipe extends ProcessingRecipe<RecipeWrapper> {

	public PressingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.PRESSING, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, GameMode worldIn) {
		if (inv.c())
			return false;
		return ingredients.get(0)
			.a(inv.a(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}
	
	@Override
	protected int getMaxOutputCount() {
		return 2;
	}
}
