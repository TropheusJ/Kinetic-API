package com.simibubi.kinetic_api.content.schematics.block;

import static com.simibubi.kinetic_api.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE;
import static com.simibubi.kinetic_api.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE_PROGRESS;

import afj;
import bfs;
import java.nio.file.Paths;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.schematics.ClientSchematicLoader;
import com.simibubi.kinetic_api.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.gui.AllIcons;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import com.simibubi.kinetic_api.foundation.gui.widgets.IconButton;
import com.simibubi.kinetic_api.foundation.gui.widgets.Label;
import com.simibubi.kinetic_api.foundation.gui.widgets.ScrollInput;
import com.simibubi.kinetic_api.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class SchematicTableScreen extends AbstractSimiContainerScreen<SchematicTableContainer>
	implements JigsawBlockScreen<SchematicTableContainer> {

	private ScrollInput schematicsArea;
	private IconButton confirmButton;
	private IconButton folderButton;
	private IconButton refreshButton;
	private Label schematicsLabel;

	private final Text title = Lang.translate("gui.schematicTable.title");
	private final Text uploading = Lang.translate("gui.schematicTable.uploading");
	private final Text finished = Lang.translate("gui.schematicTable.finished");
	private final Text refresh = Lang.translate("gui.schematicTable.refresh");
	private final Text folder = Lang.translate("gui.schematicTable.open_folder");
	private final Text noSchematics = Lang.translate("gui.schematicTable.noSchematics");
	private final Text availableSchematicsTitle = Lang.translate("gui.schematicTable.availableSchematics");
	private final ItemCooldownManager renderedItem = AllBlocks.SCHEMATIC_TABLE.asStack();

	private float progress;
	private float chasingProgress;
	private float lastChasingProgress;

	public SchematicTableScreen(SchematicTableContainer container, bfs playerInventory,
		Text title) {
		super(container, playerInventory, title);
	}

	@Override
	protected void b() {
		setWindowSize(SCHEMATIC_TABLE.width, SCHEMATIC_TABLE.height + 50);
		super.b();
		widgets.clear();

		int mainLeft = w - 56;
		int mainTop = x - 16;

		CreateClient.schematicSender.refresh();
		List<Text> availableSchematics = CreateClient.schematicSender.getAvailableSchematics();

		schematicsLabel = new Label(mainLeft + 49, mainTop + 26, LiteralText.EMPTY).withShadow();
		schematicsLabel.text = LiteralText.EMPTY;
		if (!availableSchematics.isEmpty()) {
			schematicsArea =
				new SelectionScrollInput(mainLeft + 45, mainTop + 21, 139, 18).forOptions(availableSchematics)
					.titled(availableSchematicsTitle.copy())
					.writingTo(schematicsLabel);
			widgets.add(schematicsArea);
			widgets.add(schematicsLabel);
		}

		confirmButton = new IconButton(mainLeft + 44, mainTop + 56, AllIcons.I_CONFIRM);

		folderButton = new IconButton(mainLeft + 21, mainTop + 21, AllIcons.I_OPEN_FOLDER);
		folderButton.setToolTip(folder);
		refreshButton = new IconButton(mainLeft + 207, mainTop + 21, AllIcons.I_REFRESH);
		refreshButton.setToolTip(refresh);

		widgets.add(confirmButton);
		widgets.add(folderButton);
		widgets.add(refreshButton);
	}

	@Override
	protected void renderWindow(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {

		int x = w + 20;
		int y = x;

		int mainLeft = w - 56;
		int mainTop = x - 16;

		AllGuiTextures.PLAYER_INVENTORY.draw(matrixStack, this, x - 16, y + 70 + 14);
		o.b(matrixStack, u.d(), x - 15 + 7, y + 64 + 26, 0x666666);

		SCHEMATIC_TABLE.draw(matrixStack, this, mainLeft, mainTop);

		if (t.getTileEntity().isUploading)
			o.a(matrixStack, uploading, mainLeft + 11, mainTop + 3, 0xffffff);
		else if (t.a(1)
			.f())
			o.a(matrixStack, finished, mainLeft + 11, mainTop + 3, 0xffffff);
		else
			o.a(matrixStack, d, mainLeft + 11, mainTop + 3, 0xffffff);
		if (schematicsArea == null)
			o.a(matrixStack, noSchematics, mainLeft + 54, mainTop + 26, 0xd3d3d3);

		GuiGameElement.of(renderedItem)
			.at(mainLeft + 217, mainTop + 98, -150)
			.scale(3)
			.render(matrixStack);

		i.L()
			.a(SCHEMATIC_TABLE_PROGRESS.location);
		int width = (int) (SCHEMATIC_TABLE_PROGRESS.width
			* afj.g(partialTicks, lastChasingProgress, chasingProgress));
		int height = SCHEMATIC_TABLE_PROGRESS.height;
		RenderSystem.disableLighting();
		b(matrixStack, mainLeft + 70, mainTop + 57, SCHEMATIC_TABLE_PROGRESS.startX,
			SCHEMATIC_TABLE_PROGRESS.startY, width, height);
	}

	@Override
	public void d() {
		super.tick();
		boolean finished = t.a(1)
			.f();

		if (t.getTileEntity().isUploading || finished) {
			if (finished) {
				chasingProgress = lastChasingProgress = progress = 1;
			} else {
				lastChasingProgress = chasingProgress;
				progress = t.getTileEntity().uploadingProgress;
				chasingProgress += (progress - chasingProgress) * .5f;
			}
			confirmButton.o = false;

			if (schematicsLabel != null) {
				schematicsLabel.colored(0xCCDDFF);
				String uploadingSchematic = t.getTileEntity().uploadingSchematic;
				schematicsLabel.text = uploadingSchematic == null ? null : new LiteralText(uploadingSchematic);
			}
			if (schematicsArea != null)
				schematicsArea.p = false;

		} else {
			progress = 0;
			chasingProgress = lastChasingProgress = 0;
			confirmButton.o = true;

			if (schematicsLabel != null)
				schematicsLabel.colored(0xFFFFFF);
			if (schematicsArea != null) {
				schematicsArea.writingTo(schematicsLabel);
				schematicsArea.p = true;
			}
		}
	}

	@Override
	public boolean a(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		ClientSchematicLoader schematicSender = CreateClient.schematicSender;

		if (confirmButton.o && confirmButton.g() && ((SchematicTableContainer) t).canWrite()
			&& schematicsArea != null) {

			lastChasingProgress = chasingProgress = progress = 0;
			List<Text> availableSchematics = schematicSender.getAvailableSchematics();
			Text schematic = availableSchematics.get(schematicsArea.getState());
			schematicSender.startNewUpload(schematic.asString());
		}

		if (folderButton.g()) {
			Util.getOperatingSystem()
				.open(Paths.get("schematics/")
					.toFile());
		}

		if (refreshButton.g()) {
			schematicSender.refresh();
			List<Text> availableSchematics = schematicSender.getAvailableSchematics();
			widgets.remove(schematicsArea);

			if (!availableSchematics.isEmpty()) {
				schematicsArea = new SelectionScrollInput(w - 56 + 33, x - 16 + 23, 134, 14)
					.forOptions(availableSchematics)
					.titled(availableSchematicsTitle.copy())
					.writingTo(schematicsLabel);
				schematicsArea.onChanged();
				widgets.add(schematicsArea);
			} else {
				schematicsArea = null;
				schematicsLabel.text = LiteralText.EMPTY;
			}
		}

		return super.a(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

}
