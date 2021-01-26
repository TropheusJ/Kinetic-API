package com.simibubi.create.content.contraptions.relays.gearbox;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class GearboxRenderer extends KineticTileEntityRenderer {

	public GearboxRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
			int light, int overlay) {
		final Axis boxAxis = te.p().c(BambooLeaves.F);
		final BlockPos pos = te.o();
		float time = AnimationTickHolder.getRenderTick();

		for (Direction direction : Iterate.directions) {
			final Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			SuperByteBuffer shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(te.p(), direction);
			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed() * 3f / 10) % 360;

			if (te.getSpeed() != 0 && te.hasSource()) {
				BlockPos source = te.source.subtract(te.o());
				Direction sourceFacing = Direction.getFacing(source.getX(), source.getY(), source.getZ());
				if (sourceFacing.getAxis() == direction.getAxis())
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getDirection() == direction.getDirection())
					angle *= -1;
			}

			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			kineticRotationTransform(shaft, te, axis, angle, light);
			shaft.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
		}
	}

}
