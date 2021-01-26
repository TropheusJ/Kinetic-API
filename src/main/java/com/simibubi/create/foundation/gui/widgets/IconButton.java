package com.simibubi.create.foundation.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.text.Text;

public class IconButton extends AbstractSimiWidget {

	private AllIcons icon;
	protected boolean pressed;

	public IconButton(int x, int y, AllIcons icon) {
		super(x, y, 18, 18);
		this.icon = icon;
	}

	@Override
	public void b(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.p) {
			this.n =
				mouseX >= this.l && mouseY >= this.m && mouseX < this.l + this.j && mouseY < this.m + this.k;

			AllGuiTextures button = (pressed || !o) ? button = AllGuiTextures.BUTTON_DOWN
				: (n) ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			AllGuiTextures.BUTTON.bind();
			b(matrixStack, l, m, button.startX, button.startY, button.width, button.height);
			icon.draw(matrixStack, this, l + 1, m + 1);
		}
	}

	@Override
	public void a(double p_onClick_1_, double p_onClick_3_) {
		super.a(p_onClick_1_, p_onClick_3_);
		this.pressed = true;
	}

	@Override
	public void a_(double p_onRelease_1_, double p_onRelease_3_) {
		super.a_(p_onRelease_1_, p_onRelease_3_);
		this.pressed = false;
	}

	public void setToolTip(Text text) {
		toolTip.clear();
		toolTip.add(text);
	}

}
