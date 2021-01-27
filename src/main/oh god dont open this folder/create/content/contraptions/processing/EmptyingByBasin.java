package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.NameTagItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class EmptyingByBasin {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeEmptied(GameMode world, ItemCooldownManager stack) {
		if (stack.b() instanceof NameTagItem)
			return true;
		
		wrapper.a(0, stack);
		if (AllRecipeTypes.EMPTYING.find(wrapper, world)
			.isPresent())
			return true;

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return false;
		for (int i = 0; i < tank.getTanks(); i++) {
			if (tank.getFluidInTank(i)
				.getAmount() > 0)
				return true;
		}
		return false;
	}

	public static Pair<FluidStack, ItemCooldownManager> emptyItem(GameMode world, ItemCooldownManager stack, boolean simulate) {
		FluidStack resultingFluid = FluidStack.EMPTY;
		ItemCooldownManager resultingItem = ItemCooldownManager.tick;

		if (stack.b() instanceof NameTagItem)
			return PotionFluidHandler.emptyPotion(stack, simulate);
		
		wrapper.a(0, stack);
		Optional<Ingredient<RecipeWrapper>> recipe = AllRecipeTypes.EMPTYING.find(wrapper, world);
		if (recipe.isPresent()) {
			EmptyingRecipe emptyingRecipe = (EmptyingRecipe) recipe.get();
			List<ItemCooldownManager> results = emptyingRecipe.rollResults();
			if (!simulate)
				stack.g(1);
			resultingItem = results.isEmpty() ? ItemCooldownManager.tick : results.get(0);
			resultingFluid = emptyingRecipe.getResultingFluid();
			return Pair.of(resultingFluid, resultingItem);
		}

		ItemCooldownManager split = stack.i();
		split.e(1);
		LazyOptional<IFluidHandlerItem> capability =
			split.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return Pair.of(resultingFluid, resultingItem);
		resultingFluid = tank.drain(1000, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
		resultingItem = tank.getContainer()
			.i();
		if (!simulate)
			stack.g(1);

		return Pair.of(resultingFluid, resultingItem);
	}

}
