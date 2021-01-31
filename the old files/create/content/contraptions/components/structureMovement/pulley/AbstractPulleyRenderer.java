package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.base.IRotate;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.GameMode;

public abstract class AbstractPulleyRenderer extends KineticTileEntityRenderer {

	private AllBlockPartials halfRope;
	private AllBlockPartials halfMagnet;

	public AbstractPulleyRenderer(ebv dispatcher, AllBlockPartials halfRope,
		AllBlockPartials halfMagnet) {
		super(dispatcher);
		this.halfRope = halfRope;
		this.halfMagnet = halfMagnet;
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		float offset = getOffset(te, partialTicks);
		boolean running = isRunning(te);

		Axis rotationAxis = ((IRotate) te.p()
			.b()).getRotationAxis(te.p());
		kineticRotationTransform(getRotatedCoil(te), te, rotationAxis, AngleHelper.rad(offset * 180), light)
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));

		GameMode world = te.v();
		PistonHandler blockState = te.p();
		BlockPos pos = te.o();

		SuperByteBuffer halfMagnet = this.halfMagnet.renderOn(blockState);
		SuperByteBuffer halfRope = this.halfRope.renderOn(blockState);
		SuperByteBuffer magnet = renderMagnet(te);
		SuperByteBuffer rope = renderRope(te);

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		if (running || offset == 0)
			renderAt(world, offset > .25f ? magnet : halfMagnet, offset, pos, ms, vb);

		float f = offset % 1;
		if (offset > .75f && (f < .25f || f > .75f))
			renderAt(world, halfRope, f > .75f ? f - 1 : f, pos, ms, vb);

		if (!running)
			return;

		for (int i = 0; i < offset - 1.25f; i++)
			renderAt(world, rope, offset - i - 1, pos, ms, vb);
	}

	private void renderAt(GrassColors world, SuperByteBuffer partial, float offset, BlockPos pulleyPos, BufferVertexConsumer ms,
		OverlayVertexConsumer buffer) {
		BlockPos actualPos = pulleyPos.down((int) offset);
		int light = JsonGlProgram.a(world, world.d_(actualPos), actualPos);
		partial.translate(0, -offset, 0)
			.light(light)
			.renderInto(ms, buffer);
	}

	protected abstract Axis getShaftAxis(KineticTileEntity te);

	protected abstract AllBlockPartials getCoil();

	protected abstract SuperByteBuffer renderRope(KineticTileEntity te);

	protected abstract SuperByteBuffer renderMagnet(KineticTileEntity te);

	protected abstract float getOffset(KineticTileEntity te, float partialTicks);

	protected abstract boolean isRunning(KineticTileEntity te);

	@Override
	protected PistonHandler getRenderedBlockState(KineticTileEntity te) {
		return shaft(getShaftAxis(te));
	}

	protected SuperByteBuffer getRotatedCoil(KineticTileEntity te) {
		PistonHandler blockState = te.p();
		return getCoil().renderOnDirectionalSouth(blockState,
			Direction.get(AxisDirection.POSITIVE, getShaftAxis(te)));
	}

}
