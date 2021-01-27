package com.simibubi.create.content.contraptions.fluids;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;

public class PumpRenderer extends KineticTileEntityRenderer {

	public PumpRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (!(te instanceof PumpTileEntity))
			return;
		PumpTileEntity pump = (PumpTileEntity) te;
		EntityHitResult rotationOffset = new EntityHitResult(.5, 14 / 16f, .5);
		PistonHandler blockState = te.p();
		float angle = afj.g(pump.arrowDirection.getValue(partialTicks), 0, 90) - 90;
		for (float yRot : new float[] { 0, 90 }) {
			ms.a();
			SuperByteBuffer arrow = AllBlockPartials.MECHANICAL_PUMP_ARROW.renderOn(blockState);
			Direction direction = blockState.c(PumpBlock.FACING);
			MatrixStacker.of(ms)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(direction) + 180)
				.rotateX(-AngleHelper.verticalAngle(direction) - 90)
				.unCentre()
				.translate(rotationOffset)
				.rotateY(yRot)
				.rotateZ(angle)
				.translateBack(rotationOffset);
			arrow.light(light).renderInto(ms, buffer.getBuffer(VertexConsumerProvider.c()));
			ms.b();
		}
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.MECHANICAL_PUMP_COG.renderOnDirectionalSouth(te.p());
	}

}
