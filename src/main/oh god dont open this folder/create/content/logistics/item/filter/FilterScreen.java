package com.simibubi.create.content.logistics.item.filter;

import java.util.Arrays;
import java.util.List;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import bfs;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.utility.Lang;

public class FilterScreen extends AbstractFilterScreen<FilterContainer> {

	private static final String PREFIX = "gui.filter.";

	private Text allowN = Lang.translate(PREFIX + "allow_list");
	private Text allowDESC = Lang.translate(PREFIX + "allow_list.description");
	private Text denyN = Lang.translate(PREFIX + "deny_list");
	private Text denyDESC = Lang.translate(PREFIX + "deny_list.description");

	private Text respectDataN = Lang.translate(PREFIX + "respect_data");
	private Text respectDataDESC = Lang.translate(PREFIX + "respect_data.description");
	private Text ignoreDataN = Lang.translate(PREFIX + "ignore_data");
	private Text ignoreDataDESC = Lang.translate(PREFIX + "ignore_data.description");

	private IconButton whitelist, blacklist;
	private IconButton respectNBT, ignoreNBT;
	private Indicator whitelistIndicator, blacklistIndicator;
	private Indicator respectNBTIndicator, ignoreNBTIndicator;

	public FilterScreen(FilterContainer container, bfs inv, Text title) {
		super(container, inv, title, AllGuiTextures.FILTER);
	}

	@Override
	protected void b() {
		super.b();
		int x = w;
		int y = x;

		blacklist = new IconButton(x + 18, y + 73, AllIcons.I_BLACKLIST);
		blacklist.setToolTip(denyN);
		whitelist = new IconButton(x + 36, y + 73, AllIcons.I_WHITELIST);
		whitelist.setToolTip(allowN);
		blacklistIndicator = new Indicator(x + 18, y + 67, LiteralText.EMPTY);
		whitelistIndicator = new Indicator(x + 36, y + 67, LiteralText.EMPTY);
		widgets.addAll(Arrays.asList(blacklist, whitelist, blacklistIndicator, whitelistIndicator));

		respectNBT = new IconButton(x + 60, y + 73, AllIcons.I_RESPECT_NBT);
		respectNBT.setToolTip(respectDataN);
		ignoreNBT = new IconButton(x + 78, y + 73, AllIcons.I_IGNORE_NBT);
		ignoreNBT.setToolTip(ignoreDataN);
		respectNBTIndicator = new Indicator(x + 60, y + 67, LiteralText.EMPTY);
		ignoreNBTIndicator = new Indicator(x + 78, y + 67, LiteralText.EMPTY);
		widgets.addAll(Arrays.asList(respectNBT, ignoreNBT, respectNBTIndicator, ignoreNBTIndicator));
		handleIndicators();
	}

	@Override
	public boolean a(double x, double y, int button) {
		boolean mouseClicked = super.a(x, y, button);

		if (button != 0)
			return mouseClicked;

		if (blacklist.g()) {
			t.blacklist = true;
			sendOptionUpdate(Option.BLACKLIST);
			return true;
		}

		if (whitelist.g()) {
			t.blacklist = false;
			sendOptionUpdate(Option.WHITELIST);
			return true;
		}

		if (respectNBT.g()) {
			t.respectNBT = true;
			sendOptionUpdate(Option.RESPECT_DATA);
			return true;
		}

		if (ignoreNBT.g()) {
			t.respectNBT = false;
			sendOptionUpdate(Option.IGNORE_DATA);
			return true;
		}

		return mouseClicked;
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelist, respectNBT, ignoreNBT);
	}

	@Override
	protected List<MutableText> getTooltipDescriptions() {
		return Arrays.asList(denyDESC.copy(), allowDESC.copy(), respectDataDESC.copy(), ignoreDataDESC.copy());
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return !t.blacklist;
		if (button == whitelist)
			return t.blacklist;
		if (button == respectNBT)
			return !t.respectNBT;
		if (button == ignoreNBT)
			return t.respectNBT;
		return true;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		if (indicator == blacklistIndicator)
			return t.blacklist;
		if (indicator == whitelistIndicator)
			return !t.blacklist;
		if (indicator == respectNBTIndicator)
			return t.respectNBT;
		if (indicator == ignoreNBTIndicator)
			return !t.respectNBT;
		return false;
	}

}
