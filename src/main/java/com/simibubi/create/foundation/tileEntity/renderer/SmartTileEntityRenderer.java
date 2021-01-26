package com.simibubi.create.foundation.tileEntity.renderer;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;

public class SmartTileEntityRenderer<T extends SmartTileEntity> extends SafeTileEntityRenderer<T> {

	public SmartTileEntityRenderer(ebv dispatcher) {
		super(dispatcher);
	}
	
	@Override
	protected void renderSafe(T tileEntityIn, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer, int light,
			int overlay) {
		FilteringRenderer.renderOnTileEntity(tileEntityIn, partialTicks, ms, buffer, light, overlay);
		LinkRenderer.renderOnTileEntity(tileEntityIn, partialTicks, ms, buffer, light, overlay);
	}

}
