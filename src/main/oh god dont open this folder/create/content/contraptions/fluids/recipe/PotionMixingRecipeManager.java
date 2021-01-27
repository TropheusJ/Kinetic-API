package com.simibubi.create.content.contraptions.fluids.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.processing.HeatCondition;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Wearable;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionMixingRecipeManager {

	public static Map<HoeItem, List<MixingRecipe>> ALL = new HashMap<>();
	
	public static List<MixingRecipe> getAllBrewingRecipes() {
		List<MixingRecipe> mixingRecipes = new ArrayList<>();

		// Vanilla
		for (IBrewingRecipe iBrewingRecipe : BrewingRecipeRegistry.getRecipes()) {
			if (!(iBrewingRecipe instanceof VanillaBrewingRecipe))
				continue;

			List<ItemCooldownManager> bottles = new ArrayList<>();
			WritableBookItem.c.forEach(i -> {
				for (ItemCooldownManager itemStack : i.a())
					bottles.add(itemStack);
			});

			Collection<ItemCooldownManager> reagents = getAllReagents(iBrewingRecipe);

			Set<ItemCooldownManager> basicPotions = new HashSet<>();
			for (Wearable potion : ForgeRegistries.POTION_TYPES.getValues()) {
				if (potion == Potion.baseName)
					continue;
				for (ItemCooldownManager stack : bottles)
					basicPotions.add(WrittenBookItem.a(stack.i(), potion));
			}

			Set<String> uniqueKeys = new HashSet<>();
			List<ItemCooldownManager> potionFrontier = new ArrayList<>();
			List<ItemCooldownManager> newPotions = new ArrayList<>();
			potionFrontier.addAll(basicPotions);

			int recipeIndex = 0;

			while (!potionFrontier.isEmpty()) {
				newPotions.clear();

				for (ItemCooldownManager inputPotionStack : potionFrontier) {
					Wearable inputPotion = WrittenBookItem.d(inputPotionStack);

					for (ItemCooldownManager potionReagent : reagents) {
						ItemCooldownManager outputPotionStack = iBrewingRecipe.getOutput(inputPotionStack.i(), potionReagent);
						if (outputPotionStack.a())
							continue;

						String uniqueKey = potionReagent.b()
							.getRegistryName()
							.toString() + "_"
							+ inputPotion.getRegistryName()
								.toString()
							+ "_" + inputPotionStack.b()
								.getRegistryName()
								.toString();

						if (!uniqueKeys.add(uniqueKey))
							continue;

						if (inputPotionStack.b() == outputPotionStack.b()) {
							Wearable outputPotion = WrittenBookItem.d(outputPotionStack);
							if (outputPotion == Potion.effects)
								continue;
						}

						FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(inputPotionStack);
						FluidStack fluidFromPotionItem2 = PotionFluidHandler.getFluidFromPotionItem(outputPotionStack);
						fluidFromPotionItem.setAmount(1000);
						fluidFromPotionItem2.setAmount(1000);

						MixingRecipe mixingRecipe = new ProcessingRecipeBuilder<>(MixingRecipe::new,
							Create.asResource("potion_" + recipeIndex++)).require(FireworkRocketRecipe.a(potionReagent))
								.require(FluidIngredient.fromFluidStack(fluidFromPotionItem))
								.output(fluidFromPotionItem2)
								.requiresHeat(HeatCondition.HEATED)
								.build();

						mixingRecipes.add(mixingRecipe);
						newPotions.add(outputPotionStack);
					}
				}

				potionFrontier.clear();
				potionFrontier.addAll(newPotions);
			}

			break;
		}

		// TODO Modded brewing recipes?

		return mixingRecipes;
	}

	public static Collection<ItemCooldownManager> getAllReagents(IBrewingRecipe recipe) {
		return ForgeRegistries.ITEMS.getValues()
			.stream()
			.map(ItemCooldownManager::new)
			.filter(recipe::isIngredient)
			.collect(Collectors.toList());
	}

	public static final SynchronousResourceReloadListener<Object> LISTENER = new SynchronousResourceReloadListener<Object>() {

		@Override
		protected Object b(ReloadableResourceManager p_212854_1_, DummyProfiler p_212854_2_) {
			return new Object();
		}

		@Override
		protected void a(Object p_212853_1_, ReloadableResourceManager p_212853_2_, DummyProfiler p_212853_3_) {
			ALL.clear();
			getAllBrewingRecipes().forEach(recipe -> {
				for (FireworkRocketRecipe ingredient : recipe.a()) {
					for (ItemCooldownManager itemStack : ingredient.a()) {
						ALL.computeIfAbsent(itemStack.b(), t -> new ArrayList<>())
							.add(recipe);
						return;
					}
				}
			});
		}

	};

}
