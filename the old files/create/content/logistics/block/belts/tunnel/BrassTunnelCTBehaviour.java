package com.simibubi.kinetic_api.content.logistics.block.belts.tunnel;

import bqx;
import com.simibubi.kinetic_api.AllSpriteShifts;
import com.simibubi.kinetic_api.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.kinetic_api.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BrassTunnelCTBehaviour extends ConnectedTextureBehaviour {

	@Override
	public CTSpriteShiftEntry get(PistonHandler state, Direction direction) {
		return direction == Direction.UP ? AllSpriteShifts.BRASS_TUNNEL_TOP : null;
	}

	@Override
	public boolean connectsTo(PistonHandler state, PistonHandler other, bqx reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		int yDiff = otherPos.getY() - pos.getY();
		int zDiff = otherPos.getZ() - pos.getZ();
		if (yDiff != 0)
			return false;

		BeehiveBlockEntity te = reader.c(pos);
		if (!(te instanceof BrassTunnelTileEntity))
			return false;
		BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) te;
		boolean leftSide = zDiff > 0;
		return tunnelTE.isConnected(leftSide);
	}

	@Override
	public CTContext buildContext(bqx reader, BlockPos pos, PistonHandler state, Direction face) {
		return super.buildContext(reader, pos, state, face);
	}

	@Override
	protected boolean reverseUVs(PistonHandler state, Direction face) {
		return super.reverseUVs(state, face);
	}

	@Override
	protected boolean reverseUVsHorizontally(PistonHandler state, Direction face) {
		return super.reverseUVsHorizontally(state, face);
	}

	@Override
	protected boolean reverseUVsVertically(PistonHandler state, Direction face) {
		return super.reverseUVsVertically(state, face);
	}

}
