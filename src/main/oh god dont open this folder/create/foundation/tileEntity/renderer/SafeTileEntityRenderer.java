package com.simibubi.create.foundation.tileEntity.renderer;

import ebv;
import ebw;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;

public abstract class SafeTileEntityRenderer<T extends BeehiveBlockEntity> extends ebw<T> {

	public SafeTileEntityRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	public final void a(T te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer, int light,
		int overlay) {
		if (isInvalid(te))
			return;
		renderSafe(te, partialTicks, ms, buffer, light, overlay);
	}

	protected abstract void renderSafe(T te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer, int light,
		int overlay);

	public boolean isInvalid(T te) {
		return !te.n() || te.p()
			.b() == BellBlock.FACING;
	}
}
