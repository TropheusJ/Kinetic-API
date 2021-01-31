package com.simibubi.kinetic_api.compat.jei.category.animations;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;

public class AnimatedMixer extends AnimatedKinetics {

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 200);
		matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 23;

		GuiGameElement.of(cogwheel())
			.rotateBlock(0, getCurrentAngle() * 2, 0)
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_MIXER.getDefaultState())
			.atLocal(0, 0, 0)
			.scale(scale)
			.render(matrixStack);

		float animation = ((afj.a(AnimationTickHolder.getRenderTick() / 32f) + 1) / 5) + .5f;

		GuiGameElement.of(AllBlockPartials.MECHANICAL_MIXER_POLE)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlockPartials.MECHANICAL_MIXER_HEAD)
			.rotateBlock(0, getCurrentAngle() * 4, 0)
			.atLocal(0, animation, 0)
			.scale(scale)
			.render(matrixStack);

		GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
			.atLocal(0, 1.65, 0)
			.scale(scale)
			.render(matrixStack);

		matrixStack.b();
	}

}
