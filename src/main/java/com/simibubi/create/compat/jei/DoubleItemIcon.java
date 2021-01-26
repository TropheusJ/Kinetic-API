package com.simibubi.create.compat.jei;

import java.util.function.Supplier;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.GuiGameElement;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;

public class DoubleItemIcon implements IDrawable {

	private Supplier<ItemCooldownManager> primarySupplier;
	private Supplier<ItemCooldownManager> secondarySupplier;
	private ItemCooldownManager primaryStack;
	private ItemCooldownManager secondaryStack;

	public DoubleItemIcon(Supplier<ItemCooldownManager> primary, Supplier<ItemCooldownManager> secondary) {
		this.primarySupplier = primary;
		this.secondarySupplier = secondary;
	}

	@Override
	public int getWidth() {
		return 18;
	}

	@Override
	public int getHeight() {
		return 18;
	}

	@Override
	public void draw(BufferVertexConsumer matrixStack, int xOffset, int yOffset) {
		if (primaryStack == null) {
			primaryStack = primarySupplier.get();
			secondaryStack = secondarySupplier.get();
		}
		
		GlStateManager.pushLightingAttributes();
		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.enableDepthTest();
		matrixStack.a();
		matrixStack.a(xOffset, yOffset, 0);

		matrixStack.a();
		matrixStack.a(1, 17, 0);
		GuiGameElement.of(primaryStack).render(matrixStack);
		matrixStack.b();

		matrixStack.a();
		matrixStack.a(10, 19, 100);
		matrixStack.a(.5f, .5f, .5f);
		GuiGameElement.of(secondaryStack).render(matrixStack);
		matrixStack.b();

		matrixStack.b();
		RenderSystem.enableBlend();
	}

}
