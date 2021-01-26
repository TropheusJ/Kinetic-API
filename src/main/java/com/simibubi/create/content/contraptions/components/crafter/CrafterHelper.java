package com.simibubi.create.content.contraptions.components.crafter;

import bqx;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.util.math.BlockPos;

public class CrafterHelper {

	public static MechanicalCrafterTileEntity getCrafter(bqx reader, BlockPos pos) {
		BeehiveBlockEntity te = reader.c(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return null;
		return (MechanicalCrafterTileEntity) te;
	}

	public static ConnectedInputHandler.ConnectedInput getInput(bqx reader, BlockPos pos) {
		MechanicalCrafterTileEntity crafter = getCrafter(reader, pos);
		return crafter == null ? null : crafter.input;
	}

}
