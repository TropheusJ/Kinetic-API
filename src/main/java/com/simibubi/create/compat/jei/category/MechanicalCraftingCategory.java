package com.simibubi.create.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedCrafter;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ToolItem;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

public class MechanicalCraftingCategory extends CreateRecipeCategory<BlastingRecipe> {

	private final AnimatedCrafter crafter = new AnimatedCrafter();

	public MechanicalCraftingCategory() {
		super(itemIcon(AllBlocks.MECHANICAL_CRAFTER.get()), emptyBackground(177, 107));
	}

	@Override
	public void setIngredients(BlastingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.a());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.c());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlastingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		DefaultedList<FireworkRocketRecipe> recipeIngredients = recipe.a();

		itemStacks.init(0, false, 133, 80);
		itemStacks.set(0, recipe.c()
			.getStack());

		int x = getXPadding(recipe);
		int y = getYPadding(recipe);
		float scale = getScale(recipe);
		int size = recipeIngredients.size();
		IIngredientRenderer<ItemCooldownManager> renderer = new CrafterIngredientRenderer(recipe);

		for (int i = 0; i < size; i++) {
			float f = 19 * scale;
			int slotSize = (int) (16 * scale);
			int xPosition = (int) (x + 1 + (i % getWidth(recipe)) * f);
			int yPosition = (int) (y + 1 + (i / getWidth(recipe)) * f);
			itemStacks.init(i + 1, true, renderer, xPosition, yPosition, slotSize, slotSize, 0, 0);
			itemStacks.set(i + 1, Arrays.asList(recipeIngredients.get(i)
				.a()));
		}

	}

	static int maxSize = 100;

	public static float getScale(BlastingRecipe recipe) {
		int w = getWidth(recipe);
		int h = getHeight(recipe);
		return Math.min(1, maxSize / (19f * Math.max(w, h)));
	}

	public static int getYPadding(BlastingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getHeight(recipe) * 19 * .5);
	}

	public static int getXPadding(BlastingRecipe recipe) {
		return 3 + 50 - (int) (getScale(recipe) * getWidth(recipe) * 19 * .5);
	}

	private static int getWidth(BlastingRecipe recipe) {
		return recipe instanceof RecipeSerializer ? ((RecipeSerializer) recipe).i() : 1;
	}

	private static int getHeight(BlastingRecipe recipe) {
		return recipe instanceof RecipeSerializer ? ((RecipeSerializer) recipe).j() : 1;
	}

	@Override
	public void draw(BlastingRecipe recipe, BufferVertexConsumer matrixStack, double mouseX, double mouseY) {
		matrixStack.a();
		float scale = getScale(recipe);
		matrixStack.a(getXPadding(recipe), getYPadding(recipe), 0);

		for (int row = 0; row < getHeight(recipe); row++)
			for (int col = 0; col < getWidth(recipe); col++)
				if (!recipe.a()
					.get(row * getWidth(recipe) + col)
					.d()) {
					matrixStack.a();
					matrixStack.a(col * 19 * scale, row * 19 * scale, 0);
					matrixStack.a(scale, scale, scale);
					AllGuiTextures.JEI_SLOT.draw(matrixStack, 0, 0);
					matrixStack.b();
				}

		matrixStack.b();

		AllGuiTextures.JEI_SLOT.draw(matrixStack, 133, 80);
		AllGuiTextures.JEI_DOWN_ARROW.draw(matrixStack, 128, 59);
		crafter.draw(matrixStack, 129, 25);

		matrixStack.a();
		matrixStack.a(0, 0, 300);

		GlStateManager.pushTextureAttributes();
		int amount = 0;
		for (FireworkRocketRecipe ingredient : recipe.a()) {
			if (FireworkRocketRecipe.PAPER == ingredient)
				continue;
			amount++;
		}

		KeyBinding.B().category.a(matrixStack, amount + "", 142, 39, 0xFFFFFF);
		matrixStack.b();
	}

	@Override
	public Class<? extends BlastingRecipe> getRecipeClass() {
		return BlastingRecipe.class;
	}

	private static final class CrafterIngredientRenderer implements IIngredientRenderer<ItemCooldownManager> {

		private final BlastingRecipe recipe;

		public CrafterIngredientRenderer(BlastingRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void render(BufferVertexConsumer matrixStack, int xPosition, int yPosition, ItemCooldownManager ingredient) {
			matrixStack.a();
			matrixStack.a(xPosition, yPosition, 0);
			float scale = getScale(recipe);
			matrixStack.a(scale, scale, scale);

			if (ingredient != null) {
				RenderSystem.pushMatrix();
				RenderSystem.multMatrix(matrixStack.c().a());
				RenderSystem.enableDepthTest();
				GlStateManager.pushLightingAttributes();
				KeyBinding minecraft = KeyBinding.B();
				ItemColorProvider font = getFontRenderer(minecraft, ingredient);
				HorseEntityRenderer itemRenderer = minecraft.ac();
				itemRenderer.a(null, ingredient, 0, 0);
				itemRenderer.a(font, ingredient, 0, 0, null);
				RenderSystem.disableBlend();
				GlStateManager.pushTextureAttributes();
				RenderSystem.popMatrix();
			}

			matrixStack.b();
		}

		@Override
		public List<Text> getTooltip(ItemCooldownManager ingredient, ToolItem tooltipFlag) {
			KeyBinding minecraft = KeyBinding.B();
			PlayerAbilities player = minecraft.s;
			try {
				return ingredient.a(player, tooltipFlag);
			} catch (RuntimeException | LinkageError e) {
				List<Text> list = new ArrayList<>();
				TranslatableText crash = new TranslatableText("jei.tooltip.error.crash");
				list.add(crash.formatted(Formatting.RED));
				return list;
			}
		}
	}

}
