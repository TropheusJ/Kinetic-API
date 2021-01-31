package com.simibubi.kinetic_api.content.contraptions.components.fan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.world.GameMode;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.kinetic_api.content.logistics.InWorldProcessing;
import com.simibubi.kinetic_api.content.logistics.InWorldProcessing.SplashingInv;

@ParametersAreNonnullByDefault
public class SplashingRecipe extends ProcessingRecipe<InWorldProcessing.SplashingInv> {

	public SplashingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.SPLASHING, params);
	}

	@Override
	public boolean matches(SplashingInv inv, GameMode worldIn) {
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
		return 12;
	}

}
