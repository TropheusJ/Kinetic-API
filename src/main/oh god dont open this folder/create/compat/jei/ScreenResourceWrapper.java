package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import dkt;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.render.BufferVertexConsumer;

public class ScreenResourceWrapper implements IDrawable {

	private AllGuiTextures resource;

	public ScreenResourceWrapper(AllGuiTextures resource) {
		this.resource = resource;
	}

	@Override
	public int getWidth() {
		return resource.width;
	}

	@Override
	public int getHeight() {
		return resource.height;
	}

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		resource.bind();
		dkt.a(matrixStack, xOffset, yOffset, 0, resource.startX, resource.startY, resource.width, resource.height, 256,
				256);
	}

}
