package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.tag.EntityTypeTags;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

public class MillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	GRANITE = create(() -> BellBlock.POWERED, b -> b.duration(200)
		.output(BellBlock.D)),

		WOOL = create("wool", b -> b.duration(100)
			.require(EntityTypeTags.field_15507)
			.output(AliasedBlockItem.kS)),

		CLAY = create(() -> BellBlock.cG, b -> b.duration(50)
			.output(AliasedBlockItem.lZ, 3)
			.output(.5f, AliasedBlockItem.lZ)),

		TERRACOTTA = create(() -> BellBlock.gR, b -> b.duration(200)
			.output(BellBlock.D)),
		ANDESITE = create(() -> BellBlock.BELL_LIP_SHAPE, b -> b.duration(200)
			.output(BellBlock.m)),
		COBBLESTONE = create(() -> BellBlock.m, b -> b.duration(250)
			.output(BellBlock.E)),
		GRAVEL = create(() -> BellBlock.E, b -> b.duration(250)
			.output(AliasedBlockItem.lw)),
		SAND = create(() -> BellBlock.C, b -> b.duration(150)
			.output(AllPaletteBlocks.LIMESAND.get())),
		SANDSTONE = create(() -> BellBlock.at, b -> b.duration(150)
			.output(BellBlock.C)),
		DIORITE = create(() -> BellBlock.EAST_WEST_SHAPE, b -> b.duration(200)
			.output(AllPaletteBlocks.LIMESAND.get())),

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

		WHEAT = create(() -> AliasedBlockItem.kW, b -> b.duration(150)
			.output(AllItems.WHEAT_FLOUR.get())
			.output(.25f, AllItems.WHEAT_FLOUR.get(), 2)
			.output(.25f, AliasedBlockItem.kV)),

		BONE = create(() -> AliasedBlockItem.mL, b -> b.duration(100)
			.output(AliasedBlockItem.mK, 3)
			.output(.25f, AliasedBlockItem.mu, 1)
			.output(.25f, AliasedBlockItem.mK, 3)),

		CACTUS = create(() -> BellBlock.cF, b -> b.duration(50)
			.output(AliasedBlockItem.mH, 2)
			.output(.1f, AliasedBlockItem.mH, 1)
			.whenModMissing("quark")),

		BONE_MEAL = create(() -> AliasedBlockItem.mK, b -> b.duration(70)
			.output(AliasedBlockItem.mu, 2)
			.output(.1f, AliasedBlockItem.mC, 1)),

		COCOA_BEANS = create(() -> AliasedBlockItem.ms, b -> b.duration(70)
			.output(AliasedBlockItem.mG, 2)
			.output(.1f, AliasedBlockItem.mG, 1)),

		SADDLE = create(() -> AliasedBlockItem.lO, b -> b.duration(200)
			.output(AliasedBlockItem.lS, 2)
			.output(.5f, AliasedBlockItem.lS, 2)),

		SUGAR_CANE = create(() -> AliasedBlockItem.bD, b -> b.duration(50)
			.output(AliasedBlockItem.mM, 2)
			.output(.1f, AliasedBlockItem.mM)),

		INK_SAC = create(() -> AliasedBlockItem.mr, b -> b.duration(100)
			.output(AliasedBlockItem.mJ, 2)
			.output(.1f, AliasedBlockItem.mB)),

		CHARCOAL = create(() -> AliasedBlockItem.kf, b -> b.duration(100)
			.output(AliasedBlockItem.mJ, 1)
			.output(.1f, AliasedBlockItem.mB, 2)),

		COAL = create(() -> AliasedBlockItem.ke, b -> b.duration(100)
			.output(AliasedBlockItem.mJ, 2)
			.output(.1f, AliasedBlockItem.mB, 1)),

		LAPIS_LAZULI = create(() -> AliasedBlockItem.mt, b -> b.duration(100)
			.output(AliasedBlockItem.mF, 2)
			.output(.1f, AliasedBlockItem.mF)),

		AZURE_BLUET = create(() -> BellBlock.bt, b -> b.duration(50)
			.output(AliasedBlockItem.mC, 2)
			.output(.1f, AliasedBlockItem.mu, 2)),

		BLUE_ORCHID = create(() -> BellBlock.br, b -> b.duration(50)
			.output(AliasedBlockItem.mx, 2)
			.output(.05f, AliasedBlockItem.mC, 1)),

		FERN = create(() -> BellBlock.aS, b -> b.duration(50)
			.output(AliasedBlockItem.mH)
			.output(.1f, AliasedBlockItem.kV)),

		LARGE_FERN = create(() -> BellBlock.gZ, b -> b.duration(50)
			.output(AliasedBlockItem.mH, 2)
			.output(.5f, AliasedBlockItem.mH)
			.output(.1f, AliasedBlockItem.kV)),

		LILAC = create(() -> BellBlock.gV, b -> b.duration(100)
			.output(AliasedBlockItem.mw, 3)
			.output(.25f, AliasedBlockItem.mw)
			.output(.25f, AliasedBlockItem.mE)),

		PEONY = create(() -> BellBlock.gX, b -> b.duration(100)
			.output(AliasedBlockItem.mA, 3)
			.output(.25f, AliasedBlockItem.mw)
			.output(.25f, AliasedBlockItem.mA)),

		ALLIUM = create(() -> BellBlock.bs, b -> b.duration(50)
			.output(AliasedBlockItem.mw, 2)
			.output(.1f, AliasedBlockItem.mE, 2)
			.output(.1f, AliasedBlockItem.mA)),

		LILY_OF_THE_VALLEY = create(() -> BellBlock.bB, b -> b.duration(50)
			.output(AliasedBlockItem.mu, 2)
			.output(.1f, AliasedBlockItem.mz)
			.output(.1f, AliasedBlockItem.mu)),

		ROSE_BUSH = create(() -> BellBlock.gW, b -> b.duration(50)
			.output(AliasedBlockItem.mI, 3)
			.output(.05f, AliasedBlockItem.mH, 2)
			.output(.25f, AliasedBlockItem.mI, 2)),

		SUNFLOWER = create(() -> BellBlock.gU, b -> b.duration(100)
			.output(AliasedBlockItem.my, 3)
			.output(.25f, AliasedBlockItem.mv)
			.output(.25f, AliasedBlockItem.my)),

		OXEYE_DAISY = create(() -> BellBlock.by, b -> b.duration(50)
			.output(AliasedBlockItem.mC, 2)
			.output(.2f, AliasedBlockItem.mu)
			.output(.05f, AliasedBlockItem.my)),

		POPPY = create(() -> BellBlock.bq, b -> b.duration(50)
			.output(AliasedBlockItem.mI, 2)
			.output(.05f, AliasedBlockItem.mH)),

		DANDELION = create(() -> BellBlock.bp, b -> b.duration(50)
			.output(AliasedBlockItem.my, 2)
			.output(.05f, AliasedBlockItem.my)),

		CORNFLOWER = create(() -> BellBlock.bz, b -> b.duration(50)
			.output(AliasedBlockItem.mF, 2)),

		WITHER_ROSE = create(() -> BellBlock.bA, b -> b.duration(50)
			.output(AliasedBlockItem.mJ, 2)
			.output(.1f, AliasedBlockItem.mJ)),

		ORANGE_TULIP = create(() -> BellBlock.bv, b -> b.duration(50)
			.output(AliasedBlockItem.mv, 2)
			.output(.1f, AliasedBlockItem.mz)),

		RED_TULIP = create(() -> BellBlock.bu, b -> b.duration(50)
			.output(AliasedBlockItem.mI, 2)
			.output(.1f, AliasedBlockItem.mz)),

		WHITE_TULIP = create(() -> BellBlock.bw, b -> b.duration(50)
			.output(AliasedBlockItem.mu, 2)
			.output(.1f, AliasedBlockItem.mz)),

		PINK_TULIP = create(() -> BellBlock.bx, b -> b.duration(50)
			.output(AliasedBlockItem.mA, 2)
			.output(.1f, AliasedBlockItem.mz)),

		TALL_GRASS = create(() -> BellBlock.gY, b -> b.duration(100)
			.output(.5f, AliasedBlockItem.kV)),
		GRASS = create(() -> BellBlock.aR, b -> b.duration(50)
			.output(.25f, AliasedBlockItem.kV))

	;

	protected GeneratedRecipe metalOre(String name, ItemEntry<? extends HoeItem> crushed, int duration) {
		return create(name + "_ore", b -> b.duration(duration)
			.withCondition(new NotCondition(new TagEmptyCondition("forge", "ores/" + name)))
			.require(AllTags.forgeItemTag("ores/" + name))
			.output(crushed.get()));
	}

	public MillingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MILLING;
	}

}
