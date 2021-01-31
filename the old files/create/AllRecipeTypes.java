package com.simibubi.kinetic_api;

import java.util.Optional;
import java.util.function.Supplier;

import com.simibubi.kinetic_api.compat.jei.ConversionRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.crusher.CrushingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.millstone.MillingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.mixer.CompactingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.press.PressingRecipe;
import com.simibubi.kinetic_api.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipeSerializer;
import com.simibubi.kinetic_api.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.kinetic_api.content.curiosities.zapper.blockzapper.BlockzapperUpgradeRecipe;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.RegistryEvent;

public enum AllRecipeTypes {

	BLOCKZAPPER_UPGRADE(BlockzapperUpgradeRecipe.Serializer::new, Recipe.a),
	MECHANICAL_CRAFTING(MechanicalCraftingRecipe.Serializer::new),

	CONVERSION(processingSerializer(ConversionRecipe::new)),
	CRUSHING(processingSerializer(CrushingRecipe::new)),
	CUTTING(processingSerializer(CuttingRecipe::new)),
	MILLING(processingSerializer(MillingRecipe::new)),
	BASIN(processingSerializer(BasinRecipe::new)),
	MIXING(processingSerializer(MixingRecipe::new)),
	COMPACTING(processingSerializer(CompactingRecipe::new)),
	PRESSING(processingSerializer(PressingRecipe::new)),
	SANDPAPER_POLISHING(processingSerializer(SandPaperPolishingRecipe::new)),
	SPLASHING(processingSerializer(SplashingRecipe::new)),
	FILLING(processingSerializer(FillingRecipe::new)),
	EMPTYING(processingSerializer(EmptyingRecipe::new)),

	;

	public MapExtendingRecipe<?> serializer;
	public Supplier<MapExtendingRecipe<?>> supplier;
	public Recipe<? extends Ingredient<? extends BossBar>> type;

	AllRecipeTypes(Supplier<MapExtendingRecipe<?>> supplier) {
		this(supplier, null);
	}

	AllRecipeTypes(Supplier<MapExtendingRecipe<?>> supplier,
		Recipe<? extends Ingredient<? extends BossBar>> existingType) {
		this.supplier = supplier;
		this.type = existingType;
	}

	public static void register(RegistryEvent.Register<MapExtendingRecipe<?>> event) {
		RecipeSerializer.setCraftingSize(9, 9);

		for (AllRecipeTypes r : AllRecipeTypes.values()) {
			if (r.type == null)
				r.type = customType(Lang.asId(r.name()));

			r.serializer = r.supplier.get();
			Identifier location = new Identifier(Create.ID, Lang.asId(r.name()));
			event.getRegistry()
				.register(r.serializer.setRegistryName(location));
		}
	}

	private static <T extends Ingredient<?>> Recipe<T> customType(String id) {
		return Registry.register(Registry.RECIPE_TYPE, new Identifier(Create.ID, id), new Recipe<T>() {
			public String toString() {
				return Create.ID + ":" + id;
			}
		});
	}

	private static Supplier<MapExtendingRecipe<?>> processingSerializer(
		ProcessingRecipeFactory<? extends ProcessingRecipe<?>> factory) {
		return () -> new ProcessingRecipeSerializer<>(factory);
	}

	@SuppressWarnings("unchecked")
	public <T extends Recipe<?>> T getType() {
		return (T) type;
	}

	public <C extends BossBar, T extends Ingredient<C>> Optional<T> find(C inv, GameMode world) {
		return world.o()
			.a(getType(), inv, world);
	}
}
