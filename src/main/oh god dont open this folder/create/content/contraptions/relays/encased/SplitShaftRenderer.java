package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class SplitShaftRenderer extends KineticTileEntityRenderer {

	public SplitShaftRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
			int light, int overlay) {
		BeetrootsBlock block = te.p().b();
		final Axis boxAxis = ((IRotate) block).getRotationAxis(te.p());
		final BlockPos pos = te.o();
		float time = AnimationTickHolder.getRenderTick();

		for (Direction direction : Iterate.directions) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis)
				continue;

			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed() * 3f / 10) % 360;
			float modifier = 1;

			if (te instanceof SplitShaftTileEntity)
				modifier = ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

			angle *= modifier;
			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			SuperByteBuffer superByteBuffer =
				AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(te.p(), direction);
			kineticRotationTransform(superByteBuffer, te, axis, angle, light);
			superByteBuffer.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
		}
	}

}
