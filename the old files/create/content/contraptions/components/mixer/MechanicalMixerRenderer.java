package com.simibubi.kinetic_api.content.contraptions.components.mixer;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MechanicalMixerRenderer extends KineticTileEntityRenderer {

	public MechanicalMixerRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		PistonHandler blockState = te.p();
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;
		BlockPos pos = te.o();

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());

		SuperByteBuffer superBuffer = AllBlockPartials.SHAFTLESS_COGWHEEL.renderOn(blockState);
		standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb);

		int packedLightmapCoords = JsonGlProgram.a(te.v(), blockState, pos);
		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTick();
		float angle = (float) (((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI);

		SuperByteBuffer poleRender = AllBlockPartials.MECHANICAL_MIXER_POLE.renderOn(blockState);
		poleRender.translate(0, -renderedHeadOffset, 0)
			.light(packedLightmapCoords)
			.renderInto(ms, vb);

		SuperByteBuffer headRender = AllBlockPartials.MECHANICAL_MIXER_HEAD.renderOn(blockState);
		headRender.rotateCentered(Direction.UP, angle)
			.translate(0, -renderedHeadOffset, 0)
			.light(packedLightmapCoords)
			.renderInto(ms, vb);
	}

}
