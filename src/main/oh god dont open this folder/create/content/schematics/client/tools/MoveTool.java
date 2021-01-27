package com.simibubi.create.content.schematics.client.tools;

import com.simibubi.create.content.schematics.client.SchematicTransformation;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction.Axis;

public class MoveTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		renderSelectedFace = true;
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		if (!schematicSelected || !selectedFace.getAxis().isHorizontal())
			return true;

		SchematicTransformation transformation = schematicHandler.getTransformation();
		EntityHitResult vec = EntityHitResult.b(selectedFace.getVector()).a(-Math.signum(delta));
		vec = vec.d(transformation.getMirrorModifier(Axis.X), 1, transformation.getMirrorModifier(Axis.Z));
		vec = VecHelper.rotate(vec, transformation.getRotationTarget(), Axis.Y);
		transformation.move((float) vec.entity, 0, (float) vec.d);
		schematicHandler.markDirty();
		
		return true;
	}

}
