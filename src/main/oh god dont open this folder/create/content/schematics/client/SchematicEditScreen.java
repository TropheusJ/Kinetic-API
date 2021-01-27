package com.simibubi.create.content.schematics.client;

import java.util.Collections;
import java.util.List;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SchematicEditScreen extends AbstractSimiScreen {

	private ChatMessages xInput;
	private ChatMessages yInput;
	private ChatMessages zInput;
	private IconButton confirmButton;

	private final List<Text> rotationOptions =
		Lang.translatedOptions("schematic.rotation", "none", "cw90", "cw180", "cw270");
	private final List<Text> mirrorOptions =
		Lang.translatedOptions("schematic.mirror", "none", "leftRight", "frontBack");
	private final Text rotationLabel = Lang.translate("schematic.rotation");
	private final Text mirrorLabel = Lang.translate("schematic.mirror");

	private ScrollInput rotationArea;
	private ScrollInput mirrorArea;
	private SchematicHandler handler;

	@Override
	protected void b() {
		AllGuiTextures background = AllGuiTextures.SCHEMATIC;
		setWindowSize(background.width + 50, background.height);
		int x = guiLeft;
		int y = guiTop;
		handler = CreateClient.schematicHandler;

		xInput = new ChatMessages(o, x + 50, y + 26, 34, 10, LiteralText.EMPTY);
		yInput = new ChatMessages(o, x + 90, y + 26, 34, 10, LiteralText.EMPTY);
		zInput = new ChatMessages(o, x + 130, y + 26, 34, 10, LiteralText.EMPTY);

		BlockPos anchor = handler.getTransformation()
			.getAnchor();
		if (handler.isDeployed()) {
			xInput.a("" + anchor.getX());
			yInput.a("" + anchor.getY());
			zInput.a("" + anchor.getZ());
		} else {
			BlockPos alt = i.s.cA();
			xInput.a("" + alt.getX());
			yInput.a("" + alt.getY());
			zInput.a("" + alt.getZ());
		}

		for (ChatMessages widget : new ChatMessages[] { xInput, yInput, zInput }) {
			widget.k(6);
			widget.f(false);
			widget.l(0xFFFFFF);
			widget.c_(false);
			widget.a(0, 0, 0);
			widget.a(s -> {
				if (s.isEmpty() || s.equals("-"))
					return true;
				try {
					Integer.parseInt(s);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			});
		}

		RuleTest settings = handler.getTransformation()
			.toSettings();
		Label labelR = new Label(x + 50, y + 48, LiteralText.EMPTY).withShadow();
		rotationArea = new SelectionScrollInput(x + 45, y + 43, 118, 18).forOptions(rotationOptions)
			.titled(rotationLabel.copy())
			.setState(settings.d()
				.ordinal())
			.writingTo(labelR);

		Label labelM = new Label(x + 50, y + 70, LiteralText.EMPTY).withShadow();
		mirrorArea = new SelectionScrollInput(x + 45, y + 65, 118, 18).forOptions(mirrorOptions)
			.titled(mirrorLabel.copy())
			.setState(settings.c()
				.ordinal())
			.writingTo(labelM);

		Collections.addAll(widgets, xInput, yInput, zInput);
		Collections.addAll(widgets, labelR, labelM, rotationArea, mirrorArea);

		confirmButton =
			new IconButton(guiLeft + background.width - 33, guiTop + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		super.b();
	}

	@Override
	public boolean a(int code, int p_keyPressed_2_, int p_keyPressed_3_) {

		if (g(code)) {
			String coords = i.m.a();
			if (coords != null && !coords.isEmpty()) {
				coords.replaceAll(" ", "");
				String[] split = coords.split(",");
				if (split.length == 3) {
					boolean valid = true;
					for (String s : split) {
						try {
							Integer.parseInt(s);
						} catch (NumberFormatException e) {
							valid = false;
						}
					}
					if (valid) {
						xInput.a(split[0]);
						yInput.a(split[1]);
						zInput.a(split[2]);
						return true;
					}
				}
			}
		}

		return super.a(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	protected void renderWindow(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;
		AllGuiTextures.SCHEMATIC.draw(matrixStack, this, x, y);
		o.a(matrixStack, handler.getCurrentSchematicName(),
			x + 93 - o.b(handler.getCurrentSchematicName()) / 2, y + 3, 0xffffff);

		matrixStack.a();
		matrixStack.a(guiLeft + 200, guiTop + 130, 0);
		matrixStack.a(3, 3, 3);
		GuiGameElement.of(AllItems.SCHEMATIC.asStack())
			.render(matrixStack);
		matrixStack.b();
	}

	@Override
	public void e() {
		boolean validCoords = true;
		BlockPos newLocation = null;
		try {
			newLocation = new BlockPos(Integer.parseInt(xInput.b()), Integer.parseInt(yInput.b()),
				Integer.parseInt(zInput.b()));
		} catch (NumberFormatException e) {
			validCoords = false;
		}

		RuleTest settings = new RuleTest();
		settings.a(RespawnAnchorBlock.values()[rotationArea.getState()]);
		settings.a(LoomBlock.values()[mirrorArea.getState()]);

		if (validCoords && newLocation != null) {
			ItemCooldownManager item = handler.getActiveSchematicItem();
			if (item != null) {
				item.o()
					.putBoolean("Deployed", true);
				item.o()
					.put("Anchor", NbtHelper.fromBlockPos(newLocation));
			}

			handler.getTransformation()
				.init(newLocation, settings, handler.getBounds());
			handler.markDirty();
			handler.deploy();
		}
	}

	@Override
	public boolean a(double x, double y, int button) {
		if (confirmButton.g()) {
			au_();
			return true;
		}

		return super.a(x, y, button);
	}

}
