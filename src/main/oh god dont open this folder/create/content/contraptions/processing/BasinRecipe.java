package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BasinRecipe extends ProcessingRecipe<SmartInventory> {

	public static boolean match(BasinTileEntity basin, Ingredient<?> recipe) {
		FilteringBehaviour filter = basin.getFilter();
		if (filter == null)
			return false;

		boolean filterTest = filter.test(recipe.c());
		if (recipe instanceof BasinRecipe) {
			BasinRecipe basinRecipe = (BasinRecipe) recipe;
			if (basinRecipe.getRollableResults()
				.isEmpty()
				&& !basinRecipe.getFluidResults()
					.isEmpty())
				filterTest = filter.test(basinRecipe.getFluidResults()
					.get(0));
		}

		if (!filterTest)
			return false;

		return apply(basin, recipe, true);
	}

	public static boolean apply(BasinTileEntity basin, Ingredient<?> recipe) {
		return apply(basin, recipe, false);
	}

	private static boolean apply(BasinTileEntity basin, Ingredient<?> recipe, boolean test) {
		boolean isBasinRecipe = recipe instanceof BasinRecipe;
		IItemHandler availableItems = basin.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(null);
		IFluidHandler availableFluids = basin.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			.orElse(null);

		if (availableItems == null || availableFluids == null)
			return false;

		HeatLevel heat = BasinTileEntity.getHeatLevelOf(basin.v()
			.d_(basin.o()
				.down(1)));
		if (isBasinRecipe && !((BasinRecipe) recipe).getRequiredHeat()
			.testBlazeBurner(heat))
			return false;

		List<ItemCooldownManager> recipeOutputItems = new ArrayList<>();
		List<FluidStack> recipeOutputFluids = new ArrayList<>();

		List<FireworkRocketRecipe> ingredients = new LinkedList<>(recipe.a());
		ingredients.sort(Comparator.comparingInt(i -> i.a().length));
		List<FluidIngredient> fluidIngredients =
			isBasinRecipe ? ((BasinRecipe) recipe).getFluidIngredients() : Collections.emptyList();

		for (boolean simulate : Iterate.trueAndFalse) {

			if (!simulate && test)
				return true;

			int[] extractedItemsFromSlot = new int[availableItems.getSlots()];
			int[] extractedFluidsFromTank = new int[availableFluids.getTanks()];

			Ingredients: for (int i = 0; i < ingredients.size(); i++) {
				FireworkRocketRecipe ingredient = ingredients.get(i);

				for (int slot = 0; slot < availableItems.getSlots(); slot++) {
					if (simulate && availableItems.getStackInSlot(slot)
						.E() <= extractedItemsFromSlot[slot])
						continue;
					ItemCooldownManager extracted = availableItems.extractItem(slot, 1, true);
					if (!ingredient.a(extracted))
						continue;
					// Catalyst items are never consumed
					if (extracted.hasContainerItem() && extracted.getContainerItem()
						.a(extracted))
						continue Ingredients;
					if (!simulate)
						availableItems.extractItem(slot, 1, false);
					else if (extracted.hasContainerItem())
						recipeOutputItems.add(extracted.getContainerItem()
							.i());
					extractedItemsFromSlot[slot]++;
					continue Ingredients;
				}

				// something wasn't found
				return false;
			}

			boolean fluidsAffected = false;
			FluidIngredients: for (int i = 0; i < fluidIngredients.size(); i++) {
				FluidIngredient fluidIngredient = fluidIngredients.get(i);
				int amountRequired = fluidIngredient.getRequiredAmount();

				for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
					FluidStack fluidStack = availableFluids.getFluidInTank(tank);
					if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
						continue;
					if (!fluidIngredient.test(fluidStack))
						continue;
					int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
					if (!simulate) {
						fluidStack.shrink(drainedAmount);
						fluidsAffected = true;
					}
					amountRequired -= drainedAmount;
					if (amountRequired != 0)
						continue;
					extractedFluidsFromTank[tank] += drainedAmount;
					continue FluidIngredients;
				}

				// something wasn't found
				return false;
			}

			if (fluidsAffected) {
				basin.getBehaviour(SmartFluidTankBehaviour.INPUT)
					.foreach(TankSegment::onFluidStackChanged);
				basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
					.foreach(TankSegment::onFluidStackChanged);
			}

			if (simulate) {
				if (recipe instanceof BasinRecipe) {
					recipeOutputItems.addAll(((BasinRecipe) recipe).rollResults());
					recipeOutputFluids.addAll(((BasinRecipe) recipe).getFluidResults());
				} else
					recipeOutputItems.add(recipe.c());
			}

			if (!basin.acceptOutputs(recipeOutputItems, recipeOutputFluids, simulate))
				return false;
		}

		return true;
	}

	public static BasinRecipe convertShapeless(Ingredient<?> recipe) {
		BasinRecipe basinRecipe =
			new ProcessingRecipeBuilder<>(BasinRecipe::new, recipe.f()).withItemIngredients(recipe.a())
				.withSingleItemOutput(recipe.c())
				.build();
		return basinRecipe;
	}

	protected BasinRecipe(AllRecipeTypes type, ProcessingRecipeParams params) {
		super(type, params);
	}

	public BasinRecipe(ProcessingRecipeParams params) {
		this(AllRecipeTypes.BASIN, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 9;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 2;
	}

	@Override
	protected boolean canRequireHeat() {
		return true;
	}

	@Override
	public boolean matches(SmartInventory inv, @Nonnull GameMode worldIn) {
		return false;
	}

}
