package com.simibubi.create.content.logistics.block.funnel;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import ebv;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.hit.EntityHitResult;

public class FunnelRenderer extends SmartTileEntityRenderer<FunnelTileEntity> {

	public FunnelRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(FunnelTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		if (!te.hasFlap())
			return;

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		SuperByteBuffer flapBuffer = AllBlockPartials.BELT_FUNNEL_FLAP.renderOn(te.p());
		EntityHitResult pivot = VecHelper.voxelSpace(0, 10, 9.5f);
		MatrixStacker msr = MatrixStacker.of(ms);

		float horizontalAngle = AngleHelper.horizontalAngle(FunnelBlock.getFunnelFacing(te.p())
			.getOpposite());
		float f = te.flap.get(partialTicks);

		ms.a();
		msr.centre()
			.rotateY(horizontalAngle)
			.unCentre();
		ms.a(0, 0, -te.getFlapOffset());
		
		for (int segment = 0; segment <= 3; segment++) {
			ms.a();

			float intensity = segment == 3 ? 1.5f : segment + 1;
			float abs = Math.abs(f);
			float flapAngle = afj.a((float) ((1 - abs) * Math.PI * intensity)) * 30 * -f;
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
