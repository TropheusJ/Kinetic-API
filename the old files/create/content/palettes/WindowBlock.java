package com.simibubi.kinetic_api.content.palettes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.math.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WindowBlock extends ConnectedGlassBlock {

	public WindowBlock(c p_i48392_1_) {
		super(p_i48392_1_);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean a(PistonHandler state, PistonHandler adjacentBlockState, Direction side) {
		return adjacentBlockState.b() instanceof ConnectedGlassBlock
			? (!BlockBufferBuilderStorage.canRenderInLayer(state, VertexConsumerProvider.f()) && side.getAxis()
				.isHorizontal() || state.b() == adjacentBlockState.b())
			: super.a(state, adjacentBlockState, side);
	}

}
