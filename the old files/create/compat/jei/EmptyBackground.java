package com.simibubi.kinetic_api.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.render.BufferVertexConsumer;

public class EmptyBackground implements IDrawable {

	private int width;
	private int height;

	public EmptyBackground(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
	}

}
