package com.simibubi.kinetic_api.foundation.tileEntity.renderer;

import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public abstract class ColoredOverlayTileEntityRenderer<T extends BeehiveBlockEntity> extends SafeTileEntityRenderer<T> {

	public ColoredOverlayTileEntityRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(T te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
			int light, int overlay) {
		SuperByteBuffer render = render(te.v(), te.o(), te.p(), getOverlayBuffer(te),
				getColor(te, partialTicks));
		render.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
	}

	protected abstract int getColor(T te, float partialTicks);

	protected abstract SuperByteBuffer getOverlayBuffer(T te);

	public static SuperByteBuffer render(GameMode world, BlockPos pos, PistonHandler state, SuperByteBuffer buffer,
			int color) {
		int packedLightmapCoords = JsonGlProgram.a(world, state, pos);
		return buffer.color(color).light(packedLightmapCoords);
	}

}
