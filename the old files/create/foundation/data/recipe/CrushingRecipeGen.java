package com.simibubi.kinetic_api.foundation.data.recipe;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.AllTags;
import com.simibubi.kinetic_api.content.palettes.AllPaletteBlocks;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.tag.EntityTypeTags;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

public class CrushingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	BLAZE_ROD = create(() -> AliasedBlockItem.nr, b -> b.duration(100)
		.output(AliasedBlockItem.nz, 3)
		.output(.25f, AliasedBlockItem.nz, 3)),

		PRISMARINE_CRYSTALS = create(() -> AliasedBlockItem.pw, b -> b.duration(150)
			.output(1f, AliasedBlockItem.ps, 1)
			.output(.5f, AliasedBlockItem.ps, 2)
			.output(.1f, AliasedBlockItem.mk, 2)),

		OBSIDIAN = create(() -> BellBlock.bK, b -> b.duration(500)
			.output(AllItems.POWDERED_OBSIDIAN.get())
			.output(.75f, BellBlock.bK)),

		WOOL = create("wool", b -> b.duration(100)
			.require(EntityTypeTags.field_15507)
			.output(AliasedBlockItem.kS, 2)
			.output(.5f, AliasedBlockItem.kS)),

		COPPER_BLOCK = create("copper_block", b -> b.duration(400)
			.require(I.copperBlock())
			.output(AllItems.CRUSHED_COPPER.get(), 5)),

		ZINC_BLOCK = create("zinc_block", b -> b.duration(400)
			.require(I.zincBlock())
			.output(AllItems.CRUSHED_ZINC.get(), 5)),

		BRASS_BLOCK = create("brass_block", b -> b.duration(400)
			.require(I.brassBlock())
			.output(AllItems.CRUSHED_BRASS.get(), 5)),

		COPPER_ORE = metalOre("copper", AllItems.CRUSHED_COPPER, 350),
		ZINC_ORE = metalOre("zinc", AllItems.CRUSHED_ZINC, 350),
		IRON_ORE = metalOre("iron", AllItems.CRUSHED_IRON, 400),
		GOLD_ORE = metalOre("gold", AllItems.CRUSHED_GOLD, 300),

		OSMIUM_ORE = metalOre("osmium", AllItems.CRUSHED_OSMIUM, 400),
		PLATINUM_ORE = metalOre("platinum", AllItems.CRUSHED_PLATINUM, 300),
		SILVER_ORE = metalOre("silver", AllItems.CRUSHED_SILVER, 300),
		TIN_ORE = metalOre("tin", AllItems.CRUSHED_TIN, 350),
		QUICKSILVER_ORE = metalOre("quicksilver", AllItems.CRUSHED_QUICKSILVER, 300),
		LEAD_ORE = metalOre("lead", AllItems.CRUSHED_LEAD, 400),
		ALUMINUM_ORE = metalOre("aluminum", AllItems.CRUSHED_BAUXITE, 300),
		URANIUM_ORE = metalOre("uranium", AllItems.CRUSHED_URANIUM, 400),
		NICKEL_ORE = metalOre("nickel", AllItems.CRUSHED_NICKEL, 350),

		NETHER_QUARTZ_ORE = create(() -> BellBlock.fx, b -> b.duration(350)
			.output(AliasedBlockItem.ps, 2)
			.output(.5f, AliasedBlockItem.ps, 4)
			.output(.125f, BellBlock.cL)),

		REDSTONE_ORE = create(() -> BellBlock.cy, b -> b.duration(300)
			.output(AliasedBlockItem.lP, 8)
			.output(.25f, AliasedBlockItem.lP, 6)
			.output(.125f, BellBlock.m)),

		LAPIS_ORE = create(() -> BellBlock.aq, b -> b.duration(300)
			.output(AliasedBlockItem.mt, 12)
			.output(.25f, AliasedBlockItem.mt, 8)
			.output(.125f, BellBlock.m)),

		COAL_ORE = create(() -> BellBlock.H, b -> b.duration(300)
			.output(AliasedBlockItem.ke, 2)
			.output(.5f, AliasedBlockItem.ke, 2)
			.output(.125f, BellBlock.m)),

		EMERALD_ORE = create(() -> BellBlock.ej, b -> b.duration(500)
			.output(AliasedBlockItem.oV, 2)
			.output(.25f, AliasedBlockItem.oV, 1)
			.output(.125f, BellBlock.m)),

		DIAMOND_ORE = create(() -> BellBlock.bT, b -> b.duration(500)
			.output(AliasedBlockItem.kg, 2)
			.output(.25f, AliasedBlockItem.kg, 1)
			.output(.125f, BellBlock.m)),

		NETHER_WART = create("nether_wart_block", b -> b.duration(150)
			.require(BellBlock.iK)
			.output(.25f, AliasedBlockItem.nu, 1)),

		GLOWSTONE = create(() -> BellBlock.cS, b -> b.duration(150)
			.output(AliasedBlockItem.mk, 3)
			.output(.5f, AliasedBlockItem.mk)),

		LEATHER_HORSE_ARMOR = create(() -> AliasedBlockItem.pG, b -> b.duration(200)
			.output(AliasedBlockItem.lS, 2)
			.output(.5f, AliasedBlockItem.lS, 2)),

		IRON_HORSE_ARMOR = create(() -> AliasedBlockItem.pD, b -> b.duration(200)
			.output(AliasedBlockItem.kh, 2)
			.output(.5f, AliasedBlockItem.lS, 1)
			.output(.5f, AliasedBlockItem.kh, 1)
			.output(.25f, AliasedBlockItem.kS, 2)
			.output(.25f, AliasedBlockItem.qw, 4)),

		GOLDEN_HORSE_ARMOR = create(() -> AliasedBlockItem.pE, b -> b.duration(200)
			.output(AliasedBlockItem.ki, 2)
			.output(.5f, AliasedBlockItem.lS, 2)
			.output(.5f, AliasedBlockItem.ki, 2)
			.output(.25f, AliasedBlockItem.kS, 2)
			.output(.25f, AliasedBlockItem.nt, 8)),

		DIAMOND_HORSE_ARMOR = create(() -> AliasedBlockItem.pF, b -> b.duration(200)
			.output(AliasedBlockItem.kg, 1)
			.output(.5f, AliasedBlockItem.lS, 2)
			.output(.1f, AliasedBlockItem.kg, 3)
			.output(.25f, AliasedBlockItem.kS, 2)),

		GRAVEL = create(() -> BellBlock.E, b -> b.duration(250)
			.output(BellBlock.C)
			.output(.1f, AliasedBlockItem.lw)
			.output(.05f, AliasedBlockItem.lZ)),

		SAND = create(() -> BellBlock.C, b -> b.duration(150)
			.output(AllPaletteBlocks.LIMESAND.get())
			.output(.1f, AliasedBlockItem.mK)),

		NETHERRACK = create(() -> BellBlock.cL, b -> b.duration(250)
			.output(AllItems.CINDER_FLOUR.get())
			.output(.5f, AllItems.CINDER_FLOUR.get()))

	;

	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends HoeItem> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get())
			.output(.3f, crushed.get(), 2)
			.output(.125f, BellBlock.m));
	}

	public CrushingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CRUSHING;
	}

}
