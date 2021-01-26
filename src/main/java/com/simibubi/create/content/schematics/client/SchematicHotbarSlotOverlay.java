package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dew;
import dkt;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;

public class SchematicHotbarSlotOverlay extends dkt {
	
	public void renderOn(BufferVertexConsumer matrixStack, int slot) {
		dew mainWindow = KeyBinding.B().aB();
		int x = mainWindow.o() / 2 - 88;
		int y = mainWindow.p() - 19;
		RenderSystem.enableAlphaTest();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		AllGuiTextures.SCHEMATIC_SLOT.draw(matrixStack, this, x + 20 * slot, y);
		RenderSystem.disableAlphaTest();
	}

}
