package com.simibubi.create.foundation.block;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.DirectionHelper;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.DeadBushBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.Direction;

public class ProperDirectionalBlock extends DeadBushBlock implements IWrenchable {

	public ProperDirectionalBlock(c p_i48415_1_) {
		super(p_i48415_1_);
	}
	
	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(SHAPE);
		super.a(builder);
	}

	@Override
	public PistonHandler getRotatedBlockState(PistonHandler originalState, Direction targetedFace) {
		Direction facing = originalState.c(SHAPE);

		if (facing.getAxis() == targetedFace.getAxis())
			return originalState;

		Direction newFacing = DirectionHelper.rotateAround(facing, targetedFace.getAxis());

		return originalState.a(SHAPE, newFacing);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return n().a(SHAPE, context.d());
	}
	
	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		return state.a(SHAPE, rot.a(state.c(SHAPE)));
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirrorIn) {
		return state.a(mirrorIn.a(state.c(SHAPE)));
	}

}
