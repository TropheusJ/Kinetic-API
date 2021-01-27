package com.simibubi.create.content.schematics.client.tools;

import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;

public abstract class PlacementToolBase extends SchematicToolBase {

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	@Override
	public void renderTool(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		super.renderTool(ms, buffer);
	}

	@Override
	public void renderOverlay(BufferVertexConsumer ms, BackgroundRenderer buffer) {
		super.renderOverlay(ms, buffer);
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		return false;
	}

	@Override
	public boolean handleRightClick() {
		return false;
	}

}
