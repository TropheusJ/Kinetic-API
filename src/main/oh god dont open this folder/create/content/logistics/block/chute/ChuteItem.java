package com.simibubi.create.content.logistics.block.chute;

import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.BannerItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class ChuteItem extends BannerItem {

	public ChuteItem(BeetrootsBlock p_i48527_1_, a p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public Difficulty a(PotionUtil context) {
		Direction face = context.j();
		BlockPos placedOnPos = context.a()
			.offset(face.getOpposite());
		GameMode world = context.p();
		PistonHandler placedOnState = world.d_(placedOnPos);

		if (!(placedOnState.b() instanceof ChuteBlock) || context.g())
			return super.a(context);
		if (face.getAxis()
			.isVertical())
			return super.a(context);

		BlockPos correctPos = context.a()
			.up();

		PistonHandler blockState = world.d_(correctPos);
		if (blockState.c()
			.e())
			context = PotionUtil.a(context, correctPos, face);
		else {
			if (blockState.b() instanceof ChuteBlock && blockState.c(ChuteBlock.FACING) == Direction.DOWN) {
				if (!world.v) {
					world.a(correctPos,
						ChuteBlock.updateDiagonalState(blockState.a(ChuteBlock.FACING, face),
							world.d_(correctPos.up()), world, correctPos));
					return Difficulty.SUCCESS;
				}
			}
			return Difficulty.FAIL;
		}

		return super.a(context);
	}

}
