package com.simibubi.create.foundation.gui;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.schematics.client.tools.Tools;
import com.simibubi.create.foundation.utility.Lang;
import dew;

public class ToolSelectionScreen extends PresetsScreen {

	public final String scrollToCycle = Lang.translate("gui.toolmenu.cycle")
		.getString();
	public final String holdToFocus = "gui.toolmenu.focusKey";

	protected List<Tools> tools;
	protected Consumer<Tools> callback;
	public boolean focused;
	private float yOffset;
	protected int selection;
	private boolean initialized;

	protected int w;
	protected int h;

	public ToolSelectionScreen(List<Tools> tools, Consumer<Tools> callback) {
		super(new LiteralText("Tool Selection"));
		this.i = KeyBinding.B();
		this.tools = tools;
		this.callback = callback;
		focused = false;
		yOffset = 0;
		selection = 0;
		initialized = false;

		callback.accept(tools.get(selection));

		w = Math.max(tools.size() * 50 + 30, 220);
		h = 30;
	}

	public void setSelectedElement(Tools tool) {
		if (!tools.contains(tool))
			return;
		selection = tools.indexOf(tool);
	}

	public void cycle(int direction) {
		selection += (direction < 0) ? 1 : -1;
		selection = (selection + tools.size()) % tools.size();
	}

	private void draw(BufferVertexConsumer matrixStack, float partialTicks) {
		KeyBinding mc = KeyBinding.B();
		dew mainWindow = mc.aB();
		if (!initialized)
			b(mc, mainWindow.o(), mainWindow.p());

		int x = (mainWindow.o() - w) / 2 + 15;
		int y = mainWindow.p() - h - 75;

		matrixStack.a();
		matrixStack.a(0, -yOffset, focused ? 100 : 0);

		AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;
		RenderSystem.enableBlend();
		RenderSystem.color4f(1, 1, 1, focused ? 7 / 8f : 1 / 2f);

		KeyBinding.B()
			.L()
			.a(gray.location);
		a(matrixStack, x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);

		float toolTipAlpha = yOffset / 10;
		List<Text> toolTip = tools.get(selection)
			.getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

		if (toolTipAlpha > 0.25f) {
			RenderSystem.color4f(.7f, .7f, .8f, toolTipAlpha);
			a(matrixStack, x - 15, y + 33, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
			RenderSystem.color4f(1, 1, 1, 1);

			if (toolTip.size() > 0)
				o.b(matrixStack, toolTip.get(0), x - 10, y + 38, 0xEEEEEE + stringAlphaComponent);
			if (toolTip.size() > 1)
				o.b(matrixStack, toolTip.get(1), x - 10, y + 50, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 2)
				o.b(matrixStack, toolTip.get(2), x - 10, y + 60, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 3)
				o.b(matrixStack, toolTip.get(3), x - 10, y + 72, 0xCCCCDD + stringAlphaComponent);
		}

		RenderSystem.color4f(1, 1, 1, 1);
		if (tools.size() > 1) {
			String keyName = AllKeys.TOOL_MENU.getBoundKey();
			int width = i.aB()
				.o();
			if (!focused)
				a(matrixStack, i.category, Lang.translate(holdToFocus, keyName), width / 2,
					y - 10, 0xCCDDFF);
			else
				a(matrixStack, i.category, scrollToCycle, width / 2, y - 10, 0xCCDDFF);
		} else {
			x += 65;
		}

		for (int i = 0; i < tools.size(); i++) {
			matrixStack.a();

			float alpha = focused ? 1 : .2f;
			if (i == selection) {
				matrixStack.a(0, -10, 0);
				a(matrixStack, i.category, tools.get(i)
					.getDisplayName()
					.getString(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			RenderSystem.color4f(0, 0, 0, alpha);
			tools.get(i)
				.getIcon()
				.draw(matrixStack, this, x + i * 50 + 16, y + 12);
			RenderSystem.color4f(1, 1, 1, alpha);
			tools.get(i)
				.getIcon()
				.draw(matrixStack, this, x + i * 50 + 16, y + 11);

			matrixStack.b();
		}

		matrixStack.b();
	}

	public void update() {
		if (focused)
			yOffset += (10 - yOffset) * .1f;
		else
			yOffset *= .9f;
	}

	public void renderPassive(BufferVertexConsumer matrixStack, float partialTicks) {
		draw(matrixStack, partialTicks);
	}

	@Override
	public void au_() {
		callback.accept(tools.get(selection));
	}

	@Override
	protected void b() {
		super.b();
		initialized = true;
	}
}
