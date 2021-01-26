package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.data.DataGenerator;

public class CuttingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	ANDESITE_ALLOY = create(I::andesite, b -> b.duration(200)
		.output(AllBlocks.SHAFT.get(), 6)),

		OAK_WOOD = stripAndMakePlanks(BellBlock.V, BellBlock.ab, BellBlock.n),
		SPRUCE_WOOD = stripAndMakePlanks(BellBlock.W, BellBlock.ac, BellBlock.EAST_WALL_SHAPE),
		BIRCH_WOOD = stripAndMakePlanks(BellBlock.X, BellBlock.ad, BellBlock.NORTH_WALL_SHAPE),
		JUNGLE_WOOD = stripAndMakePlanks(BellBlock.Y, BellBlock.ae, BellBlock.SOUTH_WALL_SHAPE),
		ACACIA_WOOD = stripAndMakePlanks(BellBlock.Z, BellBlock.af, BellBlock.HANGING_SHAPE),
		DARK_OAK_WOOD = stripAndMakePlanks(BellBlock.aa, BellBlock.ag, BellBlock.s),
		CRIMSON_WOOD = stripAndMakePlanks(BellBlock.ms, BellBlock.mt, BellBlock.mC),
		WARPED_WOOD = stripAndMakePlanks(BellBlock.mj, BellBlock.mk, BellBlock.mD),

		OAK_LOG = stripAndMakePlanks(BellBlock.J, BellBlock.U, BellBlock.n),
		SPRUCE_LOG = stripAndMakePlanks(BellBlock.K, BellBlock.P, BellBlock.EAST_WALL_SHAPE),
		BIRCH_LOG = stripAndMakePlanks(BellBlock.L, BellBlock.Q, BellBlock.NORTH_WALL_SHAPE),
		JUNGLE_LOG = stripAndMakePlanks(BellBlock.M, BellBlock.R, BellBlock.SOUTH_WALL_SHAPE),
		ACACIA_LOG = stripAndMakePlanks(BellBlock.N, BellBlock.S, BellBlock.HANGING_SHAPE),
		DARK_OAK_LOG = stripAndMakePlanks(BellBlock.O, BellBlock.T, BellBlock.s),
		CRIMSON_LOG = stripAndMakePlanks(BellBlock.mq, BellBlock.mr, BellBlock.mC),
		WARPED_LOG = stripAndMakePlanks(BellBlock.mh, BellBlock.mi, BellBlock.mD)

	;

	GeneratedRecipe stripAndMakePlanks(BeetrootsBlock wood, BeetrootsBlock stripped, BeetrootsBlock planks) {
		create(() -> wood, b -> b.duration(50)
			.output(stripped));
		return create(() -> stripped, b -> b.duration(100)
			.output(planks, 5));
	}

	public CuttingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CUTTING;
	}

}
