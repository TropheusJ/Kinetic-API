package com.simibubi.kinetic_api.content.logistics.block.belts.tunnel;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class BeltTunnelRenderer extends SmartTileEntityRenderer<BeltTunnelTileEntity> {

	public BeltTunnelRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BeltTunnelTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		SuperByteBuffer flapBuffer = AllBlockPartials.BELT_TUNNEL_FLAP.renderOn(te.p());
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		EntityHitResult pivot = VecHelper.voxelSpace(0, 10, 1f);
		MatrixStacker msr = MatrixStacker.of(ms);

		for (Direction direction : Iterate.directions) {
			if (!te.flaps.containsKey(direction))
				continue;

			float horizontalAngle = AngleHelper.horizontalAngle(direction.getOpposite());
			float f = te.flaps.get(direction)
				.get(partialTicks);

			ms.a();
			msr.centre()
				.rotateY(horizontalAngle)
				.unCentre();

			for (int segment = 0; segment <= 3; segment++) {
				ms.a();
				float intensity = segment == 3 ? 1.5f : segment + 1;
				float abs = Math.abs(f);
				float flapAngle = afj.a((float) ((1 - abs) * Math.PI * intensity)) * 30 * f
					* (direction.getAxis() == Axis.X ? 1 : -1);
				if (f > 0)
					flapAngle *= .5f;

				msr.translate(pivot)
					.rotateX(flapAngle)
					.translateBack(pivot);
				flapBuffer.light(light)
					.renderInto(ms, vb);

				ms.b();
				ms.a(-3 / 16f, 0, 0);
			}
			ms.b();
		}

	}

}
