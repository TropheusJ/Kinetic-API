package com.simibubi.kinetic_api.content.logistics.block.belts.tunnel;

import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class BrassTunnelBlock extends BeltTunnelBlock {

	public BrassTunnelBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.BRASS_TUNNEL.create();
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction facing, PistonHandler facingState, GrassColors worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		return super.a(state, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public void a(PistonHandler p_196243_1_, GameMode p_196243_2_, BlockPos p_196243_3_, PistonHandler p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.b() != p_196243_4_.b() || !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(p_196243_2_, p_196243_3_, FilteringBehaviour.TYPE);
			withTileEntityDo(p_196243_2_, p_196243_3_, te -> {
				if (te instanceof BrassTunnelTileEntity)
					BeetrootsBlock.a(p_196243_2_, p_196243_3_, ((BrassTunnelTileEntity) te).stackToDistribute);
			});
			p_196243_2_.o(p_196243_3_);
		}
	}

}
