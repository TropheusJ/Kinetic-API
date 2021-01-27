package com.simibubi.create.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import bfs;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterContainer> {

	private static final String PREFIX = "gui.attribute_filter.";

	private IconButton whitelistDis, whitelistCon, blacklist;
	private Indicator whitelistDisIndicator, whitelistConIndicator, blacklistIndicator;
	private IconButton add;
	private IconButton addInverted;

	private Text addDESC = Lang.translate(PREFIX + "add_attribute");
	private Text addInvertedDESC = Lang.translate(PREFIX + "add_inverted_attribute");

	private Text allowDisN = Lang.translate(PREFIX + "allow_list_disjunctive");
	private Text allowDisDESC = Lang.translate(PREFIX + "allow_list_disjunctive.description");
	private Text allowConN = Lang.translate(PREFIX + "allow_list_conjunctive");
	private Text allowConDESC = Lang.translate(PREFIX + "allow_list_conjunctive.description");
	private Text denyN = Lang.translate(PREFIX + "deny_list");
	private Text denyDESC = Lang.translate(PREFIX + "deny_list.description");

	private Text referenceH = Lang.translate(PREFIX + "add_reference_item");
	private Text noSelectedT = Lang.translate(PREFIX + "no_selected_attributes");
	private Text selectedT = Lang.translate(PREFIX + "selected_attributes");

	private ItemCooldownManager lastItemScanned = ItemCooldownManager.tick;
	private List<ItemAttribute> attributesOfItem = new ArrayList<>();
	private List<Text> selectedAttributes = new ArrayList<>();
	private SelectionScrollInput attributeSelector;
	private Label attributeSelectorLabel;

	public AttributeFilterScreen(AttributeFilterContainer container, bfs inv, Text title) {
		super(container, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
	}

	@Override
	protected void b() {
		super.b();
		int x = w;
		int y = x;

		whitelistDis = new IconButton(x + 47, y + 59, AllIcons.I_WHITELIST_OR);
		whitelistDis.setToolTip(allowDisN);
		whitelistCon = new IconButton(x + 65, y + 59, AllIcons.I_WHITELIST_AND);
		whitelistCon.setToolTip(allowConN);
		blacklist = new IconButton(x + 83, y + 59, AllIcons.I_WHITELIST_NOT);
		blacklist.setToolTip(denyN);

		whitelistDisIndicator = new Indicator(x + 47, y + 53, LiteralText.EMPTY);
		whitelistConIndicator = new Indicator(x + 65, y + 53, LiteralText.EMPTY);
		blacklistIndicator = new Indicator(x + 83, y + 53, LiteralText.EMPTY);

		widgets.addAll(Arrays.asList(blacklist, whitelistCon, whitelistDis, blacklistIndicator, whitelistConIndicator,
			whitelistDisIndicator));

		widgets.add(add = new IconButton(x + 182, y + 21, AllIcons.I_ADD));
		widgets.add(addInverted = new IconButton(x + 200, y + 21, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
		add.setToolTip(addDESC);
		addInverted.setToolTip(addInvertedDESC);

		handleIndicators();

		attributeSelectorLabel = new Label(x + 43, y + 26, LiteralText.EMPTY).colored(0xF3EBDE)
			.withShadow();
		attributeSelector = new SelectionScrollInput(x + 39, y + 21, 137, 18);
		attributeSelector.forOptions(Arrays.asList(LiteralText.EMPTY));
		attributeSelector.removeCallback();
		referenceItemChanged(t.filterInventory.getStackInSlot(0));

		widgets.add(attributeSelector);
		widgets.add(attributeSelectorLabel);

		selectedAttributes.clear();
		selectedAttributes.add((t.selectedAttributes.isEmpty() ? noSelectedT : selectedT).copy()
			.formatted(Formatting.YELLOW));
		t.selectedAttributes.forEach(at -> selectedAttributes.add(new LiteralText("- ")
			.append(at.getFirst()
				.format(at.getSecond()))
			.formatted(Formatting.GRAY)));

	}

	private void referenceItemChanged(ItemCooldownManager stack) {
		lastItemScanned = stack;

		if (stack.a()) {
			attributeSelector.o = false;
			attributeSelector.p = false;
			attributeSelectorLabel.text = referenceH.copy()
				.formatted(Formatting.ITALIC);
			add.o = false;
			addInverted.o = false;
			attributeSelector.calling(s -> {
			});
			return;
		}

		add.o = true;

		addInverted.o = true;
		attributeSelector.titled(stack.r()
			.copy()
			.append("..."));
		attributesOfItem.clear();
		for (ItemAttribute itemAttribute : ItemAttribute.types)
			attributesOfItem.addAll(itemAttribute.listAttributesOf(stack, i.r));
		List<Text> options = attributesOfItem.stream()
			.map(a -> a.format(false))
			.collect(Collectors.toList());
		attributeSelector.forOptions(options);
		attributeSelector.o = true;
		attributeSelector.p = true;
		attributeSelector.setState(0);
		attributeSelector.calling(i -> {
			attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
			ItemAttribute selected = attributesOfItem.get(i);
			for (Pair<ItemAttribute, Boolean> existing : t.selectedAttributes) {
				CompoundTag testTag = new CompoundTag();
				CompoundTag testTag2 = new CompoundTag();
				existing.getFirst()
					.serializeNBT(testTag);
				selected.serializeNBT(testTag2);
				if (testTag.equals(testTag2)) {
					add.o = false;
					addInverted.o = false;
					return;
				}
			}
			add.o = true;
			addInverted.o = true;
		});
		attributeSelector.onChanged();
	}

	@Override
	public void renderWindowForeground(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		ItemCooldownManager stack = t.filterInventory.getStackInSlot(1);
		matrixStack.a();
		matrixStack.a(0.0F, 0.0F, 32.0F);
		this.d(200);
		this.j.b = 200.0F;
		this.j.a(o, stack, w + 22, x + 57,
			String.valueOf(selectedAttributes.size() - 1));
		this.d(0);
		this.j.b = 0.0F;
		matrixStack.b();

		super.renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void d() {
		super.d();
		ItemCooldownManager stackInSlot = t.filterInventory.getStackInSlot(0);
		if (!stackInSlot.equals(lastItemScanned, false))
			referenceItemChanged(stackInSlot);
	}

	@Override
	protected void a(BufferVertexConsumer matrixStack, int mouseX, int mouseY) {
		if (this.i.s.bm.m()
			.a() && this.v != null && this.v.f()) {
			if (this.v.d == 37) {
				b(matrixStack, selectedAttributes, mouseX, mouseY);
				return;
			}
			this.a(matrixStack, this.v.e(), mouseX, mouseY);
		}
		super.a(matrixStack, mouseX, mouseY);
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelistCon, whitelistDis);
	}

	@Override
	protected List<MutableText> getTooltipDescriptions() {
		return Arrays.asList(denyDESC.copy(), allowConDESC.copy(), allowDisDESC.copy());
	}

	@Override
	public boolean a(double x, double y, int button) {
		boolean mouseClicked = super.a(x, y, button);

		if (button != 0)
			return mouseClicked;

		if (blacklist.g()) {
			t.whitelistMode = WhitelistMode.BLACKLIST;
			sendOptionUpdate(Option.BLACKLIST);
			return true;
		}

		if (whitelistCon.g()) {
			t.whitelistMode = WhitelistMode.WHITELIST_CONJ;
			sendOptionUpdate(Option.WHITELIST2);
			return true;
		}

		if (whitelistDis.g()) {
			t.whitelistMode = WhitelistMode.WHITELIST_DISJ;
			sendOptionUpdate(Option.WHITELIST);
			return true;
		}

		if (add.g() && add.o)
			return handleAddedAttibute(false);
		if (addInverted.g() && addInverted.o)
			return handleAddedAttibute(true);

		return mouseClicked;
	}

	protected boolean handleAddedAttibute(boolean inverted) {
		int index = attributeSelector.getState();
		if (index >= attributesOfItem.size())
			return false;
		add.o = false;
		addInverted.o = false;
		CompoundTag tag = new CompoundTag();
		ItemAttribute itemAttribute = attributesOfItem.get(index);
		itemAttribute.serializeNBT(tag);
		AllPackets.channel
			.sendToServer(new FilterScreenPacket(inverted ? Option.ADD_INVERTED_TAG : Option.ADD_TAG, tag));
		t.appendSelectedAttribute(itemAttribute, inverted);
		if (t.selectedAttributes.size() == 1)
			selectedAttributes.set(0, selectedT.copy()
				.formatted(Formatting.YELLOW));
		selectedAttributes.add(new LiteralText("- ").append(itemAttribute.format(inverted))
			.formatted(Formatting.GRAY));
		return true;
	}

	@Override
	protected void contentsCleared() {
		selectedAttributes.clear();
		selectedAttributes.add(noSelectedT.copy()
			.formatted(Formatting.YELLOW));
		if (!lastItemScanned.a()) {
			add.o = true;
			addInverted.o = true;
		}
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return t.whitelistMode != WhitelistMode.BLACKLIST;
		if (button == whitelistCon)
			return t.whitelistMode != WhitelistMode.WHITELIST_CONJ;
		if (button == whitelistDis)
			return t.whitelistMode != WhitelistMode.WHITELIST_DISJ;
		return true;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		if (indicator == blacklistIndicator)
			return t.whitelistMode == WhitelistMode.BLACKLIST;
		if (indicator == whitelistConIndicator)
			return t.whitelistMode == WhitelistMode.WHITELIST_CONJ;
		if (indicator == whitelistDisIndicator)
			return t.whitelistMode == WhitelistMode.WHITELIST_DISJ;
		return false;
	}

}
