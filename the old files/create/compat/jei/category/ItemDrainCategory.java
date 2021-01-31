package com.simibubi.kinetic_api.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.compat.jei.category.animations.AnimatedItemDrain;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.kinetic_api.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.NameTagItem;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemDrainCategory extends CreateRecipeCategory<EmptyingRecipe> {

	AnimatedItemDrain drain;

	public ItemDrainCategory() {
		super(doubleItemIcon(AllBlocks.ITEM_DRAIN.get(), AliasedBlockItem.lL), emptyBackground(177, 50));
		drain = new AnimatedItemDrain();
	}

	public static List<EmptyingRecipe> getRecipes(IIngredientManager ingredientManager) {
		List<EmptyingRecipe> recipes = new ArrayList<>();

		ingredientManager.getAllIngredients(VanillaTypes.ITEM)
			.stream()
			.forEach(stack -> {
				if (stack.b() instanceof NameTagItem) {
					FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
					FireworkRocketRecipe potion = FireworkRocketRecipe.a(stack);
					recipes.add(new ProcessingRecipeBuilder<>(EmptyingRecipe::new, Create.asResource("potions"))
						.withItemIngredients(potion)
						.withFluidOutputs(fluidFromPotionItem)
						.withSingleItemOutput(new ItemCooldownManager(AliasedBlockItem.nw))
						.build());
					return;
				}

				LazyOptional<IFluidHandlerItem> capability =
					stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
				if (!capability.isPresent())
					return;

				ItemCooldownManager copy = stack.i();
				capability = copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
				IFluidHandlerItem handler = capability.orElse(null);
				FluidStack extracted = handler.drain(1000, FluidAction.EXECUTE);
				ItemCooldownManager result = handler.getContainer();
				if (extracted.isEmpty())
					return;

				FireworkRocketRecipe ingredient = FireworkRocketRecipe.a(stack);
				Identifier itemName = stack.b()
					.getRegistryName();
				Identifier fluidName = extracted.getFluid()
					.getRegistryName();

				recipes.add(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
					Create.asResource("empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_of_"
						+ fluidName.getNamespace() + "_" + fluidName.getPath())).withItemIngredients(ingredient)
							.withFluidOutputs(extracted)
							.withSingleItemOutput(result)
							.build());
			});

		return recipes;
	}

	@Override
	public Class<? extends EmptyingRecipe> getRecipeClass() {
		return EmptyingRecipe.class;
	}

	@Override
	public void setIngredients(EmptyingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.a());

		if (!recipe.getRollableResults()
			.isEmpty())
			ingredients.setOutput(VanillaTypes.ITEM, recipe.c());
		if (!recipe.getFluidResults()
			.isEmpty())
			ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidResults());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, EmptyingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		FluidStack fluidOutput = recipe.getResultingFluid();
		List<ItemCooldownManager> matchingIngredients = Arrays.asList(recipe.a()
			.get(0)
			.a());

		fluidStacks.init(0, true, 132, 8);
		fluidStacks.set(0, withImprovedVisibility(fluidOutput));
		itemStacks.init(0, true, 26, 7);
		itemStacks.set(0, matchingIngredients);
		itemStacks.init(1, false, 131, 26);
		itemStacks.set(1, recipe.c());

		addFluidTooltip(fluidStacks, Collections.emptyList(), ImmutableList.of(fluidOutput));
	}

	@Override
	public void draw(EmptyingRecipe recipe, BufferVertexConsumer matrixStack, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 131, 7);
		AllGuiTextures.JEI_SLOT.draw(matrixStack, 26, 7);
		getRenderedSlot(recipe, 0).draw(matrixStack, 131, 26);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, 62, 37);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 73, 4);
		drain.withFluid(recipe.getResultingFluid())
			.draw(matrixStack, getBackground().getWidth() / 2 - 13, 40);
	}

}
