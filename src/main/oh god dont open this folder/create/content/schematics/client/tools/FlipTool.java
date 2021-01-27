package com.simibubi.create.content.schematics.client.tools;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.timer.Timer;

public class FlipTool extends PlacementToolBase {

	private AABBOutline outline = new AABBOutline(new Timer(BlockPos.ORIGIN));

	@Override
	public void init() {
		super.init();
		renderSelectedFace = false;
	}

	@Override
	public boolean handleRightClick() {
		mirror();
		return true;
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		mirror();
		return true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	private void mirror() {
		if (schematicSelected && selectedFace.getAxis()
			.isHorizontal()) {
			schematicHandler.getTransformation()
				.flip(selectedFace.getAxis());
			schematicHandler.markDirty();
		}
	}

	@Override
	public void renderOnSchematic(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		if (!schematicSelected || !selectedFace.getAxis()
			.isHorizontal()) {
			super.renderOnSchematic(ms, buffer);
			return;
		}

		Direction facing = selectedFace.rotateYClockwise();
		Timer bounds = schematicHandler.getBounds();

		EntityHitResult directionVec = EntityHitResult.b(Direction.get(AxisDirection.POSITIVE, facing.getAxis())
			.getVector());
		EntityHitResult boundsSize = new EntityHitResult(bounds.b(), bounds.c(), bounds.d());
		EntityHitResult vec = boundsSize.h(directionVec);
		bounds = bounds.a(vec.entity, vec.c, vec.d)
			.c(1 - directionVec.entity, 1 - directionVec.c, 1 - directionVec.d);
		bounds = bounds.c(directionVec.a(.5f)
			.h(boundsSize));
		
		outline.setBounds(bounds);
		AllSpecialTextures tex = AllSpecialTextures.CHECKERED;
		outline.getParams()
			.lineWidth(1 / 16f)
			.disableNormals()
			.colored(0xdddddd)
			.withFaceTextures(tex, tex);
		outline.render(ms, buffer);
		
		super.renderOnSchematic(ms, buffer);
	}

}
