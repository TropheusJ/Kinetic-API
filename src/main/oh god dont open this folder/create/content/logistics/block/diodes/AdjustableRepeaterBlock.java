package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerLogic;

public class AdjustableRepeaterBlock extends AbstractDiodeBlock {

	public static BedPart POWERING = BedPart.a("powering");

	public AdjustableRepeaterBlock(c properties) {
		super(properties);
		j(n().a(SHAPE, false)
			.a(POWERING, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(SHAPE, POWERING, aq);
		super.a(builder);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllBlocks.ADJUSTABLE_REPEATER.is(this) ? AllTileEntities.ADJUSTABLE_REPEATER.create()
			: AllTileEntities.ADJUSTABLE_PULSE_REPEATER.create();
	}

	@Override
	protected int b(MobSpawnerLogic worldIn, BlockPos pos, PistonHandler state) {
		return state.c(POWERING) ? 15 : 0;
	}

	@Override
	public int a(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		return blockState.c(aq) == side ? this.b(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int g(PistonHandler p_196346_1_) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.c(aq)
			.getAxis();
	}

}
