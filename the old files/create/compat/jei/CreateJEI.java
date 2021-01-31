package com.simibubi.kinetic_api.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.compat.jei.category.BlockCuttingCategory;
import com.simibubi.kinetic_api.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.kinetic_api.compat.jei.category.BlockzapperUpgradeCategory;
import com.simibubi.kinetic_api.compat.jei.category.CreateRecipeCategory;
import com.simibubi.kinetic_api.compat.jei.category.CrushingCategory;
import com.simibubi.kinetic_api.compat.jei.category.FanBlastingCategory;
import com.simibubi.kinetic_api.compat.jei.category.FanSmokingCategory;
import com.simibubi.kinetic_api.compat.jei.category.FanWashingCategory;
import com.simibubi.kinetic_api.compat.jei.category.ItemDrainCategory;
import com.simibubi.kinetic_api.compat.jei.category.MechanicalCraftingCategory;
import com.simibubi.kinetic_api.compat.jei.category.MillingCategory;
import com.simibubi.kinetic_api.compat.jei.category.MixingCategory;
import com.simibubi.kinetic_api.compat.jei.category.MysteriousItemConversionCategory;
import com.simibubi.kinetic_api.compat.jei.category.PackingCategory;
import com.simibubi.kinetic_api.compat.jei.category.PolishingCategory;
import com.simibubi.kinetic_api.compat.jei.category.PressingCategory;
import com.simibubi.kinetic_api.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.kinetic_api.compat.jei.category.SawingCategory;
import com.simibubi.kinetic_api.compat.jei.category.SpoutCategory;
import com.simibubi.kinetic_api.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.kinetic_api.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinRecipe;
import com.simibubi.kinetic_api.content.logistics.block.inventories.AdjustableCrateScreen;
import com.simibubi.kinetic_api.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.kinetic_api.content.schematics.block.SchematicannonScreen;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.config.CRecipes;
import com.simibubi.kinetic_api.foundation.config.ConfigBase.ConfigBool;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

@JeiPlugin
public class CreateJEI implements IModPlugin {

	private static final Identifier ID = new Identifier(Create.ID, "jei_plugin");

	@Override
	@Nonnull
	public Identifier getPluginUid() {
		return ID;
	}

	public IIngredientManager ingredientManager;
	final List<CreateRecipeCategory<?>> ALL = new ArrayList<>();
	final CreateRecipeCategory<?>

	milling = register("milling", MillingCategory::new).recipes(AllRecipeTypes.MILLING)
		.catalyst(AllBlocks.MILLSTONE::get)
		.build(),

		crushing = register("crushing", CrushingCategory::new).recipes(AllRecipeTypes.CRUSHING)
			.recipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
			.catalyst(AllBlocks.CRUSHING_WHEEL::get)
			.build(),

		pressing = register("pressing", PressingCategory::new).recipes(AllRecipeTypes.PRESSING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.build(),

		washing = register("fan_washing", FanWashingCategory::new).recipes(AllRecipeTypes.SPLASHING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
			.build(),

		smoking = register("fan_smoking", FanSmokingCategory::new).recipes(() -> Recipe.d)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
			.build(),

		blasting = register("fan_blasting", FanBlastingCategory::new)
			.recipesExcluding(() -> Recipe.b, () -> Recipe.d)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
			.build(),

		blockzapper = register("blockzapper_upgrade", BlockzapperUpgradeCategory::new)
			.recipes(AllRecipeTypes.BLOCKZAPPER_UPGRADE.serializer.getRegistryName())
			.catalyst(AllItems.BLOCKZAPPER::get)
			.build(),

		mixing = register("mixing", MixingCategory::standard).recipes(AllRecipeTypes.MIXING::getType)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

		autoShapeless = register("automatic_shapeless", MixingCategory::autoShapeless)
			.recipes(r -> r.ag_() == MapExtendingRecipe.b && r.a()
				.size() > 1 && !MechanicalPressTileEntity.canCompress(r.a()),
				BasinRecipe::convertShapeless)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> c.allowShapelessInMixer)
			.build(),

		brewing = register("automatic_brewing", MixingCategory::autoBrewing)
			.recipeList(PotionMixingRecipeManager::getAllBrewingRecipes)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

		sawing = register("sawing", SawingCategory::new).recipes(AllRecipeTypes.CUTTING)
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.build(),

		blockCutting = register("block_cutting", BlockCuttingCategory::new)
			.recipeList(() -> CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(Recipe.f)))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhen(c -> c.allowStonecuttingOnSaw)
			.build(),

		packing = register("packing", PackingCategory::standard).recipes(AllRecipeTypes.COMPACTING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

		autoSquare = register("automatic_packing", PackingCategory::autoSquare)
			.recipes(r -> (r instanceof BlastingRecipe) && MechanicalPressTileEntity.canCompress(r.a()),
				BasinRecipe::convertShapeless)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> c.allowShapedSquareInPress)
			.build(),

		polishing = register("sandpaper_polishing", PolishingCategory::new).recipes(AllRecipeTypes.SANDPAPER_POLISHING)
			.catalyst(AllItems.SAND_PAPER::get)
			.catalyst(AllItems.RED_SAND_PAPER::get)
			.build(),

		mysteryConversion = register("mystery_conversion", MysteriousItemConversionCategory::new)
			.recipeList(MysteriousItemConversionCategory::getRecipes)
			.build(),

		spoutFilling = register("spout_filling", SpoutCategory::new).recipes(AllRecipeTypes.FILLING)
			.recipeList(() -> SpoutCategory.getRecipes(ingredientManager))
			.catalyst(AllBlocks.SPOUT::get)
			.build(),

		draining = register("draining", ItemDrainCategory::new)
			.recipeList(() -> ItemDrainCategory.getRecipes(ingredientManager))
			.recipes(AllRecipeTypes.EMPTYING)
			.catalyst(AllBlocks.ITEM_DRAIN::get)
			.build(),

		autoShaped = register("automatic_shaped", MechanicalCraftingCategory::new)
			.recipes(r -> r.ag_() == MapExtendingRecipe.b && r.a()
				.size() == 1)
			.recipes(
				r -> (r.g() == Recipe.a && r.g() != AllRecipeTypes.MECHANICAL_CRAFTING.type)
					&& (r instanceof RecipeSerializer))
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
			.enableWhen(c -> c.allowRegularCraftingInCrafter)
			.build(),

		mechanicalCrafting =
			register("mechanical_crafting", MechanicalCraftingCategory::new).recipes(AllRecipeTypes.MECHANICAL_CRAFTING)
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.build()

	;

	private <T extends Ingredient<?>> CategoryBuilder<T> register(String name,
		Supplier<CreateRecipeCategory<T>> supplier) {
		return new CategoryBuilder<T>(name, supplier);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(AllItems.BLOCKZAPPER.get());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		ALL.forEach(registration::addRecipeCategories);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ingredientManager = registration.getIngredientManager();
		ALL.forEach(c -> c.recipes.forEach(s -> registration.addRecipes(s.get(), c.getUid())));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		ALL.forEach(c -> c.recipeCatalysts.forEach(s -> registration.addRecipeCatalyst(s.get(), c.getUid())));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(AdjustableCrateScreen.class, new SlotMover<>());
		registration.addGuiContainerHandler(SchematicannonScreen.class, new SlotMover<>());
		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new FilterGhostIngredientHandler());
	}

	private class CategoryBuilder<T extends Ingredient<?>> {
		CreateRecipeCategory<T> category;
		private Predicate<CRecipes> pred;

		CategoryBuilder(String name, Supplier<CreateRecipeCategory<T>> category) {
			this.category = category.get();
			this.category.setCategoryId(name);
			this.pred = Predicates.alwaysTrue();
		}

		CategoryBuilder<T> catalyst(Supplier<GameRules> supplier) {
			return catalystStack(() -> new ItemCooldownManager(supplier.get()
				.h()));
		}

		CategoryBuilder<T> catalystStack(Supplier<ItemCooldownManager> supplier) {
			category.recipeCatalysts.add(supplier);
			return this;
		}

		CategoryBuilder<T> recipes(AllRecipeTypes recipeTypeEntry) {
			return recipes(recipeTypeEntry::getType);
		}

		CategoryBuilder<T> recipes(Supplier<Recipe<T>> recipeType) {
			return recipes(r -> r.g() == recipeType.get());
		}

		CategoryBuilder<T> recipes(Identifier serializer) {
			return recipes(r -> r.ag_()
				.getRegistryName()
				.equals(serializer));
		}

		CategoryBuilder<T> recipes(Predicate<Ingredient<?>> pred) {
			return recipeList(() -> findRecipes(pred));
		}

		CategoryBuilder<T> recipes(Predicate<Ingredient<?>> pred, Function<Ingredient<?>, T> converter) {
			return recipeList(() -> findRecipes(pred), converter);
		}

		CategoryBuilder<T> recipeList(Supplier<List<? extends Ingredient<?>>> list) {
			return recipeList(list, null);
		}

		CategoryBuilder<T> recipeList(Supplier<List<? extends Ingredient<?>>> list, Function<Ingredient<?>, T> converter) {
			category.recipes.add(() -> {
				if (!this.pred.test(AllConfigs.SERVER.recipes))
					return Collections.emptyList();
				if (converter != null)
					return list.get()
						.stream()
						.map(converter)
						.collect(Collectors.toList());
				return list.get();
			});
			return this;
		}

		CategoryBuilder<T> recipesExcluding(Supplier<Recipe<? extends T>> recipeType,
			Supplier<Recipe<? extends T>> excluded) {
			category.recipes.add(() -> {
				if (!this.pred.test(AllConfigs.SERVER.recipes))
					return Collections.emptyList();
				return findRecipesByTypeExcluding(recipeType.get(), excluded.get());
			});
			return this;
		}

		CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBool> configValue) {
			this.pred = c -> configValue.apply(c)
				.get()
				.booleanValue();
			return this;
		}

		CreateRecipeCategory<T> build() {
			ALL.add(category);
			return category;
		}

	}

	static List<Ingredient<?>> findRecipesByType(Recipe<?> type) {
		return findRecipes(r -> r.g() == type);
	}

	static List<Ingredient<?>> findRecipes(Predicate<Ingredient<?>> predicate) {
		return KeyBinding.B().r.o()
			.b()
			.stream()
			.filter(predicate)
			.collect(Collectors.toList());
	}

	static List<Ingredient<?>> findRecipesByTypeExcluding(Recipe<?> type, Recipe<?> excludingType) {
		List<Ingredient<?>> byType = findRecipes(r -> r.g() == type);
		List<Ingredient<?>> byExcludingType = findRecipes(r -> r.g() == excludingType);
		byType.removeIf(recipe -> {
			for (Ingredient<?> r : byExcludingType) {
				ItemCooldownManager[] matchingStacks = recipe.a()
					.get(0)
					.a();
				if (matchingStacks.length == 0)
					return true;
				if (r.a()
					.get(0)
					.a(matchingStacks[0]))
					return true;
			}
			return false;
		});
		return byType;
	}

}
