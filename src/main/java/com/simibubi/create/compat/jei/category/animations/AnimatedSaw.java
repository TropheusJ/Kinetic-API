package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class AnimatedSaw extends AnimatedKinetics {

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.draw(matrixStack, -16, 13);

		matrixStack.a(0, 0, 200);
		matrixStack.a(29, 17, 0);
		matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion(-22.5f));
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(90 - 225f));
		int scale = 25;

		GuiGameElement.of(shaft(Axis.X))
			.rotateBlock(-getCurrentAngle(), 0, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_SAW.getDefaultState()
			.a(SawBlock.FACING, Direction.UP))
			.rotateBlock(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlockPartials.SAW_BLADE_VERTICAL_ACTIVE)
			.rotateBlock(0, -90, -90)
			.scale(scale)
			.render(matrixStack);

		matrixStack.b();
	}

}
