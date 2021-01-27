package com.simibubi.create.content.contraptions.components.crafter;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameMode;

public class MechanicalCraftingRecipe extends RecipeSerializer {

	public MechanicalCraftingRecipe(Identifier idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
			DefaultedList<FireworkRocketRecipe> recipeItemsIn, ItemCooldownManager recipeOutputIn) {
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
	}

	private static MechanicalCraftingRecipe fromShaped(RecipeSerializer recipe) {
		return new MechanicalCraftingRecipe(recipe.f(), recipe.d(), recipe.i(), recipe.j(),
				recipe.a(), recipe.c());
	}

	@Override
	public boolean a(PropertyDelegate inv, GameMode worldIn) {
		return inv instanceof MechanicalCraftingInventory && super.a(inv, worldIn);
	}

	@Override
	public Recipe<?> g() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.type;
	}
	
	@Override
	public MapExtendingRecipe<?> ag_() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.serializer;
	}

	public static class Serializer extends RecipeSerializer.a {

		@Override
		public RecipeSerializer a(Identifier recipeId, JsonObject json) {
			return fromShaped(super.b(recipeId, json));
		}
		
		@Override
		public RecipeSerializer a(Identifier recipeId, PacketByteBuf buffer) {
			return fromShaped(super.b(recipeId, buffer));
		}

	}

}
