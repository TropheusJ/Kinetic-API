package com.simibubi.kinetic_api.content.contraptions.relays.encased;

import bqx;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.kinetic_api.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EncasedCTBehaviour extends ConnectedTextureBehaviour {

	private CTSpriteShiftEntry shift;

	public EncasedCTBehaviour(CTSpriteShiftEntry shift) {
		this.shift = shift;
	}

	@Override
	public boolean connectsTo(PistonHandler state, PistonHandler other, bqx reader, BlockPos pos, BlockPos otherPos,
							  Direction face) {
		if (isBeingBlocked(state, reader, pos, otherPos, face))
			return false;
		CasingConnectivity cc = CreateClient.getCasingConnectivity();
		CasingConnectivity.Entry entry = cc.get(state);
		CasingConnectivity.Entry otherEntry = cc.get(other);
		if (entry == null || otherEntry == null)
			return false;
		if (!entry.isSideValid(state, face) || !otherEntry.isSideValid(other, face))
			return false;
		if (entry.getCasing() != otherEntry.getCasing())
			return false;
		return true;
	}

	@Override
	public CTSpriteShiftEntry get(PistonHandler state, Direction direction) {
		return shift;
	}

}
