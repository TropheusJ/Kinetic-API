package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.Mods.EID;
import static com.simibubi.create.foundation.data.recipe.Mods.IE;
import static com.simibubi.create.foundation.data.recipe.Mods.INF;
import static com.simibubi.create.foundation.data.recipe.Mods.MEK;
import static com.simibubi.create.foundation.data.recipe.Mods.MW;
import static com.simibubi.create.foundation.data.recipe.Mods.SM;
import static com.simibubi.create.foundation.data.recipe.Mods.TH;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen.GeneratedRecipeBuilder;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.recipe.CookingRecipeJsonFactory;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonFactory;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

@SuppressWarnings("unused")
public class StandardRecipeGen extends CreateRecipeProvider {

	/*
	 * Recipes are added through fields, so one can navigate to the right one easily
	 * 
	 * (Ctrl-o) in Eclipse
	 */

	private Marker MATERIALS = enterSection(AllSections.MATERIALS);

	GeneratedRecipe

	COPPER_COMPACTING =
		metalCompacting(ImmutableList.of(AllItems.COPPER_NUGGET, AllItems.COPPER_INGOT, AllBlocks.COPPER_BLOCK),
			ImmutableList.of(I::copperNugget, I::copper, I::copperBlock)),

		BRASS_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.BRASS_NUGGET, AllItems.BRASS_INGOT, AllBlocks.BRASS_BLOCK),
				ImmutableList.of(I::brassNugget, I::brass, I::brassBlock)),

		ZINC_COMPACTING =
			metalCompacting(ImmutableList.of(AllItems.ZINC_NUGGET, AllItems.ZINC_INGOT, AllBlocks.ZINC_BLOCK),
				ImmutableList.of(I::zincNugget, I::zinc, I::zincBlock)),

		ANDESITE_ALLOY = create(AllItems.ANDESITE_ALLOY).unlockedByTag(I::iron)
			.viaShaped(b -> b.a('A', BellBlock.BELL_LIP_SHAPE)
				.a('B', Tags.Items.NUGGETS_IRON)
				.pattern("BA")
				.pattern("AB")),

		ANDESITE_ALLOY_FROM_ZINC = create(AllItems.ANDESITE_ALLOY).withSuffix("_from_zinc")
			.unlockedByTag(I::zinc)
			.viaShaped(b -> b.a('A', BellBlock.BELL_LIP_SHAPE)
				.a('B', I.zincNugget())
				.pattern("BA")
				.pattern("AB")),

		ANDESITE_CASING = create(AllBlocks.ANDESITE_CASING).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', EntityTypeTags.field_19168)
				.a('C', I.andesite())
				.a('S', EntityTypeTags.q)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		BRASS_CASING = create(AllBlocks.BRASS_CASING).returns(4)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.a('A', EntityTypeTags.field_19168)
				.a('C', I.brassSheet())
				.a('S', EntityTypeTags.q)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		COPPER_CASING = create(AllBlocks.COPPER_CASING).returns(4)
			.unlockedByTag(I::copper)
			.viaShaped(b -> b.a('A', EntityTypeTags.field_19168)
				.a('C', I.copperSheet())
				.a('S', EntityTypeTags.q)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		RADIANT_CASING = create(AllBlocks.REFINED_RADIANCE_CASING).returns(4)
			.unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.a('A', EntityTypeTags.field_19168)
				.a('C', I.refinedRadiance())
				.a('S', Tags.Items.GLASS_COLORLESS)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		SHADOW_CASING = create(AllBlocks.SHADOW_STEEL_CASING).returns(4)
			.unlockedBy(I::shadowSteel)
			.viaShaped(b -> b.a('A', EntityTypeTags.field_19168)
				.a('C', I.shadowSteel())
				.a('S', Tags.Items.OBSIDIAN)
				.pattern("AAA")
				.pattern("CSC")
				.pattern("AAA")),

		ELECTRON_TUBE = create(AllItems.ELECTRON_TUBE).unlockedBy(AllItems.ROSE_QUARTZ::get)
			.viaShaped(b -> b.a('L', AllItems.POLISHED_ROSE_QUARTZ.get())
				.a('R', AliasedBlockItem.cT)
				.a('N', Tags.Items.NUGGETS_IRON)
				.pattern("L")
				.pattern("R")
				.pattern("N")),

		ROSE_QUARTZ = create(AllItems.ROSE_QUARTZ).unlockedBy(() -> AliasedBlockItem.lP)
			.viaShapeless(b -> b.a(Tags.Items.GEMS_QUARTZ)
				.a(FireworkRocketRecipe.a(I.redstone()), 8)),

		SAND_PAPER = create(AllItems.SAND_PAPER).unlockedBy(() -> AliasedBlockItem.mb)
			.viaShapeless(b -> b.b(AliasedBlockItem.mb)
				.a(Tags.Items.SAND_COLORLESS)),

		RED_SAND_PAPER = create(AllItems.RED_SAND_PAPER).unlockedBy(() -> AliasedBlockItem.mb)
			.viaShapeless(b -> b.b(AliasedBlockItem.mb)
				.a(Tags.Items.SAND_RED))

	;

	private Marker CURIOSITIES = enterSection(AllSections.CURIOSITIES);

	GeneratedRecipe DEFORESTER = create(AllItems.DEFORESTER).unlockedBy(I::refinedRadiance)
		.viaShaped(b -> b.a('E', I.refinedRadiance())
			.a('G', I.cog())
			.a('O', Tags.Items.OBSIDIAN)
			.pattern("EG")
			.pattern("EO")
			.pattern(" O")),

		WAND_OF_SYMMETRY = create(AllItems.WAND_OF_SYMMETRY).unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.a('E', I.refinedRadiance())
				.a('G', Tags.Items.GLASS_PANES_WHITE)
				.a('O', Tags.Items.OBSIDIAN)
				.a('L', I.brass())
				.pattern(" GE")
				.pattern("LEG")
				.pattern("OL ")),

		MINECART_COUPLING = create(AllItems.MINECART_COUPLING).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('E', I.andesite())
				.a('O', I.ironSheet())
				.pattern("  E")
				.pattern(" O ")
				.pattern("E  ")),

		BLOCKZAPPER = create(AllItems.BLOCKZAPPER).unlockedBy(I::refinedRadiance)
			.viaShaped(b -> b.a('E', I.refinedRadiance())
				.a('A', I.andesite())
				.a('O', Tags.Items.OBSIDIAN)
				.pattern("  E")
				.pattern(" O ")
				.pattern("OA "))

	;

	private Marker KINETICS = enterSection(AllSections.KINETICS);

	GeneratedRecipe BASIN = create(AllBlocks.BASIN).unlockedBy(I::andesite)
		.viaShaped(b -> b.a('A', I.andesite())
			.pattern("A A")
			.pattern("AAA")),

		GOGGLES = create(AllItems.GOGGLES).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('G', Tags.Items.GLASS)
				.a('P', I.goldSheet())
				.a('S', Tags.Items.STRING)
				.pattern(" S ")
				.pattern("GPG")),

		WRENCH = create(AllItems.WRENCH).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('G', I.goldSheet())
				.a('P', I.cog())
				.a('S', Tags.Items.RODS_WOODEN)
				.pattern("GG")
				.pattern("GP")
				.pattern(" S")),

		FILTER = create(AllItems.FILTER).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', EntityTypeTags.field_15507)
				.a('A', Tags.Items.NUGGETS_IRON)
				.pattern("ASA")),

		ATTRIBUTE_FILTER = create(AllItems.ATTRIBUTE_FILTER).unlockedByTag(I::brass)
			.viaShaped(b -> b.a('S', EntityTypeTags.field_15507)
				.a('A', I.brassNugget())
				.pattern("ASA")),

		BRASS_HAND = create(AllItems.BRASS_HAND).unlockedByTag(I::brass)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('B', I.brassSheet())
				.pattern(" A ")
				.pattern("BBB")
				.pattern(" B ")),

		SUPER_GLUE = create(AllItems.SUPER_GLUE).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.a('A', Tags.Items.SLIMEBALLS)
				.a('S', I.ironSheet())
				.a('N', Tags.Items.NUGGETS_IRON)
				.pattern("AS")
				.pattern("NA")),

		CRAFTER_SLOT_COVER = create(AllItems.CRAFTER_SLOT_COVER).unlockedBy(AllBlocks.MECHANICAL_CRAFTER::get)
			.viaShaped(b -> b.a('A', I.brassNugget())
				.pattern("AAA")),

		COGWHEEL = create(AllBlocks.COGWHEEL).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', EntityTypeTags.field_21508)
				.a('C', I.andesite())
				.pattern("SSS")
				.pattern("SCS")
				.pattern("SSS")),

		LARGE_COGWHEEL = create(AllBlocks.LARGE_COGWHEEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', EntityTypeTags.field_21508)
				.a('C', I.andesite())
				.a('D', EntityTypeTags.field_19168)
				.pattern("SDS")
				.pattern("DCD")
				.pattern("SDS")),

		WATER_WHEEL = create(AllBlocks.WATER_WHEEL).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', EntityTypeTags.j)
				.a('C', AllBlocks.LARGE_COGWHEEL.get())
				.pattern("SSS")
				.pattern("SCS")
				.pattern("SSS")),

		SHAFT = create(AllBlocks.SHAFT).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', I.andesite())
				.pattern("A")
				.pattern("A")),

		MECHANICAL_PRESS = create(AllBlocks.MECHANICAL_PRESS).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('B', I.andesite())
				.a('S', I.cog())
				.a('C', I.andesiteCasing())
				.a('I', AllTags.forgeItemTag("storage_blocks/iron"))
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		MILLSTONE = create(AllBlocks.MILLSTONE).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('B', EntityTypeTags.field_19168)
				.a('S', I.andesite())
				.a('C', I.cog())
				.a('I', I.stone())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		MECHANICAL_PISTON = create(AllBlocks.MECHANICAL_PISTON).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('B', EntityTypeTags.field_19168)
				.a('S', I.cog())
				.a('C', I.andesiteCasing())
				.a('I', AllBlocks.PISTON_EXTENSION_POLE.get())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		STICKY_MECHANICAL_PISTON = create(AllBlocks.STICKY_MECHANICAL_PISTON).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', Tags.Items.SLIMEBALLS)
				.a('P', AllBlocks.MECHANICAL_PISTON.get())
				.pattern("S")
				.pattern("P")),

		TURNTABLE = create(AllBlocks.TURNTABLE).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', I.shaft())
				.a('P', EntityTypeTags.j)
				.pattern("P")
				.pattern("S")),

		PISTON_EXTENSION_POLE = create(AllBlocks.PISTON_EXTENSION_POLE).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('P', EntityTypeTags.field_19168)
				.pattern("P")
				.pattern("A")
				.pattern("P")),

		ANALOG_LEVER = create(AllBlocks.ANALOG_LEVER).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', I.andesiteCasing())
				.a('P', Tags.Items.RODS_WOODEN)
				.pattern("P")
				.pattern("S")),

		BELT_CONNECTOR = create(AllItems.BELT_CONNECTOR).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('D', AliasedBlockItem.ni)
				.pattern("DDD")
				.pattern("DDD")),

		ADJUSTABLE_PULLEY = create(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('A', I.electronTube())
				.a('B', AllBlocks.ENCASED_CHAIN_DRIVE.get())
				.a('C', AllBlocks.LARGE_COGWHEEL.get())
				.pattern("A")
				.pattern("B")
				.pattern("C")),

		CART_ASSEMBLER = create(AllBlocks.CART_ASSEMBLER).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('L', EntityTypeTags.q)
				.a('R', I.redstone())
				.a('C', I.andesite())
				.pattern(" L ")
				.pattern("CRC")
				.pattern("L L")),

		CONTROLLER_RAIL = create(AllBlocks.CONTROLLER_RAIL).returns(6)
			.unlockedBy(() -> AliasedBlockItem.aH)
			.viaShaped(b -> b.a('A', I.gold())
				.a('E', I.electronTube())
				.a('S', Tags.Items.RODS_WOODEN)
				.pattern("A A")
				.pattern("ASA")
				.pattern("AEA")),

		HAND_CRANK = create(AllBlocks.HAND_CRANK).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('C', EntityTypeTags.field_19168)
				.a('S', I.shaft())
				.pattern(" S ")
				.pattern("CCC")
				.pattern("  A")),

		COPPER_VALVE_HANDLE = create(AllBlocks.COPPER_VALVE_HANDLE).unlockedByTag(I::copper)
			.viaShaped(b -> b.a('S', I.andesite())
				.a('C', I.copperSheet())
				.pattern("CCC")
				.pattern(" S ")),

		COPPER_VALVE_HANDLE_FROM_OTHER_HANDLES = create(AllBlocks.COPPER_VALVE_HANDLE).withSuffix("_from_others")
			.unlockedByTag(I::copper)
			.viaShapeless(b -> b.a(AllItemTags.VALVE_HANDLES.tag)),

		NOZZLE = create(AllBlocks.NOZZLE).unlockedBy(AllBlocks.ENCASED_FAN::get)
			.viaShaped(b -> b.a('S', I.andesite())
				.a('C', EntityTypeTags.field_15507)
				.pattern(" S ")
				.pattern(" C ")
				.pattern("SSS")),

		PROPELLER = create(AllItems.PROPELLER).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.a('S', I.ironSheet())
				.a('C', I.andesite())
				.pattern(" S ")
				.pattern("SCS")
				.pattern(" S ")),

		WHISK = create(AllItems.WHISK).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.a('S', I.ironSheet())
				.a('C', I.andesite())
				.pattern(" C ")
				.pattern("SCS")
				.pattern("SSS")),

		ENCASED_FAN = create(AllBlocks.ENCASED_FAN).unlockedByTag(I::ironSheet)
			.viaShaped(b -> b.a('S', I.shaft())
				.a('A', I.andesiteCasing())
				.a('R', I.cog())
				.a('P', AllItems.PROPELLER.get())
				.pattern(" S ")
				.pattern("RAR")
				.pattern(" P ")),

		CUCKOO_CLOCK = create(AllBlocks.CUCKOO_CLOCK).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', EntityTypeTags.field_19168)
				.a('A', AliasedBlockItem.mj)
				.a('B', EntityTypeTags.q)
				.a('P', I.cog())
				.pattern(" S ")
				.pattern("SAS")
				.pattern("BPB")),

		MECHANICAL_CRAFTER = create(AllBlocks.MECHANICAL_CRAFTER).returns(3)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('B', I.electronTube())
				.a('R', BellBlock.bV)
				.a('C', I.brassCasing())
				.a('S', I.cog())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" R ")),

		WINDMILL_BEARING = create(AllBlocks.WINDMILL_BEARING).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('I', I.shaft())
				.a('B', AllBlocks.TURNTABLE.get())
				.a('C', I.stone())
				.pattern(" B ")
				.pattern(" C ")
				.pattern(" I ")),

		MECHANICAL_BEARING = create(AllBlocks.MECHANICAL_BEARING).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('I', I.shaft())
				.a('S', I.andesite())
				.a('B', AllBlocks.TURNTABLE.get())
				.a('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		CLOCKWORK_BEARING = create(AllBlocks.CLOCKWORK_BEARING).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('I', I.shaft())
				.a('S', I.electronTube())
				.a('B', AllBlocks.TURNTABLE.get())
				.a('C', I.brassCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		WOODEN_BRACKET = create(AllBlocks.WOODEN_BRACKET).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', Tags.Items.RODS_WOODEN)
				.a('P', I.planks())
				.a('C', I.andesite())
				.pattern("SSS")
				.pattern("PCP")),

		METAL_BRACKET = create(AllBlocks.METAL_BRACKET).returns(4)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', Tags.Items.NUGGETS_IRON)
				.a('P', I.iron())
				.a('C', I.andesite())
				.pattern("SSS")
				.pattern("PCP")),

		FLUID_PIPE = create(AllBlocks.FLUID_PIPE).returns(8)
			.unlockedByTag(I::copper)
			.viaShaped(b -> b.a('S', I.copperSheet())
				.a('C', I.copper())
				.pattern("SCS")),

		MECHANICAL_PUMP = create(AllBlocks.MECHANICAL_PUMP).unlockedByTag(I::copper)
			.viaShaped(b -> b.a('P', I.cog())
				.a('S', AllBlocks.FLUID_PIPE.get())
				.pattern("P")
				.pattern("S")),

		SMART_FLUID_PIPE = create(AllBlocks.SMART_FLUID_PIPE).unlockedByTag(I::copper)
			.viaShaped(b -> b.a('P', I.electronTube())
				.a('S', AllBlocks.FLUID_PIPE.get())
				.a('I', I.brassSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		FLUID_VALVE = create(AllBlocks.FLUID_VALVE).unlockedByTag(I::copper)
			.viaShaped(b -> b.a('P', I.shaft())
				.a('S', AllBlocks.FLUID_PIPE.get())
				.a('I', I.ironSheet())
				.pattern("I")
				.pattern("S")
				.pattern("P")),

		SPOUT = create(AllBlocks.SPOUT).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.a('T', AllBlocks.FLUID_TANK.get())
				.a('P', AliasedBlockItem.ni)
				.a('S', I.copperNugget())
				.pattern("T")
				.pattern("P")
				.pattern("S")),

		ITEM_DRAIN = create(AllBlocks.ITEM_DRAIN).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.a('P', BellBlock.dH)
				.a('S', I.copperCasing())
				.pattern("P")
				.pattern("S")),

		FLUID_TANK = create(AllBlocks.FLUID_TANK).returns(2)
			.unlockedBy(I::copperCasing)
			.viaShaped(b -> b.a('B', I.copperCasing())
				.a('S', I.copperNugget())
				.a('C', Tags.Items.GLASS)
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		DEPLOYER = create(AllBlocks.DEPLOYER).unlockedBy(I::electronTube)
			.viaShaped(b -> b.a('I', AllItems.BRASS_HAND.get())
				.a('B', I.electronTube())
				.a('S', I.cog())
				.a('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		PORTABLE_STORAGE_INTERFACE = create(AllBlocks.PORTABLE_STORAGE_INTERFACE).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('I', I.brassCasing())
				.a('B', AllBlocks.ANDESITE_FUNNEL.get())
				.pattern(" B ")
				.pattern(" I ")),

		PORTABLE_FLUID_INTERFACE = create(AllBlocks.PORTABLE_FLUID_INTERFACE).unlockedBy(I::copperCasing)
			.viaShaped(b -> b.a('I', I.copperCasing())
				.a('B', AllBlocks.ANDESITE_FUNNEL.get())
				.pattern(" B ")
				.pattern(" I ")),

		ROPE_PULLEY = create(AllBlocks.ROPE_PULLEY).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', I.shaft())
				.a('B', I.andesiteCasing())
				.a('C', EntityTypeTags.field_15507)
				.a('I', I.ironSheet())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		HOSE_PULLEY = create(AllBlocks.HOSE_PULLEY).unlockedByTag(I::copper)
			.viaShaped(b -> b.a('S', I.shaft())
				.a('P', AllBlocks.FLUID_PIPE.get())
				.a('B', I.copperCasing())
				.a('C', AliasedBlockItem.ni)
				.a('I', I.copperSheet())
				.pattern(" B ")
				.pattern("SCP")
				.pattern(" I ")),

		EMPTY_BLAZE_BURNER = create(AllItems.EMPTY_BLAZE_BURNER).unlockedByTag(I::iron)
			.viaShaped(b -> b.a('A', BellBlock.dH)
				.a('I', I.ironSheet())
				.pattern("II")
				.pattern("AA")),

		CHUTE = create(AllBlocks.CHUTE).unlockedBy(I::andesite)
			.returns(4)
			.viaShaped(b -> b.a('A', I.ironSheet())
				.a('I', I.andesite())
				.pattern("II")
				.pattern("AA")),

		DEPOT = create(AllBlocks.DEPOT).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('I', I.andesiteCasing())
				.pattern("A")
				.pattern("I")),

		MECHANICAL_ARM = create(AllBlocks.MECHANICAL_ARM::get).unlockedBy(I::brassCasing)
			.returns(1)
			.viaShaped(b -> b.a('L', I.brassSheet())
				.a('R', I.cog())
				.a('I', I.electronTube())
				.a('A', I.andesite())
				.a('C', I.brassCasing())
				.pattern("LLA")
				.pattern("LR ")
				.pattern("ICI")),

		MECHANICAL_MIXER = create(AllBlocks.MECHANICAL_MIXER).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', I.cog())
				.a('B', I.andesite())
				.a('C', I.andesiteCasing())
				.a('I', AllItems.WHISK.get())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I ")),

		CLUTCH = create(AllBlocks.CLUTCH).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', I.shaft())
				.a('B', I.redstone())
				.a('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		GEARSHIFT = create(AllBlocks.GEARSHIFT).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('S', I.cog())
				.a('B', I.redstone())
				.a('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		SAIL_FRAME = create(AllBlocks.SAIL_FRAME).returns(8)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('S', Tags.Items.RODS_WOODEN)
				.pattern("SSS")
				.pattern("SAS")
				.pattern("SSS")),

		SAIL = create(AllBlocks.SAIL).returns(8)
			.unlockedBy(AllBlocks.SAIL_FRAME::get)
			.viaShaped(b -> b.a('F', AllBlocks.SAIL_FRAME.get())
				.a('W', EntityTypeTags.field_15507)
				.pattern("FFF")
				.pattern("FWF")
				.pattern("FFF")),

		RADIAL_CHASIS = create(AllBlocks.RADIAL_CHASSIS).returns(3)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('P', I.andesite())
				.a('L', EntityTypeTags.q)
				.pattern(" L ")
				.pattern("PLP")
				.pattern(" L ")),

		LINEAR_CHASIS = create(AllBlocks.LINEAR_CHASSIS).returns(3)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('P', I.andesite())
				.a('L', EntityTypeTags.q)
				.pattern(" P ")
				.pattern("LLL")
				.pattern(" P ")),

		LINEAR_CHASSIS_CYCLE =
			conversionCycle(ImmutableList.of(AllBlocks.LINEAR_CHASSIS, AllBlocks.SECONDARY_LINEAR_CHASSIS)),

		MINECART = create(() -> AliasedBlockItem.lN).withSuffix("_from_contraption_cart")
			.unlockedBy(AllBlocks.CART_ASSEMBLER::get)
			.viaShapeless(b -> b.b(AllItems.MINECART_CONTRAPTION.get())),

		FURNACE_MINECART = create(() -> AliasedBlockItem.mf).withSuffix("_from_contraption_cart")
			.unlockedBy(AllBlocks.CART_ASSEMBLER::get)
			.viaShapeless(b -> b.b(AllItems.FURNACE_MINECART_CONTRAPTION.get())),

		GEARBOX = create(AllBlocks.GEARBOX).unlockedBy(I::cog)
			.viaShaped(b -> b.a('C', I.cog())
				.a('B', I.andesiteCasing())
				.pattern(" C ")
				.pattern("CBC")
				.pattern(" C ")),

		GEARBOX_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.GEARBOX, AllItems.VERTICAL_GEARBOX)),

		MYSTERIOUS_CUCKOO_CLOCK = create(AllBlocks.MYSTERIOUS_CUCKOO_CLOCK).unlockedBy(AllBlocks.CUCKOO_CLOCK::get)
			.viaShaped(b -> b.a('C', Tags.Items.GUNPOWDER)
				.a('B', AllBlocks.CUCKOO_CLOCK.get())
				.pattern(" C ")
				.pattern("CBC")
				.pattern(" C ")),

		ENCASED_CHAIN_DRIVE = create(AllBlocks.ENCASED_CHAIN_DRIVE).returns(2)
			.unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('S', I.shaft())
				.a('B', Tags.Items.NUGGETS_IRON)
				.a('C', I.andesiteCasing())
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" B ")),

		SPEEDOMETER = create(AllBlocks.SPEEDOMETER).unlockedBy(I::andesite)
			.viaShaped(b -> b.a('C', AliasedBlockItem.mh)
				.a('A', I.andesiteCasing())
				.a('S', I.shaft())
				.pattern(" C ")
				.pattern("SAS")),

		GAUGE_CYCLE = conversionCycle(ImmutableList.of(AllBlocks.SPEEDOMETER, AllBlocks.STRESSOMETER)),

		ROTATION_SPEED_CONTROLLER = create(AllBlocks.ROTATION_SPEED_CONTROLLER).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('B', I.circuit())
				.a('C', I.brassCasing())
				.a('S', I.shaft())
				.pattern(" B ")
				.pattern("SCS")),

		NIXIE_TUBE = create(AllBlocks.NIXIE_TUBE).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('E', I.electronTube())
				.a('B', I.brassCasing())
				.pattern("EBE")),

		MECHANICAL_SAW = create(AllBlocks.MECHANICAL_SAW).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('C', I.andesiteCasing())
				.a('A', I.ironSheet())
				.a('I', I.iron())
				.pattern(" A ")
				.pattern("AIA")
				.pattern(" C ")),

		MECHANICAL_HARVESTER = create(AllBlocks.MECHANICAL_HARVESTER).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('C', I.andesiteCasing())
				.a('A', I.andesite())
				.a('I', I.ironSheet())
				.pattern("AIA")
				.pattern("AIA")
				.pattern(" C ")),

		MECHANICAL_PLOUGH = create(AllBlocks.MECHANICAL_PLOUGH).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('C', I.andesiteCasing())
				.a('A', I.andesite())
				.a('I', I.ironSheet())
				.pattern("III")
				.pattern("AAA")
				.pattern(" C ")),

		MECHANICAL_DRILL = create(AllBlocks.MECHANICAL_DRILL).unlockedBy(I::andesiteCasing)
			.viaShaped(b -> b.a('C', I.andesiteCasing())
				.a('A', I.andesite())
				.a('I', I.iron())
				.pattern(" A ")
				.pattern("AIA")
				.pattern(" C ")),

		SEQUENCED_GEARSHIFT = create(AllBlocks.SEQUENCED_GEARSHIFT).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('B', I.electronTube())
				.a('S', I.cog())
				.a('C', I.brassCasing())
				.a('I', AliasedBlockItem.mj)
				.pattern(" B ")
				.pattern("SCS")
				.pattern(" I "))

	;

	private Marker LOGISTICS = enterSection(AllSections.LOGISTICS);

	GeneratedRecipe

	REDSTONE_CONTACT = create(AllBlocks.REDSTONE_CONTACT).returns(2)
		.unlockedBy(I::brassCasing)
		.viaShaped(b -> b.a('W', I.redstone())
			.a('D', I.brassCasing())
			.a('S', I.iron())
			.pattern("WDW")
			.pattern(" S ")
			.pattern("WDW")),

		ANDESITE_FUNNEL = create(AllBlocks.ANDESITE_FUNNEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('K', AliasedBlockItem.ni)
				.pattern("AKA")
				.pattern(" K ")),

		BRASS_FUNNEL = create(AllBlocks.BRASS_FUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.a('A', I.brass())
				.a('K', AliasedBlockItem.ni)
				.a('E', I.electronTube())
				.pattern("AEA")
				.pattern(" K ")),

		ANDESITE_TUNNEL = create(AllBlocks.ANDESITE_TUNNEL).returns(2)
			.unlockedBy(I::andesite)
			.viaShaped(b -> b.a('A', I.andesite())
				.a('K', AliasedBlockItem.ni)
				.pattern("AA")
				.pattern("KK")),

		BRASS_TUNNEL = create(AllBlocks.BRASS_TUNNEL).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.a('A', I.brass())
				.a('K', AliasedBlockItem.ni)
				.a('E', I.electronTube())
				.pattern("E ")
				.pattern("AA")
				.pattern("KK")),

		ADJUSTABLE_CRATE = create(AllBlocks.ADJUSTABLE_CRATE).returns(4)
			.unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('B', I.brassCasing())
				.pattern("BBB")
				.pattern("B B")
				.pattern("BBB")),

		BELT_OBSERVER = create(AllBlocks.CONTENT_OBSERVER).unlockedBy(AllItems.BELT_CONNECTOR::get)
			.viaShaped(b -> b.a('B', I.brassCasing())
				.a('R', I.redstone())
				.a('I', I.iron())
				.a('C', BellBlock.iO)
				.pattern("RCI")
				.pattern(" B ")),

		STOCKPILE_SWITCH = create(AllBlocks.STOCKPILE_SWITCH).unlockedBy(I::brassCasing)
			.viaShaped(b -> b.a('B', I.brassCasing())
				.a('R', I.redstone())
				.a('I', I.iron())
				.a('C', BellBlock.fu)
				.pattern("RCI")
				.pattern(" B ")),

		ADJUSTABLE_REPEATER = create(AllBlocks.ADJUSTABLE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.a('T', BellBlock.cz)
				.a('C', AliasedBlockItem.mj)
				.a('R', I.redstone())
				.a('S', I.stone())
				.pattern("RCT")
				.pattern("SSS")),

		ADJUSTABLE_PULSE_REPEATER = create(AllBlocks.ADJUSTABLE_PULSE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.a('S', AllBlocks.PULSE_REPEATER.get())
				.a('P', AllBlocks.ADJUSTABLE_REPEATER.get())
				.pattern("SP")),

		PULSE_REPEATER = create(AllBlocks.PULSE_REPEATER).unlockedByTag(I::redstone)
			.viaShaped(b -> b.a('T', BellBlock.cz)
				.a('R', I.redstone())
				.a('S', I.stone())
				.pattern("RRT")
				.pattern("SSS")),

		POWERED_TOGGLE_LATCH = create(AllBlocks.POWERED_TOGGLE_LATCH).unlockedByTag(I::redstone)
			.viaShaped(b -> b.a('T', BellBlock.cz)
				.a('C', BellBlock.cp)
				.a('S', I.stone())
				.pattern(" T ")
				.pattern(" C ")
				.pattern("SSS")),

		POWERED_LATCH = create(AllBlocks.POWERED_LATCH).unlockedByTag(I::redstone)
			.viaShaped(b -> b.a('T', BellBlock.cz)
				.a('C', BellBlock.cp)
				.a('R', I.redstone())
				.a('S', I.stone())
				.pattern(" T ")
				.pattern("RCR")
				.pattern("SSS")),

		REDSTONE_LINK = create(AllBlocks.REDSTONE_LINK).returns(2)
			.unlockedByTag(I::brass)
			.viaShaped(b -> b.a('C', BellBlock.cz)
				.a('S', I.brassSheet())
				.a('I', EntityTypeTags.field_19168)
				.pattern("  C")
				.pattern("SIS"))

	;

	private Marker SCHEMATICS = enterSection(AllSections.SCHEMATICS);

	GeneratedRecipe

	SCHEMATIC_TABLE = create(AllBlocks.SCHEMATIC_TABLE).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
		.viaShaped(b -> b.a('W', EntityTypeTags.j)
			.a('S', BellBlock.id)
			.pattern("WWW")
			.pattern(" S ")
			.pattern(" S ")),

		SCHEMATICANNON = create(AllBlocks.SCHEMATICANNON).unlockedBy(AllItems.EMPTY_SCHEMATIC::get)
			.viaShaped(b -> b.a('L', EntityTypeTags.q)
				.a('D', BellBlock.as)
				.a('C', BellBlock.eb)
				.a('S', BellBlock.id)
				.a('I', BellBlock.bF)
				.pattern(" C ")
				.pattern("LDL")
				.pattern("SIS")),

		EMPTY_SCHEMATIC = create(AllItems.EMPTY_SCHEMATIC).unlockedBy(() -> AliasedBlockItem.mb)
			.viaShapeless(b -> b.b(AliasedBlockItem.mb)
				.a(Tags.Items.DYES_LIGHT_BLUE)),

		SCHEMATIC_AND_QUILL = create(AllItems.SCHEMATIC_AND_QUILL).unlockedBy(() -> AliasedBlockItem.mb)
			.viaShapeless(b -> b.b(AllItems.EMPTY_SCHEMATIC.get())
				.a(Tags.Items.FEATHERS))

	;

	private Marker PALETTES = enterSection(AllSections.PALETTES);

	GeneratedRecipe

	DARK_SCORIA = create(AllPaletteBlocks.DARK_SCORIA).returns(8)
		.unlockedBy(() -> AllPaletteBlocks.SCORIA.get())
		.viaShaped(b -> b.a('#', AllPaletteBlocks.SCORIA.get())
			.a('D', Tags.Items.DYES_BLACK)
			.pattern("###")
			.pattern("#D#")
			.pattern("###")),

		COPPER_SHINGLES = create(AllBlocks.COPPER_SHINGLES).returns(16)
			.unlockedByTag(I::copperSheet)
			.viaShaped(b -> b.a('#', I.copperSheet())
				.pattern("##")
				.pattern("##")),

		COPPER_SHINGLES_FROM_TILES = create(AllBlocks.COPPER_SHINGLES).withSuffix("_from_tiles")
			.unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.b(AllBlocks.COPPER_TILES.get())),

		COPPER_TILES = create(AllBlocks.COPPER_TILES).unlockedByTag(I::copperSheet)
			.viaShapeless(b -> b.b(AllBlocks.COPPER_SHINGLES.get()))

	;

	private Marker APPLIANCES = enterFolder("appliances");

	GeneratedRecipe

	DOUGH = create(AllItems.DOUGH).unlockedBy(AllItems.WHEAT_FLOUR::get)
		.viaShapeless(b -> b.b(AllItems.WHEAT_FLOUR.get())
			.b(AliasedBlockItem.lL)),

		SLIME_BALL = create(() -> AliasedBlockItem.md).unlockedBy(AllItems.DOUGH::get)
			.viaShapeless(b -> b.b(AllItems.DOUGH.get())
				.a(Tags.Items.DYES_LIME)),

		TREE_FERTILIZER = create(AllItems.TREE_FERTILIZER).returns(2)
			.unlockedBy(() -> AliasedBlockItem.mK)
			.viaShapeless(b -> b.a(FireworkRocketRecipe.a(EntityTypeTags.I), 2)
				.a(FireworkRocketRecipe.a(AliasedBlockItem.iR, AliasedBlockItem.iO, AliasedBlockItem.iN,
					AliasedBlockItem.iP, AliasedBlockItem.iQ))
				.b(AliasedBlockItem.mK))

	;

	private Marker COOKING = enterFolder("/");

	GeneratedRecipe

	DOUGH_TO_BREAD = create(() -> AliasedBlockItem.kX).viaCooking(AllItems.DOUGH::get)
		.inSmoker(),

		LIMESAND = create(AllPaletteBlocks.LIMESTONE::get).viaCooking(AllPaletteBlocks.LIMESAND::get)
			.inFurnace(),
		SOUL_SAND = create(AllPaletteBlocks.SCORIA::get).viaCooking(() -> BellBlock.cM)
			.inFurnace(),
		DIORITE = create(AllPaletteBlocks.DOLOMITE::get).viaCooking(() -> BellBlock.EAST_WEST_SHAPE)
			.inFurnace(),
		GRANITE = create(AllPaletteBlocks.GABBRO::get).viaCooking(() -> BellBlock.POWERED)
			.inFurnace(),
		NAT_SCORIA = create(AllPaletteBlocks.SCORIA::get).withSuffix("_from_natural")
			.viaCooking(AllPaletteBlocks.NATURAL_SCORIA::get)
			.inFurnace(),

		FRAMED_GLASS = recycleGlass(AllPaletteBlocks.FRAMED_GLASS),
		TILED_GLASS = recycleGlass(AllPaletteBlocks.TILED_GLASS),
		VERTICAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.VERTICAL_FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS = recycleGlass(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS),
		FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.FRAMED_GLASS_PANE),
		TILED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.TILED_GLASS_PANE),
		VERTICAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.VERTICAL_FRAMED_GLASS_PANE),
		HORIZONTAL_FRAMED_GLASS_PANE = recycleGlassPane(AllPaletteBlocks.HORIZONTAL_FRAMED_GLASS_PANE),

		COPPER_ORE = blastMetalOre(AllItems.COPPER_INGOT::get, AllTags.forgeItemTag("ores/copper")),
		ZINC_ORE = blastMetalOre(AllItems.ZINC_INGOT::get, AllTags.forgeItemTag("ores/zinc")),
		CRUSHED_IRON = blastCrushedMetal(() -> AliasedBlockItem.kh, AllItems.CRUSHED_IRON::get),
		CRUSHED_GOLD = blastCrushedMetal(() -> AliasedBlockItem.ki, AllItems.CRUSHED_GOLD::get),
		CRUSHED_COPPER = blastCrushedMetal(AllItems.COPPER_INGOT::get, AllItems.CRUSHED_COPPER::get),
		CRUSHED_ZINC = blastCrushedMetal(AllItems.ZINC_INGOT::get, AllItems.CRUSHED_ZINC::get),
		CRUSHED_BRASS = blastCrushedMetal(AllItems.BRASS_INGOT::get, AllItems.CRUSHED_BRASS::get),

		CRUSHED_OSMIUM = blastModdedCrushedMetal(AllItems.CRUSHED_OSMIUM, "osmium", MEK),
		CRUSHED_PLATINUM = blastModdedCrushedMetal(AllItems.CRUSHED_PLATINUM, "platinum", SM),
		CRUSHED_SILVER = blastModdedCrushedMetal(AllItems.CRUSHED_SILVER, "silver", MW, TH, IE, SM, INF),
		CRUSHED_TIN = blastModdedCrushedMetal(AllItems.CRUSHED_TIN, "tin", MEK, TH, MW, SM),
		CRUSHED_LEAD = blastModdedCrushedMetal(AllItems.CRUSHED_LEAD, "lead", MEK, MW, TH, IE, SM, EID),
		CRUSHED_QUICKSILVER = blastModdedCrushedMetal(AllItems.CRUSHED_QUICKSILVER, "quicksilver", MW),
		CRUSHED_BAUXITE = blastModdedCrushedMetal(AllItems.CRUSHED_BAUXITE, "aluminum", IE, SM),
		CRUSHED_URANIUM = blastModdedCrushedMetal(AllItems.CRUSHED_URANIUM, "uranium", MEK, IE, SM),
		CRUSHED_NICKEL = blastModdedCrushedMetal(AllItems.CRUSHED_NICKEL, "nickel", TH, IE, SM)

	;

	/*
	 * End of recipe list
	 */

	String currentFolder = "";

	Marker enterSection(AllSections section) {
		currentFolder = Lang.asId(section.name());
		return new Marker();
	}

	Marker enterFolder(String folder) {
		currentFolder = folder;
		return new Marker();
	}

	GeneratedRecipeBuilder create(Supplier<GameRules> result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	GeneratedRecipeBuilder create(Identifier result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	GeneratedRecipeBuilder create(ItemProviderEntry<? extends GameRules> result) {
		return create(result::get);
	}

	GeneratedRecipe blastCrushedMetal(Supplier<? extends GameRules> result,
		Supplier<? extends GameRules> ingredient) {
		return create(result::get).withSuffix("_from_crushed")
			.viaCooking(ingredient::get)
			.rewardXP(.1f)
			.inBlastFurnace();
	}

	GeneratedRecipe blastModdedCrushedMetal(ItemEntry<? extends HoeItem> ingredient, String metalName, Mods... mods) {
		for (Mods mod : mods) {
			Identifier ingot = mod.ingotOf(metalName);
			String modId = mod.getId();
			create(ingot).withSuffix("_compat_" + modId)
				.whenModLoaded(modId)
				.viaCooking(ingredient::get)
				.rewardXP(.1f)
				.inBlastFurnace();
		}
		return null;
	}

	GeneratedRecipe blastMetalOre(Supplier<? extends GameRules> result, RequiredTagList.e<HoeItem> ore) {
		return create(result::get).withSuffix("_from_ore")
			.viaCookingTag(() -> ore)
			.rewardXP(.1f)
			.inBlastFurnace();
	}

	GeneratedRecipe recycleGlass(BlockEntry<? extends BeetrootsBlock> ingredient) {
		return create(() -> BellBlock.ap).withSuffix("_from_" + ingredient.getId()
			.getPath())
			.viaCooking(ingredient::get)
			.forDuration(50)
			.inFurnace();
	}

	GeneratedRecipe recycleGlassPane(BlockEntry<? extends BeetrootsBlock> ingredient) {
		return create(() -> BellBlock.dJ).withSuffix("_from_" + ingredient.getId()
			.getPath())
			.viaCooking(ingredient::get)
			.forDuration(50)
			.inFurnace();
	}

	GeneratedRecipe metalCompacting(List<ItemProviderEntry<? extends GameRules>> variants,
		List<Supplier<RequiredTagList<HoeItem>>> ingredients) {
		GeneratedRecipe result = null;
		for (int i = 0; i + 1 < variants.size(); i++) {
			ItemProviderEntry<? extends GameRules> currentEntry = variants.get(i);
			ItemProviderEntry<? extends GameRules> nextEntry = variants.get(i + 1);
			Supplier<RequiredTagList<HoeItem>> currentIngredient = ingredients.get(i);
			Supplier<RequiredTagList<HoeItem>> nextIngredient = ingredients.get(i + 1);

			result = create(nextEntry).withSuffix("_from_compacting")
				.unlockedBy(currentEntry::get)
				.viaShaped(b -> b.pattern("###")
					.pattern("###")
					.pattern("###")
					.a('#', currentIngredient.get()));

			result = create(currentEntry).returns(9)
				.withSuffix("_from_decompacting")
				.unlockedBy(nextEntry::get)
				.viaShapeless(b -> b.a(nextIngredient.get()));
		}
		return result;
	}

	GeneratedRecipe conversionCycle(List<ItemProviderEntry<? extends GameRules>> cycle) {
		GeneratedRecipe result = null;
		for (int i = 0; i < cycle.size(); i++) {
			ItemProviderEntry<? extends GameRules> currentEntry = cycle.get(i);
			ItemProviderEntry<? extends GameRules> nextEntry = cycle.get((i + 1) % cycle.size());
			result = create(nextEntry).withSuffix("from_conversion")
				.unlockedBy(currentEntry::get)
				.viaShapeless(b -> b.b(currentEntry.get()));
		}
		return result;
	}

	class GeneratedRecipeBuilder {

		private String path;
		private String suffix;
		private Supplier<? extends GameRules> result;
		private Identifier compatDatagenOutput;
		List<ICondition> recipeConditions;

		private Supplier<ItemPredicate> unlockedBy;
		private int amount;

		private GeneratedRecipeBuilder(String path) {
			this.path = path;
			this.recipeConditions = new ArrayList<>();
			this.suffix = "";
			this.amount = 1;
		}

		public GeneratedRecipeBuilder(String path, Supplier<? extends GameRules> result) {
			this(path);
			this.result = result;
		}

		public GeneratedRecipeBuilder(String path, Identifier result) {
			this(path);
			this.compatDatagenOutput = result;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder unlockedBy(Supplier<? extends GameRules> item) {
			this.unlockedBy = () -> ItemPredicate.Builder.create()
				.a(item.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder unlockedByTag(Supplier<RequiredTagList<HoeItem>> tag) {
			this.unlockedBy = () -> ItemPredicate.Builder.create()
				.a(tag.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder whenModLoaded(String modid) {
			return withCondition(new ModLoadedCondition(modid));
		}

		GeneratedRecipeBuilder whenModMissing(String modid) {
			return withCondition(new NotCondition(new ModLoadedCondition(modid)));
		}

		GeneratedRecipeBuilder withCondition(ICondition condition) {
			recipeConditions.add(condition);
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeJsonFactory> builder) {
			return register(consumer -> {
				ShapedRecipeJsonFactory b = builder.apply(ShapedRecipeJsonFactory.a(result.get(), amount));
				if (unlockedBy != null)
					b.criterion("has_item", conditionsFromItemPredicates(unlockedBy.get()));
				b.offerTo(consumer, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeJsonFactory> builder) {
			return register(consumer -> {
				ShapelessRecipeJsonFactory b = builder.apply(ShapelessRecipeJsonFactory.a(result.get(), amount));
				if (unlockedBy != null)
					b.criterion("has_item", conditionsFromItemPredicates(unlockedBy.get()));
				b.offerTo(consumer, createLocation("crafting"));
			});
		}

		private Identifier createSimpleLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
		}

		private Identifier createLocation(String recipeType) {
			return Create.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix);
		}

		private Identifier getRegistryName() {
			return compatDatagenOutput == null ? result.get()
				.h()
				.getRegistryName() : compatDatagenOutput;
		}

		GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends GameRules> item) {
			return unlockedBy(item).viaCookingIngredient(() -> FireworkRocketRecipe.a(item.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingTag(Supplier<RequiredTagList<HoeItem>> tag) {
			return unlockedByTag(tag).viaCookingIngredient(() -> FireworkRocketRecipe.a(tag.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<FireworkRocketRecipe> ingredient) {
			return new GeneratedCookingRecipeBuilder(ingredient);
		}

		class GeneratedCookingRecipeBuilder {

			private Supplier<FireworkRocketRecipe> ingredient;
			private float exp;
			private int cookingTime;

			private final ShapelessRecipe<?> FURNACE = MapExtendingRecipe.p,
				SMOKER = MapExtendingRecipe.r, BLAST = MapExtendingRecipe.q,
				CAMPFIRE = MapExtendingRecipe.s;

			GeneratedCookingRecipeBuilder(Supplier<FireworkRocketRecipe> ingredient) {
				this.ingredient = ingredient;
				cookingTime = 200;
				exp = 0;
			}

			GeneratedCookingRecipeBuilder forDuration(int duration) {
				cookingTime = duration;
				return this;
			}

			GeneratedCookingRecipeBuilder rewardXP(float xp) {
				exp = xp;
				return this;
			}

			GeneratedRecipe inFurnace() {
				return inFurnace(b -> b);
			}

			GeneratedRecipe inFurnace(UnaryOperator<CookingRecipeJsonFactory> builder) {
				return create(FURNACE, builder, 1);
			}

			GeneratedRecipe inSmoker() {
				return inSmoker(b -> b);
			}

			GeneratedRecipe inSmoker(UnaryOperator<CookingRecipeJsonFactory> builder) {
				create(FURNACE, builder, 1);
				create(CAMPFIRE, builder, 3);
				return create(SMOKER, builder, .5f);
			}

			GeneratedRecipe inBlastFurnace() {
				return inBlastFurnace(b -> b);
			}

			GeneratedRecipe inBlastFurnace(UnaryOperator<CookingRecipeJsonFactory> builder) {
				create(FURNACE, builder, 1);
				return create(BLAST, builder, .5f);
			}

			private GeneratedRecipe create(ShapelessRecipe<?> serializer,
				UnaryOperator<CookingRecipeJsonFactory> builder, float cookingTimeModifier) {
				return register(consumer -> {
					boolean isOtherMod = compatDatagenOutput != null;

					CookingRecipeJsonFactory b = builder.apply(
						CookingRecipeJsonFactory.a(ingredient.get(), isOtherMod ? AliasedBlockItem.j : result.get(),
							exp, (int) (cookingTime * cookingTimeModifier), serializer));
					if (unlockedBy != null)
						b.criterion("has_item", conditionsFromItemPredicates(unlockedBy.get()));
					b.offerTo(result -> {
						consumer.accept(
							isOtherMod ? new ModdedCookingRecipeResult(result, compatDatagenOutput, recipeConditions)
								: result);
					}, createSimpleLocation(serializer.getRegistryName()
						.getPath()));
				});
			}
		}
	}

	@Override
	public String getName() {
		return "Create's Standard Recipes";
	}

	public StandardRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	private static class ModdedCookingRecipeResult implements RecipeJsonProvider {

		private RecipeJsonProvider wrapped;
		private Identifier outputOverride;
		private List<ICondition> conditions;

		public ModdedCookingRecipeResult(RecipeJsonProvider wrapped, Identifier outputOverride,
			List<ICondition> conditions) {
			this.wrapped = wrapped;
			this.outputOverride = outputOverride;
			this.conditions = conditions;
		}

		@Override
		public Identifier getRecipeId() {
			return wrapped.getRecipeId();
		}

		@Override
		public MapExtendingRecipe<?> c() {
			return wrapped.c();
		}

		@Override
		public JsonObject toAdvancementJson() {
			return wrapped.toAdvancementJson();
		}

		@Override
		public Identifier getAdvancementId() {
			return wrapped.getAdvancementId();
		}

		@Override
		public void serialize(JsonObject object) {
			wrapped.serialize(object);
			object.addProperty("result", outputOverride.toString());

			JsonArray conds = new JsonArray();
			conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			object.add("conditions", conds);
		}

	}

}
