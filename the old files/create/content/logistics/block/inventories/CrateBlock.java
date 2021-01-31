package com.simibubi.kinetic_api.content.logistics.block.inventories;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.foundation.block.ProperDirectionalBlock;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class CrateBlock extends ProperDirectionalBlock implements IWrenchable {

	public static final BedPart DOUBLE = BedPart.a("double");

	public CrateBlock(c p_i48415_1_) {
		super(p_i48415_1_);
		j(n().a(SHAPE, Direction.UP)
			.a(DOUBLE, false));
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

	@Override
	public PistonHandler a(PistonHandler stateIn, Direction facing, PistonHandler facingState, GrassColors worldIn,
		BlockPos currentPos, BlockPos facingPos) {

		boolean isDouble = stateIn.c(DOUBLE);
		Direction blockFacing = stateIn.c(SHAPE);
		boolean isFacingOther = facingState.b() == this && facingState.c(DOUBLE)
			&& facingState.c(SHAPE) == facing.getOpposite();

		if (!isDouble) {
			if (!isFacingOther)
				return stateIn;
			return stateIn.a(DOUBLE, true)
				.a(SHAPE, facing);
		}

		if (facing != blockFacing)
			return stateIn;
		if (!isFacingOther)
			return stateIn.a(DOUBLE, false);

		return stateIn;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		BlockPos pos = context.a();
		GameMode world = context.p();

		if (context.n() == null || !context.n()
			.bt()) {
			for (Direction d : Iterate.directions) {
				PistonHandler state = world.d_(pos.offset(d));
				if (state.b() == this && !state.c(DOUBLE))
					return n().a(SHAPE, d)
						.a(DOUBLE, true);
			}
		}

		Direction placedOnFace = context.j()
			.getOpposite();
		PistonHandler state = world.d_(pos.offset(placedOnFace));
		if (state.b() == this && !state.c(DOUBLE))
			return n().a(SHAPE, placedOnFace)
				.a(DOUBLE, true);
		return n();
	}

	@Override
	public PistonHandler getRotatedBlockState(PistonHandler originalState, Direction targetedFace) {
		return originalState;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(DOUBLE));
	}

}
