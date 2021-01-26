package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.block.enums.BambooLeaves.M;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class EncasedFanRenderer extends KineticTileEntityRenderer {

	public EncasedFanRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		Direction direction = te.p()
			.c(M);
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.d());

		int lightBehind = JsonGlProgram.a(te.v(), te.o().offset(direction.getOpposite()));
		int lightInFront = JsonGlProgram.a(te.v(), te.o().offset(direction));
		
		SuperByteBuffer shaftHalf =
			AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(te.p(), direction.getOpposite());
		SuperByteBuffer fanInner =
			AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouth(te.p(), direction.getOpposite());
		
		float time = AnimationTickHolder.getRenderTick();
		float speed = te.getSpeed() * 5;
		if (speed > 0)
			speed = afj.a(speed, 80, 64 * 20);
		if (speed < 0)
			speed = afj.a(speed, -64 * 20, -80);
		float angle = (time * speed * 3 / 10f) % 360;
		angle = angle / 180f * (float) Math.PI;

		standardKineticRotationTransform(shaftHalf, te, lightBehind).renderInto(ms, vb);
		kineticRotationTransform(fanInner, te, direction.getAxis(), angle, lightInFront).renderInto(ms, vb);
	}

}
