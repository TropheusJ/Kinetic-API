package com.simibubi.create.compat.jei.category.animations;

import static com.simibubi.create.foundation.utility.AnimationTickHolder.ticks;

import afj;
import java.util.List;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraftforge.fluids.FluidStack;

public class AnimatedSpout extends AnimatedKinetics {

	private List<FluidStack> fluids;

	public AnimatedSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 100);
		matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion(-15.5f));
		matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(22.5f));
		int scale = 20;

		GuiGameElement.of(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render(matrixStack);

		float cycle = (ticks + KeyBinding.B()
			.ai()) % 30;
		float squeeze = cycle < 20 ? afj.a((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrixStack.a();

		GuiGameElement.of(AllBlockPartials.SPOUT_TOP)
			.scale(scale)
			.render(matrixStack);
		matrixStack.a(0, -3 * squeeze / 32f, 0);
		GuiGameElement.of(AllBlockPartials.SPOUT_MIDDLE)
			.scale(scale)
			.render(matrixStack);
		matrixStack.a(0, -3 * squeeze / 32f, 0);
		GuiGameElement.of(AllBlockPartials.SPOUT_BOTTOM)
			.scale(scale)
			.render(matrixStack);
		matrixStack.a(0, -3 * squeeze / 32f, 0);

		matrixStack.b();

		GuiGameElement.of(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(matrixStack);

		FogType buffer = BackgroundRenderer.a(FixedColorVertexConsumer.a()
			.c());
		matrixStack.a();
		matrixStack.a(16, -16, 16);
		float from = 2/16f;
		float to = 1f - from;
		FluidRenderer.renderTiledFluidBB(fluids.get(0), from, from, from, to, to, to, buffer, matrixStack, 0xf000f0, false);
		matrixStack.b();

		float width = 1 / 128f * squeeze;
		matrixStack.a(scale / 2f, scale * 1.5f, scale / 2f);
		matrixStack.a(16, -16, 16);
		matrixStack.a(-width / 2, 0, -width / 2);
		FluidRenderer.renderTiledFluidBB(fluids.get(0), 0, -0.001f, 0, width, 2.001f, width, buffer, matrixStack, 0xf000f0,
			false);
		buffer.method_23792();

		matrixStack.b();
	}

}
