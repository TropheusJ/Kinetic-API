package com.simibubi.create.content.schematics.client.tools;

import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;

public interface ISchematicTool {

	public void init();
	public void updateSelection();
	
	public boolean handleRightClick();
	public boolean handleMouseWheel(double delta);
	
	public void renderTool(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer);
	public void renderOverlay(BufferVertexConsumer ms, BackgroundRenderer buffer);
	public void renderOnSchematic(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer);
	
}
