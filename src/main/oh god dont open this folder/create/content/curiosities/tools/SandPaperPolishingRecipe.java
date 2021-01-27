package com.simibubi.create.content.curiosities.tools;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe.SandPaperInv;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

@ParametersAreNonnullByDefault
public class SandPaperPolishingRecipe extends ProcessingRecipe<SandPaperInv> {

	public SandPaperPolishingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.SANDPAPER_POLISHING, params);
	}

	@Override
	public boolean matches(SandPaperInv inv, GameMode worldIn) {
		return ingredients.get(0)
			.a(inv.a(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	public static boolean canPolish(GameMode world, ItemCooldownManager stack) {
		return !getMatchingRecipes(world, stack).isEmpty();
	}

	public static ItemCooldownManager applyPolish(GameMode world, EntityHitResult position, ItemCooldownManager stack, ItemCooldownManager sandPaperStack) {
		List<Ingredient<SandPaperInv>> matchingRecipes = getMatchingRecipes(world, stack);
		if (!matchingRecipes.isEmpty())
			return matchingRecipes.get(0)
				.a(new SandPaperInv(stack))
				.i();
		return stack;
	}

	public static List<Ingredient<SandPaperInv>> getMatchingRecipes(GameMode world, ItemCooldownManager stack) {
		return world.o()
			.b(AllRecipeTypes.SANDPAPER_POLISHING.getType(), new SandPaperInv(stack), world);
	}

	public static class SandPaperInv extends RecipeWrapper {

		public SandPaperInv(ItemCooldownManager stack) {
			super(new ItemStackHandler(1));
			inv.setStackInSlot(0, stack);
		}

	}

}
