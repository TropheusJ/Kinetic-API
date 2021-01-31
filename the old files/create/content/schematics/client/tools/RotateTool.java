package com.simibubi.kinetic_api.content.schematics.client.tools;

import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.kinetic_api.foundation.utility.outliner.LineOutline;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.timer.Timer;

public class RotateTool extends PlacementToolBase {

	private LineOutline line = new LineOutline();

	@Override
	public boolean handleMouseWheel(double delta) {
		schematicHandler.getTransformation()
			.rotate90(delta > 0);
		schematicHandler.markDirty();
		return true;
	}

	@Override
	public void renderOnSchematic(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		Timer bounds = schematicHandler.getBounds();
		double height = bounds.c() + Math.max(20, bounds.c());
		EntityHitResult center = bounds.f()
			.e(schematicHandler.getTransformation()
				.getRotationOffset(false));
		EntityHitResult start = center.a(0, height / 2, 0);
		EntityHitResult end = center.b(0, height / 2, 0);

		line.getParams()
			.disableCull()
			.disableNormals()
			.colored(0xdddddd)
			.lineWidth(1 / 16f);
		line.set(start, end)
			.render(ms, buffer);

		super.renderOnSchematic(ms, buffer);
	}

}
