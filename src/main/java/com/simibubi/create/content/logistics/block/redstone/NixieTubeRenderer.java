package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.text.Style;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.MatrixStacker;
import ebv;

public class NixieTubeRenderer extends SafeTileEntityRenderer<NixieTubeTileEntity> {

	Random r = new Random();

	public NixieTubeRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(NixieTubeTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		ms.a();
		PistonHandler blockState = te.p();
		MatrixStacker.of(ms)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(blockState.c(NixieTubeBlock.aq)));

		float height = blockState.c(NixieTubeBlock.CEILING) ? 2 : 6;
		float scale = 1 / 20f;

		Couple<String> s = te.getVisibleText();

		ms.a();
		ms.a(-4 / 16f, 0, 0);
		ms.a(scale, -scale, scale);
		drawTube(ms, buffer, s.getFirst(), height);
		ms.b();

		ms.a();
		ms.a(4 / 16f, 0, 0);
		ms.a(scale, -scale, scale);
		drawTube(ms, buffer, s.getSecond(), height);
		ms.b();

		ms.b();
	}

	private void drawTube(BufferVertexConsumer ms, BackgroundRenderer buffer, String c, float height) {
		ItemColorProvider fontRenderer = KeyBinding.B().category;
		float charWidth = fontRenderer.b(c);
		float shadowOffset = .5f;
		float flicker = r.nextFloat();
		int brightColor = 0xFF982B;
		int darkColor = 0xE03221;
		int flickeringBrightColor = ColorHelper.mixColors(brightColor, darkColor, flicker / 4);

		ms.a();
		ms.a((charWidth - shadowOffset) / -2f, -height, 0);
		drawChar(ms, buffer, c, flickeringBrightColor);
		ms.a();
		ms.a(shadowOffset, shadowOffset, -1 / 16f);
		drawChar(ms, buffer, c, darkColor);
		ms.b();
		ms.b();

		ms.a();
		ms.a(-1, 1, 1);
		ms.a((charWidth - shadowOffset) / -2f, -height, 0);
		drawChar(ms, buffer, c, darkColor);
		ms.a();
		ms.a(-shadowOffset, shadowOffset, -1 / 16f);
		drawChar(ms, buffer, c, 0x99180F);
		ms.b();
		ms.b();
	}

	private static void drawChar(BufferVertexConsumer ms, BackgroundRenderer buffer, String c, int color) {
		ItemColorProvider fontRenderer = KeyBinding.B().category;
		fontRenderer.a(c, 0, 0, color, false, ms.c()
			.a(), buffer, false, 0, 15728880);
	}

	private static float getCharWidth(char p_211125_1_, ItemColorProvider fontRenderer) {
		return p_211125_1_ == 167 ? 0.0F : fontRenderer.a(Style.DEFAULT_FONT_ID).a(p_211125_1_).a(false);
	}
}
