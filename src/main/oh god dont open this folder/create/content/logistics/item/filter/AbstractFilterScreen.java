package com.simibubi.create.content.logistics.item.filter;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static net.minecraft.util.Formatting.GRAY;

import bfs;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;

public abstract class AbstractFilterScreen<F extends AbstractFilterContainer> extends AbstractSimiContainerScreen<F> {

	protected AllGuiTextures background;

	private IconButton resetButton;
	private IconButton confirmButton;

	protected AbstractFilterScreen(F container, bfs inv, Text title, AllGuiTextures background) {
		super(container, inv, title);
		this.background = background;
	}

	@Override
	protected void b() {
		setWindowSize(background.width + 80, background.height + PLAYER_INVENTORY.height + 20);
		super.b();
		widgets.clear();

		resetButton =
			new IconButton(w + background.width - 62, x + background.height - 24, AllIcons.I_TRASH);
		confirmButton =
			new IconButton(w + background.width - 33, x + background.height - 24, AllIcons.I_CONFIRM);

		widgets.add(resetButton);
		widgets.add(confirmButton);
	}

	@Override
	protected void renderWindow(BufferVertexConsumer ms, int mouseX, int mouseY, float partialTicks) {
		int x = w;
		int y = x;
		background.draw(ms, this, x, y);

		int invX = x + 50;
		int invY = y + background.height + 10;
		PLAYER_INVENTORY.draw(ms, this, invX, invY);
		o.b(ms, u.d(), invX + 7, invY + 6, 0x666666);
		o.b(ms, StatusEffectSpriteManager.a(t.filterItem.j()), x + 15, y + 3, 0xdedede);

		GuiGameElement.of(t.filterItem)
			.at(w + background.width, x + background.height + 25, -150)
			.scale(5)
			.render(ms);

	}

	@Override
	public void d() {
		handleTooltips();
		super.tick();
		handleIndicators();

		if (!t.player.dC()
			.equals(t.filterItem, false))
			i.s.m();
	}

	public void handleIndicators() {
		List<IconButton> tooltipButtons = getTooltipButtons();
		for (IconButton button : tooltipButtons)
			button.o = isButtonEnabled(button);
		for (OptionSliderWidget w : widgets)
			if (w instanceof Indicator)
				((Indicator) w).state = isIndicatorOn((Indicator) w) ? State.ON : State.OFF;
	}

	protected abstract boolean isButtonEnabled(IconButton button);

	protected abstract boolean isIndicatorOn(Indicator indicator);

	protected void handleTooltips() {
		List<IconButton> tooltipButtons = getTooltipButtons();

		for (IconButton button : tooltipButtons) {
			if (!button.getToolTip()
				.isEmpty()) {
				button.setToolTip(button.getToolTip()
					.get(0));
				button.getToolTip()
					.add(TooltipHelper.holdShift(Palette.Yellow, y()));
			}
		}

		if (y()) {
			List<MutableText> tooltipDescriptions = getTooltipDescriptions();
			for (int i = 0; i < tooltipButtons.size(); i++)
				fillToolTip(tooltipButtons.get(i), tooltipDescriptions.get(i));
		}
	}

	protected List<IconButton> getTooltipButtons() {
		return Collections.emptyList();
	}

	protected List<MutableText> getTooltipDescriptions() {
		return Collections.emptyList();
	}

	private void fillToolTip(IconButton button, Text tooltip) {
		if (!button.g())
			return;
		List<Text> tip = button.getToolTip();
		tip.addAll(TooltipHelper.cutTextComponent(tooltip, GRAY, GRAY));
	}

	@Override
	public boolean a(double x, double y, int button) {
		boolean mouseClicked = super.a(x, y, button);

		if (button == 0) {
			if (confirmButton.g()) {
				i.s.m();
				return true;
			}
			if (resetButton.g()) {
				t.clearContents();
				contentsCleared();
				sendOptionUpdate(Option.CLEAR);
				return true;
			}
		}

		return mouseClicked;
	}

	protected void contentsCleared() {}

	protected void sendOptionUpdate(Option option) {
		AllPackets.channel.sendToServer(new FilterScreenPacket(option));
	}

}
