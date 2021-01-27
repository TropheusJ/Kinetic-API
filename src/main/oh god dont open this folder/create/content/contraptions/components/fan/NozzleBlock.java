package com.simibubi.create.content.contraptions.components.fan;

import bnx;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NozzleBlock extends ProperDirectionalBlock {

	public NozzleBlock(c p_i48415_1_) {
		super(p_i48415_1_);
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
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.NOZZLE.create();
	}
	
	@Override
	public PistonHandler a(PotionUtil context) {
		return n().a(SHAPE, context.j());
	}
	
	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.NOZZLE.get(state.c(SHAPE));
	}
	
	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.v)
			return;

		if (fromPos.equals(pos.offset(state.c(SHAPE).getOpposite())))
			if (!a(state, worldIn, pos)) {
				worldIn.b(pos, true);
				return;
			}
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		Direction towardsFan = state.c(SHAPE).getOpposite();
		BeehiveBlockEntity te = worldIn.c(pos.offset(towardsFan));
		return te instanceof IAirCurrentSource
				&& ((IAirCurrentSource) te).getAirflowOriginSide() == towardsFan.getOpposite();
	}

}
