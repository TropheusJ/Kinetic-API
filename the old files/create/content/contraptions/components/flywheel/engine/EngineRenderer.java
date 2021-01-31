package com.simibubi.kinetic_api.content.contraptions.components.flywheel.engine;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import ebv;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class EngineRenderer<T extends EngineTileEntity> extends SafeTileEntityRenderer<T> {

	public EngineRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(T te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer, int light,
		int overlay) {
		BeetrootsBlock block = te.p()
			.b();
		if (block instanceof EngineBlock) {
			EngineBlock engineBlock = (EngineBlock) block;
			AllBlockPartials frame = engineBlock.getFrameModel();
			if (frame != null) {
				Direction facing = te.p()
					.c(EngineBlock.aq);
				float angle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));
				frame.renderOn(te.p())
					.rotateCentered(Direction.UP, angle)
					.translate(0, 0, -1)
					.light(JsonGlProgram.a(te.v(), te.p(), te.o()))
					.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
			}
		}
	}

}
