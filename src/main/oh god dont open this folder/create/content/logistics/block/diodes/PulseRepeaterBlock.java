package com.simibubi.create.content.logistics.block.diodes;

import java.util.Random;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class PulseRepeaterBlock extends AbstractDiodeBlock {

	public static BedPart PULSING = BedPart.a("pulsing");

	public PulseRepeaterBlock(c properties) {
		super(properties);
		j(n().a(PULSING, false).a(SHAPE, false));
	}

	@Override
	protected int g(PistonHandler state) {
		return 1;
	}
	
	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.c(aq).getAxis();
	}
	
	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean powered = state.c(SHAPE);
		boolean pulsing = state.c(PULSING);
		boolean shouldPower = a(worldIn, pos, state);

		if (pulsing) {
			worldIn.a(pos, state.a(SHAPE, shouldPower).a(PULSING, false), 2);
		} else if (powered && !shouldPower) {
			worldIn.a(pos, state.a(SHAPE, false).a(PULSING, false), 2);
		} else if (!powered) {
			worldIn.a(pos, state.a(SHAPE, true).a(PULSING, true), 2);
			worldIn.j().a(pos, this, this.g(state), StructureAccessor.c);
		}

	}

	@Override
	protected int b(MobSpawnerLogic worldIn, BlockPos pos, PistonHandler state) {
		return state.c(PULSING) ? 15 : 0;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(aq, SHAPE, PULSING);
		super.a(builder);
	}

}
