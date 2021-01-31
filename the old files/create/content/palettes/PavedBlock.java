package com.simibubi.kinetic_api.content.palettes;

import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PavedBlock extends BeetrootsBlock {

	public static final BedPart COVERED = BedPart.a("covered");

	public PavedBlock(c properties) {
		super(properties);
		j(n().a(COVERED, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(COVERED));
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return n().a(COVERED, context.p()
				.d_(context.a().up())
				.b() == this);
	}

	@Override
	public PistonHandler a(PistonHandler stateIn, Direction face, PistonHandler neighbour, GrassColors worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (face == Direction.UP)
			return stateIn.a(COVERED, worldIn.d_(facingPos).b() == this);
		return stateIn;
	}

}
