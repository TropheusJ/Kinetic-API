package com.simibubi.kinetic_api.foundation.data.recipe;

import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.EID;
import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.IE;
import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.INF;
import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.MEK;
import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.MW;
import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.SM;
import static com.simibubi.kinetic_api.foundation.data.recipe.Mods.TH;

import java.util.function.Supplier;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.content.palettes.AllPaletteBlocks;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.Tags;

public class WashingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	WOOL = create("wool", b -> b.require(EntityTypeTags.field_15507)
		.output(AliasedBlockItem.aR)),

		STAINED_GLASS = create("stained_glass", b -> b.require(Tags.Items.STAINED_GLASS)
			.output(AliasedBlockItem.az)),
		STAINED_GLASS_PANE = create("stained_glass_pane", b -> b.require(Tags.Items.STAINED_GLASS_PANES)
			.output(AliasedBlockItem.dP)),

		GRAVEL = create(() -> BellBlock.E, b -> b.output(.25f, AliasedBlockItem.lw)
			.output(.125f, AliasedBlockItem.qw)),
		SOUL_SAND = create(() -> BellBlock.cM, b -> b.output(.125f, AliasedBlockItem.ps, 4)
			.output(.02f, AliasedBlockItem.nt)),
		RED_SAND = create(() -> BellBlock.D, b -> b.output(.125f, AliasedBlockItem.nt, 3)
			.output(.05f, AliasedBlockItem.aN)),
		SAND = create(() -> BellBlock.C, b -> b.output(.25f, AliasedBlockItem.lZ)),

		CRUSHED_COPPER = crushedOre(AllItems.CRUSHED_COPPER, AllItems.COPPER_NUGGET::get),
		CRUSHED_ZINC = crushedOre(AllItems.CRUSHED_ZINC, AllItems.ZINC_NUGGET::get),
		CRUSHED_BRASS = crushedOre(AllItems.CRUSHED_BRASS, AllItems.BRASS_NUGGET::get),
		CRUSHED_GOLD = crushedOre(AllItems.CRUSHED_GOLD, () -> AliasedBlockItem.nt),
		CRUSHED_IRON = crushedOre(AllItems.CRUSHED_IRON, () -> AliasedBlockItem.qw),

		CRUSHED_OSMIUM = moddedCrushedOre(AllItems.CRUSHED_OSMIUM, "osmium", MEK),
		CRUSHED_PLATINUM = moddedCrushedOre(AllItems.CRUSHED_PLATINUM, "platinum", SM),
		CRUSHED_SILVER = moddedCrushedOre(AllItems.CRUSHED_SILVER, "silver", TH, MW, IE, SM, INF),
		CRUSHED_TIN = moddedCrushedOre(AllItems.CRUSHED_TIN, "tin", TH, MEK, MW, SM),
		CRUSHED_LEAD = moddedCrushedOre(AllItems.CRUSHED_LEAD, "lead", MEK, TH, MW, IE, SM, EID),
		CRUSHED_QUICKSILVER = moddedCrushedOre(AllItems.CRUSHED_QUICKSILVER, "quicksilver", MW),
		CRUSHED_BAUXITE = moddedCrushedOre(AllItems.CRUSHED_BAUXITE, "aluminum", IE, SM),
		CRUSHED_URANIUM = moddedCrushedOre(AllItems.CRUSHED_URANIUM, "uranium", MEK, IE, SM),
		CRUSHED_NICKEL = moddedCrushedOre(AllItems.CRUSHED_NICKEL, "nickel", TH, IE, SM),

		ICE = convert(BellBlock.cD, BellBlock.gT), MAGMA_BLOCK = convert(BellBlock.iJ, BellBlock.bK),

		WHITE_CONCRETE = convert(BellBlock.jM, BellBlock.jw),
		ORANGE_CONCRETE = convert(BellBlock.jN, BellBlock.jx),
		MAGENTA_CONCRETE = convert(BellBlock.jO, BellBlock.jy),
		LIGHT_BLUE_CONCRETE = convert(BellBlock.jP, BellBlock.jz),
		LIME_CONCRETE = convert(BellBlock.jR, BellBlock.jB),
		YELLOW_CONCRETE = convert(BellBlock.jQ, BellBlock.jA),
		PINK_CONCRETE = convert(BellBlock.jS, BellBlock.jC),
		LIGHT_GRAY_CONCRETE = convert(BellBlock.jU, BellBlock.jE),
		GRAY_CONCRETE = convert(BellBlock.jT, BellBlock.jD),
		PURPLE_CONCRETE = convert(BellBlock.jW, BellBlock.jG),
		GREEN_CONCRETE = convert(BellBlock.jZ, BellBlock.jJ),
		BROWN_CONCRETE = convert(BellBlock.jY, BellBlock.jI),
		RED_CONCRETE = convert(BellBlock.ka, BellBlock.jK),
		BLUE_CONCRETE = convert(BellBlock.jX, BellBlock.jH),
		CYAN_CONCRETE = convert(BellBlock.jV, BellBlock.jF),
		BLACK_CONCRETE = convert(BellBlock.kb, BellBlock.jL),

		LIMESTONE = create(AllPaletteBlocks.LIMESTONE::get, b -> b.output(AllPaletteBlocks.WEATHERED_LIMESTONE.get())),
		FLOUR = create(AllItems.WHEAT_FLOUR::get, b -> b.output(AllItems.DOUGH.get()))

	;

	public GeneratedRecipe convert(BeetrootsBlock block, BeetrootsBlock result) {
		return create(() -> block, b -> b.output(result));
	}

	public GeneratedRecipe crushedOre(ItemEntry<HoeItem> crushed, Supplier<GameRules> nugget) {
		return create(crushed::get, b -> b.output(nugget.get(), 10)
			.output(.5f, nugget.get(), 5));
	}

	public GeneratedRecipe moddedCrushedOre(ItemEntry<? extends HoeItem> crushed, String metalName, Mods... mods) {
		for (Mods mod : mods) {
			Identifier nugget = mod.nuggetOf(metalName);
			create(mod.getId() + "/" + crushed.getId()
				.getPath(),
				b -> b.withItemIngredients(FireworkRocketRecipe.a(crushed::get))
					.output(1, nugget, 10)
					.output(.5f, nugget, 5)
					.whenModLoaded(mod.getId()));
		}
		return null;
	}

	public WashingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SPLASHING;
	}

}
