package com.simibubi.kinetic_api.content.contraptions.components.mixer;

import com.simibubi.kinetic_api.AllBlocks;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.BannerItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;

public class BasinOperatorBlockItem extends BannerItem {

	public BasinOperatorBlockItem(BeetrootsBlock block, a builder) {
		super(block, builder);
	}

	@Override
	public Difficulty a(PotionUtil context) {
		BlockPos placedOnPos = context.a()
			.offset(context.j()
				.getOpposite());
		PistonHandler placedOnState = context.p()
			.d_(placedOnPos);
		if (AllBlocks.BASIN.has(placedOnState) || AllBlocks.BELT.has(placedOnState)
			|| AllBlocks.DEPOT.has(placedOnState)) {
			if (context.p()
				.d_(placedOnPos.up(2))
				.c()
				.e())
				context = PotionUtil.a(context, placedOnPos.up(2), Direction.UP);
			else
				return Difficulty.FAIL;
		}

		return super.a(context);
	}

}