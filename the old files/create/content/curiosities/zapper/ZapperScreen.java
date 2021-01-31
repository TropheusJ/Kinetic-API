package com.simibubi.kinetic_api.content.curiosities.zapper;

import java.util.Vector;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import com.simibubi.kinetic_api.foundation.gui.AbstractSimiScreen;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.gui.AllIcons;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import com.simibubi.kinetic_api.foundation.gui.widgets.IconButton;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.networking.NbtPacket;
import com.simibubi.kinetic_api.foundation.utility.Lang;

public class ZapperScreen extends AbstractSimiScreen {

	protected ItemCooldownManager zapper;
	protected boolean offhand;
	protected float animationProgress;
	protected AllGuiTextures background;
	private IconButton confirmButton;
	
	protected final Text patternSection = Lang.translate("gui.blockzapper.patternSection");

	protected Text d;
	protected Vector<IconButton> patternButtons;
	protected int brightColor;
	protected int fontColor;

	public ZapperScreen(AllGuiTextures background, ItemCooldownManager zapper, boolean offhand) {
		super();
		this.background = background;
		this.zapper = zapper;
		this.offhand = offhand;
		d = LiteralText.EMPTY;
		brightColor = 0xfefefe;
		fontColor = AllGuiTextures.FONT_COLOR;
	}

	@Override
	protected void b() {
		animationProgress = 0;
		setWindowSize(background.width + 40, background.height);
		super.b();
		widgets.clear();
		
		confirmButton = new IconButton(guiLeft + background.width - 53, guiTop + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		int i = guiLeft - 20;
		int j = guiTop;
		CompoundTag nbt = zapper.p();

		patternButtons = new Vector<>(6);
		for (int row = 0; row <= 1; row++) {
			for (int col = 0; col <= 2; col++) {
				int id = patternButtons.size();
				PlacementPatterns pattern = PlacementPatterns.values()[id];
				patternButtons.add(new IconButton(i + background.width - 76 + col * 18, j + 19 + row * 18, pattern.icon));
				patternButtons.get(id)
					.setToolTip(Lang.translate("gui.blockzapper.pattern." + pattern.translationKey));
			}
		}

		if (nbt.contains("Pattern"))
			patternButtons.get(PlacementPatterns.valueOf(nbt.getString("Pattern"))
				.ordinal()).o = false;

		widgets.addAll(patternButtons);
	}

	@Override
	protected void renderWindow(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		int i = guiLeft - 20;
		int j = guiTop;

		background.draw(matrixStack, this, i, j);
		drawOnBackground(matrixStack, i, j);

		renderBlock(matrixStack);
		renderZapper(matrixStack);
	}

	protected void drawOnBackground(BufferVertexConsumer matrixStack, int i, int j) {
		o.a(matrixStack, d, i + 11, j + 3, brightColor);
	}

	@Override
	public void d() {
		super.tick();
		animationProgress += 5;
	}

	@Override
	public void e() {
		CompoundTag nbt = zapper.o();
		writeAdditionalOptions(nbt);
		AllPackets.channel.sendToServer(new NbtPacket(zapper, offhand ? ItemScatterer.b : ItemScatterer.RANDOM));
	}

	@Override
	public boolean a(double x, double y, int button) {
		CompoundTag nbt = zapper.o();

		for (IconButton patternButton : patternButtons) {
			if (patternButton.g()) {
				patternButtons.forEach(b -> b.o = true);
				patternButton.o = false;
				patternButton.a(KeyBinding.B()
					.V());
				nbt.putString("Pattern", PlacementPatterns.values()[patternButtons.indexOf(patternButton)].name());
			}
		}
		
		if (confirmButton.g()) {
			au_();
			return true;
		}

		return super.a(x, y, button);
	}

	protected void renderZapper(BufferVertexConsumer matrixStack) {
		GuiGameElement.of(zapper)
			.at((this.k - this.sWidth) / 2 + 220, this.l / 2 - this.sHeight / 4 + 30, -150)
			.scale(4)
			.render(matrixStack);
	}

	protected void renderBlock(BufferVertexConsumer matrixStack) {
		matrixStack.a();
		matrixStack.a(guiLeft + 7f, guiTop + 43.5f, 120);
		matrixStack.a(new Vector3f(.5f, .9f, -.1f).getDegreesQuaternion(-30f));
		matrixStack.a(20, 20, 20);

		PistonHandler state = BellBlock.FACING.n();
		if (zapper.n() && zapper.o()
			.contains("BlockUsed"))
			state = NbtHelper.c(zapper.o()
				.getCompound("BlockUsed"));

		GuiGameElement.of(state)
			.render(matrixStack);
		matrixStack.b();
	}

	protected void writeAdditionalOptions(CompoundTag nbt) {}

}
