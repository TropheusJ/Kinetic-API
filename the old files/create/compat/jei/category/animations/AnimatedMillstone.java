package com.simibubi.kinetic_api.compat.jei.category.animations;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import net.minecraft.client.render.BufferVertexConsumer;

public class AnimatedMillstone extends AnimatedKinetics {

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, -16, 13);
		matrixStack.a(-2, 18, 0);
		int scale = 22;

		GuiGameElement.of(AllBlockPartials.MILLSTONE_COG)
			.rotateBlock(22.5, getCurrentAngle() * 2, 0)
			.scale(scale)
			.render(matrixStack);
		
		GuiGameElement.of(AllBlocks.MILLSTONE.getDefaultState())
			.rotateBlock(22.5, 22.5, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.b();
	}

}
