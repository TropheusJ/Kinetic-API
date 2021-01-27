package com.simibubi.create.compat.jei.category.animations;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraftforge.fluids.FluidStack;

public class AnimatedItemDrain extends AnimatedKinetics {

	private FluidStack fluid;

	public AnimatedItemDrain withFluid(FluidStack fluid) {
		this.fluid = fluid;
		return this;
	}

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 100);
		matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 20;

		GuiGameElement.of(AllBlocks.ITEM_DRAIN.getDefaultState())
			.scale(scale)
			.render(matrixStack);

		FogType buffer = BackgroundRenderer.a(FixedColorVertexConsumer.a()
			.c());
		BufferVertexConsumer ms = new BufferVertexConsumer();
		ms.a(scale, -scale, scale);
		float from = 2/16f;
		float to = 1f - from;
		FluidRenderer.renderTiledFluidBB(fluid, from, from, from, to, 3/4f, to, buffer, ms, 0xf000f0, false);
		buffer.method_23792();

		matrixStack.b();
	}
}
