package com.simibubi.create.content.palettes;

import bqx;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ForcedChunkState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public class StandardFoliageColorHandler implements RenderTickCounter {

	@Override
	public int getColor(PistonHandler state, bqx light, BlockPos pos, int layer) {
		return pos != null && light != null ? AbstractClientPlayerEntity.a(light, pos) : ForcedChunkState.a(0.5D, 1.0D);
	}

}
