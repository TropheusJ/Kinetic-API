package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;
import bnx;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.BlockHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MobSpawnerLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AttachedActorBlock extends HayBlock implements IWrenchable {

	protected AttachedActorBlock(c p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		return Difficulty.FAIL;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		Direction direction = state.c(aq);
		return AllShapes.HARVESTER_BASE.get(direction);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(aq);
		super.a(builder);
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		Direction direction = state.c(aq);
		BlockPos offset = pos.offset(direction.getOpposite());
		return BlockHelper.hasBlockSolidSide(worldIn.d_(offset), worldIn, offset, direction);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction facing;
		if (context.j().getAxis().isVertical())
			facing = context.f().getOpposite();
		else {
			PistonHandler blockState =
				context.p().d_(context.a().offset(context.j().getOpposite()));
			if (blockState.b() instanceof AttachedActorBlock)
				facing = blockState.c(aq);
			else
				facing = context.j();
		}
		return n().a(aq, facing);
	}

}
