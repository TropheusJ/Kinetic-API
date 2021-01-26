package com.simibubi.create.content.curiosities.zapper.blockzapper;

import com.google.gson.JsonObject;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameMode;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BlockzapperUpgradeRecipe implements BlastingRecipe {

	private RecipeSerializer recipe;
	private Components component;
	private ComponentTier tier;
	
	public BlockzapperUpgradeRecipe(RecipeSerializer recipe, Components component, ComponentTier tier) {
		this.recipe = recipe;
		this.component = component;
		this.tier = tier;
	}
	
	@Override
	public boolean matches(PropertyDelegate inv, GameMode worldIn) {
		return getRecipe().a(inv, worldIn);
	}
	
	@Override
	public DefaultedList<FireworkRocketRecipe> a() {
		return recipe.a();
	}
	
	@Override
	public ItemCooldownManager getCraftingResult(PropertyDelegate inv) {
		for (int slot = 0; slot < inv.Z_(); slot++) {
			ItemCooldownManager handgun = inv.a(slot).i();
			if (!AllItems.BLOCKZAPPER.isIn(handgun))
				continue;
			BlockzapperItem.setTier(getUpgradedComponent(), getTier(), handgun);
			return handgun;
		}
		return ItemCooldownManager.tick;
	}

	@Override
	public ItemCooldownManager c() {
		ItemCooldownManager handgun = new ItemCooldownManager(AllItems.BLOCKZAPPER.get());
		BlockzapperItem.setTier(getUpgradedComponent(), getTier(), handgun);
		return handgun;
	}

	@Override
	public boolean af_() {
		return true;
	}
	
	@Override
	public Identifier f() {
		return getRecipe().f();
	}

//	@Override
//	public IRecipeType<?> getType() {
//		return AllRecipes.Types.BLOCKZAPPER_UPGRADE;
//	}
	
	@Override
	public MapExtendingRecipe<?> ag_() {
		return AllRecipeTypes.BLOCKZAPPER_UPGRADE.serializer;
	}
	
	public static class Serializer extends ForgeRegistryEntry<MapExtendingRecipe<?>> implements MapExtendingRecipe<BlockzapperUpgradeRecipe> {

		@Override
		public BlockzapperUpgradeRecipe a(Identifier recipeId, JsonObject json) {
			RecipeSerializer recipe = MapExtendingRecipe.a.a(recipeId, json);
			
			Components component = Components.valueOf(OrderedText.h(json, "component"));
			ComponentTier tier = ComponentTier.valueOf(OrderedText.h(json, "tier"));
			return new BlockzapperUpgradeRecipe(recipe, component, tier);
		}

		@Override
		public BlockzapperUpgradeRecipe a(Identifier recipeId, PacketByteBuf buffer) {
			RecipeSerializer recipe = MapExtendingRecipe.a.a(recipeId, buffer);
			
			Components component = Components.valueOf(buffer.readString(buffer.readInt()));
			ComponentTier tier = ComponentTier.valueOf(buffer.readString(buffer.readInt()));
			return new BlockzapperUpgradeRecipe(recipe, component, tier);
		}

		@Override
		public void write(PacketByteBuf buffer, BlockzapperUpgradeRecipe recipe) {
			MapExtendingRecipe.a.a(buffer, recipe.getRecipe());
			
			String name = recipe.getUpgradedComponent().name();
			String name2 = recipe.getTier().name();
			buffer.writeInt(name.length());
			buffer.writeString(name);
			buffer.writeInt(name2.length());
			buffer.writeString(name2);
		}
		
	}

	@Override
	public boolean a(int width, int height) {
		return getRecipe().a(width, height);
	}

	public RecipeSerializer getRecipe() {
		return recipe;
	}

	public Components getUpgradedComponent() {
		return component;
	}

	public ComponentTier getTier() {
		return tier;
	}

}
