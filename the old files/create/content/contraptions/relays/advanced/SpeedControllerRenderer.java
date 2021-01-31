package com.simibubi.kinetic_api.content.contraptions.relays.advanced;

import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public class SpeedControllerRenderer extends SmartTileEntityRenderer<SpeedControllerTileEntity> {

	public SpeedControllerRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(SpeedControllerTileEntity tileEntityIn, float partialTicks, BufferVertexConsumer ms,
			BackgroundRenderer buffer, int light, int overlay) {
		super.renderSafe(tileEntityIn, partialTicks, ms, buffer, light, overlay);

		KineticTileEntityRenderer.renderRotatingBuffer(tileEntityIn, getRotatedModel(tileEntityIn), ms,
				buffer.getBuffer(VertexConsumerProvider.c()), light);
	}

	private SuperByteBuffer getRotatedModel(SpeedControllerTileEntity te) {
		return CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE,
				KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te)));
	}

}
