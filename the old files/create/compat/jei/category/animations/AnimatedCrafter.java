package com.simibubi.kinetic_api.compat.jei.category.animations;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import net.minecraft.client.render.BufferVertexConsumer;

public class AnimatedCrafter extends AnimatedKinetics {

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, -16, 13);

		matrixStack.a(3, 16, 0);
		MatrixStacker.of(matrixStack)
			.rotateX(-12.5f)
			.rotateY(-22.5f);
		int scale = 22;

		GuiGameElement.of(cogwheel())
			.rotateBlock(90, 0, getCurrentAngle())
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_CRAFTER.getDefaultState())
			.rotateBlock(0, 180, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.b();
	}

}
