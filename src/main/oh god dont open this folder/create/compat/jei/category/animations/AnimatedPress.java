package com.simibubi.create.compat.jei.category.animations;

import static com.simibubi.create.foundation.utility.AnimationTickHolder.ticks;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction.Axis;

public class AnimatedPress extends AnimatedKinetics {

	private boolean basin;

	public AnimatedPress(boolean basin) {
		this.basin = basin;
	}

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 100);
		matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = basin ? 20 : 24;

		GuiGameElement.of(shaft(Axis.Z))
				.rotateBlock(0, 0, getCurrentAngle())
				.scale(scale)
				.render(matrixStack);

		GuiGameElement.of(AllBlocks.MECHANICAL_PRESS.getDefaultState())
				.scale(scale)
				.render(matrixStack);

		GuiGameElement.of(AllBlockPartials.MECHANICAL_PRESS_HEAD)
				.atLocal(0, -getAnimatedHeadOffset(), 0)
				.scale(scale)
				.render(matrixStack);

		if (basin)
			GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
					.atLocal(0, 1.65, 0)
					.scale(scale)
					.render(matrixStack);

		matrixStack.b();
	}

	private float getAnimatedHeadOffset() {
		float cycle = (ticks + KeyBinding.B()
				.ai()) % 30;
		if (cycle < 10) {
			float progress = cycle / 10;
			return -(progress * progress * progress);
		}
		if (cycle < 15)
			return -1;
		if (cycle < 20)
			return -1 + (1 - ((20 - cycle) / 5));
		return 0;
	}

}
