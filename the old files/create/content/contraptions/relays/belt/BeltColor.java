package com.simibubi.kinetic_api.content.contraptions.relays.belt;

import bqx;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public class BeltColor implements RenderTickCounter {

	@Override
	public int getColor(PistonHandler state, bqx reader, BlockPos pos, int layer) {
		if (reader == null)
			return 0;
		BeehiveBlockEntity tileEntity = reader.c(pos);
		if (tileEntity instanceof BeltTileEntity) {
			BeltTileEntity te = (BeltTileEntity) tileEntity;
			if (te.color != -1)
				return te.color;
		}
		return 0;
	}

}