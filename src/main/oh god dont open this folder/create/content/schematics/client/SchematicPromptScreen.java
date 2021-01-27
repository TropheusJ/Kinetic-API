package com.simibubi.create.content.schematics.client;

import org.lwjgl.glfw.GLFW;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class SchematicPromptScreen extends AbstractSimiScreen {

	private final Text title = Lang.translate("schematicAndQuill.title");
	private final Text convertLabel = Lang.translate("schematicAndQuill.convert");
	private final Text abortLabel = Lang.translate("action.discard");
	private final Text confirmLabel = Lang.translate("action.saveToFile");

	private ChatMessages nameField;
	private IconButton confirm;
	private IconButton abort;
	private IconButton convert;

	@Override
	public void b() {
		super.b();
		AllGuiTextures background = AllGuiTextures.SCHEMATIC_PROMPT;
		setWindowSize(background.width, background.height + 30);

		nameField = new ChatMessages(o, guiLeft + 49, guiTop + 26, 131, 10, LiteralText.EMPTY);
		nameField.l(-1);
		nameField.m(-1);
		nameField.f(false);
		nameField.k(35);
		nameField.c_(true);

		abort = new IconButton(guiLeft + 7, guiTop + 53, AllIcons.I_TRASH);
		abort.setToolTip(abortLabel);
		widgets.add(abort);

		confirm = new IconButton(guiLeft + 158, guiTop + 53, AllIcons.I_CONFIRM);
		confirm.setToolTip(confirmLabel);
		widgets.add(confirm);

		convert = new IconButton(guiLeft + 180, guiTop + 53, AllIcons.I_SCHEMATIC);
		convert.setToolTip(convertLabel);
		widgets.add(convert);

		widgets.add(confirm);
		widgets.add(convert);
		widgets.add(abort);
		widgets.add(nameField);
	}

	@Override
	protected void renderWindow(BufferVertexConsumer ms, int mouseX, int mouseY, float partialTicks) {
		AllGuiTextures.SCHEMATIC_PROMPT.draw(ms, this, guiLeft, guiTop);
		o.a(ms, d, guiLeft + (sWidth / 2) - (o.a(d) / 2), guiTop + 3,
			0xffffff);
		ms.a();
		ms.a(guiLeft + 22, guiTop + 39, 0);
		GuiGameElement.of(AllItems.SCHEMATIC.asStack()).render(ms);
		ms.b();
	}

	@Override
	public boolean a(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			confirm(false);
			return true;
		}
		if (keyCode == 256 && this.at_()) {
			this.au_();
			return true;
		}
		return nameField.a(keyCode, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean a(double x, double y, int button) {
		if (confirm.g()) {
			confirm(false);
			return true;
		}
		if (abort.g()) {
			CreateClient.schematicAndQuillHandler.discard();
			KeyBinding.B().s.m();
			return true;
		}
		if (convert.g()) {
			confirm(true);
			return true;
		}
		return super.a(x, y, button);
	}

	private void confirm(boolean convertImmediately) {
		CreateClient.schematicAndQuillHandler.saveSchematic(nameField.b(), convertImmediately);
		KeyBinding.B().s.m();
	}

}
