package com.simibubi.create.content.contraptions.components.flywheel.engine;

import javax.annotation.Nullable;
import bnx;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.Iterate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class EngineBlock extends HayBlock implements IWrenchable {

	protected EngineBlock(c builder) {
		super(builder);
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		return isValidPosition(state, worldIn, pos, state.c(aq));
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}
	
	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		return Difficulty.FAIL;
	}
	
	@Override
	public abstract BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world);

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction facing = context.j();
		return n().a(aq,
				facing.getAxis().isVertical() ? context.f().getOpposite() : facing);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(aq));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.v)
			return;

		if (fromPos.equals(getBaseBlockPos(state, pos))) {
			if (!a(state, worldIn, pos)) {
				worldIn.b(pos, true);
				return;
			}
		}
	}

	private boolean isValidPosition(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction facing) {
		BlockPos baseBlockPos = getBaseBlockPos(state, pos);
		if (!isValidBaseBlock(world.d_(baseBlockPos), world, pos))
			return false;
		for (Direction otherFacing : Iterate.horizontalDirections) {
			if (otherFacing == facing)
				continue;
			BlockPos otherPos = baseBlockPos.offset(otherFacing);
			PistonHandler otherState = world.d_(otherPos);
			if (otherState.b() instanceof EngineBlock
					&& getBaseBlockPos(otherState, otherPos).equals(baseBlockPos))
				return false;
		}

		return true;
	}
	
	public static BlockPos getBaseBlockPos(PistonHandler state, BlockPos pos) {
		return pos.offset(state.c(aq).getOpposite());
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public abstract AllBlockPartials getFrameModel();

	protected abstract boolean isValidBaseBlock(PistonHandler baseBlock, MobSpawnerLogic world, BlockPos pos);

}
