package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import bfs;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import dpm;
import mcp.MethodsReturnNonnullByDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.FoodComponent;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractSimiContainerScreen<T extends FoodComponent> extends dpm<T> {

	protected List<OptionSliderWidget> widgets;

	public AbstractSimiContainerScreen(T container, bfs inv, Text title) {
		super(container, inv, title);
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		this.b = width;
		this.c = height;
	}

	@Override
	protected void b(BufferVertexConsumer p_230451_1_, int p_230451_2_, int p_230451_3_) {
	}

	@Override
	public void a(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		a(matrixStack);
		renderWindow(matrixStack, mouseX, mouseY, partialTicks);
		
		for (OptionSliderWidget widget : widgets)
			widget.a(matrixStack, mouseX, mouseY, partialTicks);
		
		super.a(matrixStack, mouseX, mouseY, partialTicks);
		
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.disableRescaleNormal();
		GlStateManager.pushTextureAttributes();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
		for (OptionSliderWidget widget : widgets)
			widget.a(matrixStack, mouseX, mouseY);
	}

	@Override
	public boolean a(double x, double y, int button) {
		boolean result = false;
		for (OptionSliderWidget widget : widgets) {
			if (widget.a(x, y, button))
				result = true;
		}
		return result || super.a(x, y, button);
	}

	@Override
	public boolean a(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (OptionSliderWidget widget : widgets) {
			if (widget.a(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;
		}
		return super.a(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean a(char character, int code) {
		for (OptionSliderWidget widget : widgets) {
			if (widget.a(character, code))
				return true;
		}
		if (character == 'e')
			au_();
		return super.a(character, code);
	}

	@Override
	public boolean a(double mouseX, double mouseY, double delta) {
		for (OptionSliderWidget widget : widgets) {
			if (widget.a(mouseX, mouseY, delta))
				return true;
		}
		return super.a(mouseX, mouseY, delta);
	}
	
	@Override
	public boolean c(double x, double y, int button) {
		boolean result = false;
		for (OptionSliderWidget widget : widgets) {
			if (widget.c(x, y, button))
				result = true;
		}
		return result | super.c(x, y, button);
	}

	@Override
	public boolean at_() {
		return true;
	}

	@Override
	public boolean ay_() {
		return false;
	}

	protected abstract void renderWindow(BufferVertexConsumer ms, int mouseX, int mouseY, float partialTicks);

	@Override
	protected void a(BufferVertexConsumer p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {

	}

	protected void renderWindowForeground(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		a(matrixStack, mouseX, mouseY);
		for (OptionSliderWidget widget : widgets) {
			if (!widget.g())
				continue;

			if (widget instanceof AbstractSimiWidget && !((AbstractSimiWidget) widget).getToolTip().isEmpty()) {
				b(matrixStack, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);
			}
		}
	}
	
	protected void renderItemOverlayIntoGUI(BufferVertexConsumer matrixStack, ItemColorProvider fr, ItemCooldownManager stack, int xPosition, int yPosition,
			@Nullable String text, int textColor) {
		if (!stack.a()) {
			if (stack.b().showDurabilityBar(stack)) {
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableAlphaTest();
				RenderSystem.disableBlend();
				FixedColorVertexConsumer tessellator = FixedColorVertexConsumer.a();
				GlShader bufferbuilder = tessellator.c();
				double health = stack.b().getDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				int j = stack.b().getRGBDurabilityForDisplay(stack);
				this.draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
				this.draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255,
						255);
				RenderSystem.enableBlend();
				RenderSystem.enableAlphaTest();
				RenderSystem.enableTexture();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
			}

			if (stack.E() != 1 || text != null) {
				String s = text == null ? String.valueOf(stack.E()) : text;
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableBlend();
				matrixStack.a();

				int guiScaleFactor = (int) i.aB().s();
				matrixStack.a(xPosition + 16.5f, yPosition + 16.5f, 0);
				double scale = getItemCountTextScale();

				matrixStack.a((float) scale, (float) scale, 0);
				matrixStack.a(-fr.b(s) - (guiScaleFactor > 1 ? 0 : -.5f),
						-o.a + (guiScaleFactor > 1 ? 1 : 1.75f), 0);
				fr.a(matrixStack, s, 0, 0, textColor);

				matrixStack.b();
				RenderSystem.enableBlend();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
				RenderSystem.enableBlend();
			}
		}
	}

	public double getItemCountTextScale() {
		int guiScaleFactor = (int) i.aB().s();
		double scale = 1;
		switch (guiScaleFactor) {
		case 1:
			scale = 2060 / 2048d;
			break;
		case 2:
			scale = .5;
			break;
		case 3:
			scale = .675;
			break;
		case 4:
			scale = .75;
			break;
		default:
			scale = ((float) guiScaleFactor - 1) / guiScaleFactor;
		}
		return scale;
	}

	private void draw(GlShader renderer, int x, int y, int width, int height, int red, int green, int blue,
			int alpha) {
		renderer.a(7, BufferBuilder.elementOffset);
		renderer.a((double) (x + 0), (double) (y + 0), 0.0D).a(red, green, blue, alpha).d();
		renderer.a((double) (x + 0), (double) (y + height), 0.0D).a(red, green, blue, alpha).d();
		renderer.a((double) (x + width), (double) (y + height), 0.0D).a(red, green, blue, alpha).d();
		renderer.a((double) (x + width), (double) (y + 0), 0.0D).a(red, green, blue, alpha).d();
		FixedColorVertexConsumer.a().b();
	}

	/**
	 * Used for moving JEI out of the way of extra things like Flexcrate renders
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link ContainerScreen}.
	 */
	public List<ItemModels> getExtraAreas() {
		return Collections.emptyList();
	}
}
