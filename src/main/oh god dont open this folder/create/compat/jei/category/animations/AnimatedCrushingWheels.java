package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction.Axis;

public class AnimatedCrushingWheels extends AnimatedKinetics {

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		RenderSystem.enableDepthTest();
		matrixStack.a(xOffset, yOffset, 100);
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(-22.5f));
		int scale = 22;
		
		PistonHandler wheel = AllBlocks.CRUSHING_WHEEL.get()
				.n()
				.a(BambooLeaves.F, Axis.X);

		GuiGameElement.of(wheel)
				.rotateBlock(0, 90, -getCurrentAngle())
				.scale(scale)
				.render(matrixStack);

		GuiGameElement.of(wheel)
				.rotateBlock(0, 90, getCurrentAngle())
				.atLocal(2, 0, 0)
				.scale(scale)
				.render(matrixStack);
	}

}
