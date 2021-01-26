package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.text.LiteralText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public abstract class AbstractSimiScreen extends PresetsScreen {

	protected int sWidth, sHeight;
	protected int guiLeft, guiTop;
	protected List<OptionSliderWidget> widgets;

	protected AbstractSimiScreen() {
		super(new LiteralText(""));
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		sWidth = width;
		sHeight = height;
		guiLeft = (this.k - sWidth) / 2;
		guiTop = (this.l - sHeight) / 2;
	}

	@Override
	public void a(BufferVertexConsumer ms, int mouseX, int mouseY, float partialTicks) {
		a(ms);
		renderWindow(ms, mouseX, mouseY, partialTicks);
		for (OptionSliderWidget widget : widgets)
			widget.a(ms, mouseX, mouseY, partialTicks);
		renderWindowForeground(ms, mouseX, mouseY, partialTicks);
		for (OptionSliderWidget widget : widgets)
			widget.a(ms, mouseX, mouseY);
	}

	@Override
	public boolean a(double x, double y, int button) {
		boolean result = false;
		for (OptionSliderWidget widget : widgets) {
			if (widget.a(x, y, button))
				result = true;
		}
		return result;
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

	protected void renderWindowForeground(BufferVertexConsumer ms, int mouseX, int mouseY, float partialTicks) {
		for (OptionSliderWidget widget : widgets) {
			if (!widget.g())
				continue;
			
			if (widget instanceof AbstractSimiWidget && !((AbstractSimiWidget) widget).getToolTip().isEmpty()) {
				b(ms, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);
			}
		}
	}

}
