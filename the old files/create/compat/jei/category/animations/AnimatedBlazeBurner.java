package com.simibubi.kinetic_api.compat.jei.category.animations;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;

public class AnimatedBlazeBurner implements IDrawable {

	private HeatLevel heatLevel;

	public AnimatedBlazeBurner withHeat(HeatLevel heatLevel) {
		this.heatLevel = heatLevel;
		return this;
	}

	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 200);
		matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 23;

		GuiGameElement.of(AllBlocks.BLAZE_BURNER.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render(matrixStack);

		AllBlockPartials blaze = AllBlockPartials.BLAZES.get(heatLevel);
		GuiGameElement.of(blaze)
			.atLocal(1, 1.65, 1)
			.rotate(0, 180, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.b();
	}

	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 50;
	}
}
