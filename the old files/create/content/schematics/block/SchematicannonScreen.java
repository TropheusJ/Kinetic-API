package com.simibubi.kinetic_api.content.schematics.block;

import static net.minecraft.util.Formatting.BLUE;
import static net.minecraft.util.Formatting.DARK_PURPLE;
import static net.minecraft.util.Formatting.GRAY;

import bfs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import com.google.common.collect.ImmutableList;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.schematics.packet.ConfigureSchematicannonPacket;
import com.simibubi.kinetic_api.content.schematics.packet.ConfigureSchematicannonPacket.Option;
import com.simibubi.kinetic_api.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.gui.AllIcons;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import com.simibubi.kinetic_api.foundation.gui.widgets.IconButton;
import com.simibubi.kinetic_api.foundation.gui.widgets.Indicator;
import com.simibubi.kinetic_api.foundation.gui.widgets.Indicator.State;
import com.simibubi.kinetic_api.foundation.item.ItemDescription.Palette;
import com.simibubi.kinetic_api.foundation.item.TooltipHelper;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.Lang;
public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonContainer> {

	private static final AllGuiTextures BG_BOTTOM = AllGuiTextures.SCHEMATICANNON_BOTTOM;
	private static final AllGuiTextures BG_TOP = AllGuiTextures.SCHEMATICANNON_TOP;

	protected Vector<Indicator> replaceLevelIndicators;
	protected Vector<IconButton> replaceLevelButtons;

	protected IconButton skipMissingButton;
	protected Indicator skipMissingIndicator;
	protected IconButton skipTilesButton;
	protected Indicator skipTilesIndicator;

	protected IconButton playButton;
	protected Indicator playIndicator;
	protected IconButton pauseButton;
	protected Indicator pauseIndicator;
	protected IconButton resetButton;
	protected Indicator resetIndicator;

	private List<ItemModels> extraAreas;
	protected List<OptionSliderWidget> placementSettingWidgets;

	private final Text title = Lang.translate("gui.schematicannon.title");
	private final Text listPrinter = Lang.translate("gui.schematicannon.listPrinter");
	private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
	private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
	private final String _showSettings = "gui.schematicannon.showOptions";
	private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

	private final String _slotGunpowder = "gui.schematicannon.slot.gunpowder";
	private final String _slotListPrinter = "gui.schematicannon.slot.listPrinter";
	private final String _slotSchematic = "gui.schematicannon.slot.schematic";

	private final Text optionEnabled = Lang.translate("gui.schematicannon.optionEnabled");
	private final Text optionDisabled = Lang.translate("gui.schematicannon.optionDisabled");

	private final ItemCooldownManager renderedItem = AllBlocks.SCHEMATICANNON.asStack();

	private IconButton confirmButton;
	private IconButton showSettingsButton;
	private Indicator showSettingsIndicator;

	public SchematicannonScreen(SchematicannonContainer container, bfs inventory,
								Text p_i51105_3_) {
		super(container, inventory, p_i51105_3_);
		placementSettingWidgets = new ArrayList<>();
	}

	@Override
	protected void b() {
		setWindowSize(BG_TOP.width + 50, BG_BOTTOM.height + BG_TOP.height + 80);
		super.b();

		int x = w + 20;
		int y = x;

		widgets.clear();

		// Play Pause Stop
		playButton = new IconButton(x + 75, y + 86, AllIcons.I_PLAY);
		playIndicator = new Indicator(x + 75, y + 79, LiteralText.EMPTY);
		pauseButton = new IconButton(x + 93, y + 86, AllIcons.I_PAUSE);
		pauseIndicator = new Indicator(x + 93, y + 79, LiteralText.EMPTY);
		resetButton = new IconButton(x + 111, y + 86, AllIcons.I_STOP);
		resetIndicator = new Indicator(x + 111, y + 79, LiteralText.EMPTY);
		resetIndicator.state = State.RED;
		Collections.addAll(widgets, playButton, playIndicator, pauseButton, pauseIndicator, resetButton,
			resetIndicator);

		extraAreas = new ArrayList<>();
		extraAreas.add(new ItemModels(w + 240, x + 88, 84, 113));

		confirmButton = new IconButton(x + 180, x + 117, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);
		showSettingsButton = new IconButton(w + 29, x + 117, AllIcons.I_PLACEMENT_SETTINGS);
		showSettingsButton.setToolTip(Lang.translate(_showSettings));
		widgets.add(showSettingsButton);
		showSettingsIndicator = new Indicator(w + 29, x + 111, LiteralText.EMPTY);
		widgets.add(showSettingsIndicator);

		d();
	}

	private void initPlacementSettings() {
		widgets.removeAll(placementSettingWidgets);
		placementSettingWidgets.clear();

		if (placementSettingsHidden())
			return;

		int x = w + 20;
		int y = x;

		// Replace settings
		replaceLevelButtons = new Vector<>(4);
		replaceLevelIndicators = new Vector<>(4);
		List<AllIcons> icons = ImmutableList.of(AllIcons.I_DONT_REPLACE, AllIcons.I_REPLACE_SOLID,
			AllIcons.I_REPLACE_ANY, AllIcons.I_REPLACE_EMPTY);
		List<Text> toolTips = ImmutableList.of(Lang.translate("gui.schematicannon.option.dontReplaceSolid"),
			Lang.translate("gui.schematicannon.option.replaceWithSolid"),
			Lang.translate("gui.schematicannon.option.replaceWithAny"),
			Lang.translate("gui.schematicannon.option.replaceWithEmpty"));

		for (int i = 0; i < 4; i++) {
			replaceLevelIndicators.add(new Indicator(x + 33 + i * 18, y + 111, LiteralText.EMPTY));
			replaceLevelButtons.add(new IconButton(x + 33 + i * 18, y + 117, icons.get(i)));
			replaceLevelButtons.get(i)
				.setToolTip(toolTips.get(i));
		}
		placementSettingWidgets.addAll(replaceLevelButtons);
		placementSettingWidgets.addAll(replaceLevelIndicators);

		// Other Settings
		skipMissingButton = new IconButton(x + 111, y + 117, AllIcons.I_SKIP_MISSING);
		skipMissingButton.setToolTip(Lang.translate("gui.schematicannon.option.skipMissing"));
		skipMissingIndicator = new Indicator(x + 111, y + 111, LiteralText.EMPTY);
		Collections.addAll(placementSettingWidgets, skipMissingButton, skipMissingIndicator);

		skipTilesButton = new IconButton(x + 129, y + 117, AllIcons.I_SKIP_TILES);
		skipTilesButton.setToolTip(Lang.translate("gui.schematicannon.option.skipTileEntities"));
		skipTilesIndicator = new Indicator(x + 129, y + 111, LiteralText.EMPTY);
		Collections.addAll(placementSettingWidgets, skipTilesButton, skipTilesIndicator);

		widgets.addAll(placementSettingWidgets);
	}

	protected boolean placementSettingsHidden() {
		return showSettingsIndicator.state == State.OFF;
	}

	@Override
	public void d() {
		SchematicannonTileEntity te = t.getTileEntity();

		if (!placementSettingsHidden()) {
			for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++)
				replaceLevelIndicators.get(replaceMode).state = replaceMode == te.replaceMode ? State.ON : State.OFF;
			skipMissingIndicator.state = te.skipMissing ? State.ON : State.OFF;
			skipTilesIndicator.state = !te.replaceTileEntities ? State.ON : State.OFF;
		}

		playIndicator.state = State.OFF;
		pauseIndicator.state = State.OFF;
		resetIndicator.state = State.OFF;

		switch (te.state) {
			case PAUSED:
				pauseIndicator.state = State.YELLOW;
				playButton.o = true;
				pauseButton.o = false;
				resetButton.o = true;
				break;
			case RUNNING:
				playIndicator.state = State.GREEN;
				playButton.o = false;
				pauseButton.o = true;
				resetButton.o = true;
				break;
			case STOPPED:
				resetIndicator.state = State.RED;
				playButton.o = true;
				pauseButton.o = false;
				resetButton.o = false;
				break;
			default:
				break;
		}

		handleTooltips();

		super.tick();
	}

	protected void handleTooltips() {
		if (placementSettingsHidden())
			return;

		for (OptionSliderWidget w : placementSettingWidgets)
			if (w instanceof IconButton) {
				IconButton button = (IconButton) w;
				if (!button.getToolTip()
					.isEmpty()) {
					button.setToolTip(button.getToolTip()
						.get(0));
					button.getToolTip()
						.add(TooltipHelper.holdShift(Palette.Blue, y()));
				}
			}

		if (y()) {
			fillToolTip(skipMissingButton, skipMissingIndicator, "skipMissing");
			fillToolTip(skipTilesButton, skipTilesIndicator, "skipTileEntities");
			fillToolTip(replaceLevelButtons.get(0), replaceLevelIndicators.get(0), "dontReplaceSolid");
			fillToolTip(replaceLevelButtons.get(1), replaceLevelIndicators.get(1), "replaceWithSolid");
			fillToolTip(replaceLevelButtons.get(2), replaceLevelIndicators.get(2), "replaceWithAny");
			fillToolTip(replaceLevelButtons.get(3), replaceLevelIndicators.get(3), "replaceWithEmpty");
		}
	}

	private void fillToolTip(IconButton button, Indicator indicator, String tooltipKey) {
		if (!button.g())
			return;
		boolean enabled = indicator.state == State.ON;
		List<Text> tip = button.getToolTip();
		tip.add((enabled ? optionEnabled : optionDisabled).copy().formatted(BLUE));
		tip.addAll(TooltipHelper.cutTextComponent(Lang.translate("gui.schematicannon.option." + tooltipKey + ".description"),
			GRAY, GRAY));
	}

	@Override
	protected void renderWindow(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		AllGuiTextures.PLAYER_INVENTORY.draw(matrixStack, this, w - 10, x + 145);
		BG_TOP.draw(matrixStack, this, w + 20, x);
		BG_BOTTOM.draw(matrixStack, this, w + 20, x + BG_TOP.height);

		SchematicannonTileEntity te = t.getTileEntity();
		renderPrintingProgress(matrixStack, te.schematicProgress);
		renderFuelBar(matrixStack, te.fuelLevel);
		renderChecklistPrinterProgress(matrixStack, te.bookPrintingProgress);

		if (!te.inventory.getStackInSlot(0)
			.a())
			renderBlueprintHighlight(matrixStack);

		GuiGameElement.of(renderedItem)
			.at(w + 230, x + 190, -200)
			.scale(5)
			.render(matrixStack);

		o.a(matrixStack, d, w + 80, x + 3, 0xfefefe);

		Text msg = Lang.translate("schematicannon.status." + te.statusMsg);
		int stringWidth = o.a(msg);

		if (te.missingItem != null) {
			stringWidth += 15;
			matrixStack.a();
			GuiGameElement.of(te.missingItem)
				.at(w + 150, x + 62, 100)
				.scale(1)
				.render(matrixStack);
			matrixStack.b();
		}

		o.a(matrixStack, msg, w + 20 + 102 - stringWidth / 2, x + 50, 0xCCDDFF);
		o.b(matrixStack, u.d(), w - 10 + 7, x + 145 + 6, 0x666666);

		// to see or debug the bounds of the extra area uncomment the following lines
		// Rectangle2d r = extraAreas.get(0);
		// fill(r.getX() + r.getWidth(), r.getY() + r.getHeight(), r.getX(), r.getY(),
		// 0xd3d3d3d3);
	}

	protected void renderBlueprintHighlight(BufferVertexConsumer matrixStack) {
		AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.draw(matrixStack, this, w + 20 + 10, x + 60);
	}

	protected void renderPrintingProgress(BufferVertexConsumer matrixStack, float progress) {
		progress = Math.min(progress, 1);
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
		i.L()
			.a(sprite.location);
		b(matrixStack, w + 20 + 44, x + 64, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderChecklistPrinterProgress(BufferVertexConsumer matrixStack, float progress) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_CHECKLIST_PROGRESS;
		i.L()
			.a(sprite.location);
		b(matrixStack, w + 20 + 154, x + 20, sprite.startX, sprite.startY, (int) (sprite.width * progress),
			sprite.height);
	}

	protected void renderFuelBar(BufferVertexConsumer matrixStack, float amount) {
		AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
		if (t.getTileEntity().hasCreativeCrate) {
			AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.draw(matrixStack, this, w + 20 + 36, x + 19);
			return;
		}
		i.L()
			.a(sprite.location);
		b(matrixStack, w + 20 + 36, x + 19, sprite.startX, sprite.startY, (int) (sprite.width * amount),
			sprite.height);
	}

	@Override
	protected void renderWindowForeground(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		SchematicannonTileEntity te = t.getTileEntity();

		int fuelX = w + 20 + 36, fuelY = x + 19;
		if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.width
			&& mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.height) {
			List<Text> tooltip = getFuelLevelTooltip(te);
			b(matrixStack, tooltip, mouseX, mouseY);
		}

		if (v != null && !v.f()) {
			if (v.d == 0)
				b(matrixStack,
					TooltipHelper.cutTextComponent(Lang.translate(_slotSchematic), GRAY, Formatting.BLUE),
					mouseX, mouseY);
			if (v.d == 2)
				b(matrixStack,
					TooltipHelper.cutTextComponent(Lang.translate(_slotListPrinter), GRAY, Formatting.BLUE),
					mouseX, mouseY);
			if (v.d == 4)
				b(matrixStack,
					TooltipHelper.cutTextComponent(Lang.translate(_slotGunpowder), GRAY, Formatting.BLUE),
					mouseX, mouseY);
		}

		if (te.missingItem != null) {
			int missingBlockX = w + 150, missingBlockY = x + 46;
			if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16
				&& mouseY <= missingBlockY + 16) {
				a(matrixStack, te.missingItem, mouseX, mouseY);
			}
		}

		int paperX = w + 132, paperY = x + 19;
		if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16)
			b(matrixStack, listPrinter, mouseX, mouseY);

		super.renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	protected List<Text> getFuelLevelTooltip(SchematicannonTileEntity te) {
		double fuelUsageRate = te.getFuelUsageRate();
		int shotsLeft = (int) (te.fuelLevel / fuelUsageRate);
		int shotsLeftWithItems = (int) (shotsLeft + te.inventory.getStackInSlot(4)
			.E() * (te.getFuelAddedByGunPowder() / fuelUsageRate));
		List<Text> tooltip = new ArrayList<>();

		if (te.hasCreativeCrate) {
			tooltip.add(Lang.translate(_gunpowderLevel, "" + 100));
			tooltip.add(new LiteralText("(").append(new TranslatableText(AllBlocks.CREATIVE_CRATE.get()
				.i())).append(")").formatted(DARK_PURPLE));
			return tooltip;
		}

		float f = te.fuelLevel * 100;
		tooltip.add(Lang.translate(_gunpowderLevel, "" + (int) f));
		tooltip.add(Lang.translate(_shotsRemaining, "" + Formatting.BLUE + shotsLeft).formatted(GRAY)); // fixme
		if (shotsLeftWithItems != shotsLeft)
			tooltip
				.add(Lang.translate(_shotsRemainingWithBackup, "" + Formatting.BLUE + shotsLeftWithItems).formatted(GRAY)); // fixme
		return tooltip;
	}

	@Override
	public boolean a(double x, double y, int button) {
		if (showSettingsButton.g()) {
			showSettingsIndicator.state = placementSettingsHidden() ? State.GREEN : State.OFF;
			initPlacementSettings();
		}

		if (confirmButton.g()) {
			KeyBinding.B().s.m();
			return true;
		}

		if (!placementSettingsHidden()) {
			for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++) {
				if (!replaceLevelButtons.get(replaceMode)
					.g())
					continue;
				if (t.getTileEntity().replaceMode == replaceMode)
					continue;
				sendOptionUpdate(Option.values()[replaceMode], true);
			}
			if (skipMissingButton.g())
				sendOptionUpdate(Option.SKIP_MISSING, !t.getTileEntity().skipMissing);
			if (skipTilesButton.g())
				sendOptionUpdate(Option.SKIP_TILES, !t.getTileEntity().replaceTileEntities);
		}

		if (playButton.g() && playButton.o)
			sendOptionUpdate(Option.PLAY, true);
		if (pauseButton.g() && pauseButton.o)
			sendOptionUpdate(Option.PAUSE, true);
		if (resetButton.g() && resetButton.o)
			sendOptionUpdate(Option.STOP, true);

		return super.a(x, y, button);
	}

	@Override
	public List<ItemModels> getExtraAreas() {
		return extraAreas;
	}

	protected void sendOptionUpdate(Option option, boolean set) {
		AllPackets.channel.sendToServer(ConfigureSchematicannonPacket.setOption(t.getTileEntity()
			.o(), option, set));
	}

}

