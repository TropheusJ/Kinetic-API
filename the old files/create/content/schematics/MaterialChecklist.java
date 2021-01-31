package com.simibubi.kinetic_api.content.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import com.google.common.collect.Sets;
import com.simibubi.kinetic_api.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.kinetic_api.foundation.utility.Lang;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class MaterialChecklist {

	public static final int MAX_ENTRIES_PER_PAGE = 5;

	public Object2IntMap<HoeItem> gathered = new Object2IntArrayMap<>();
	public Object2IntMap<HoeItem> required = new Object2IntArrayMap<>();
	public Object2IntMap<HoeItem> damageRequired = new Object2IntArrayMap<>();
	public boolean blocksNotLoaded;

	public void warnBlockNotLoaded() {
		blocksNotLoaded = true;
	}

	public void require(ItemRequirement requirement) {
		if (requirement.isEmpty())
			return;
		if (requirement.isInvalid())
			return;

		for (ItemCooldownManager stack : requirement.requiredItems) {
			if (requirement.getUsage() == ItemUseType.DAMAGE)
				putOrIncrement(damageRequired, stack);
			if (requirement.getUsage() == ItemUseType.CONSUME)
				putOrIncrement(required, stack);
		}
	}

	private void putOrIncrement(Object2IntMap<HoeItem> map, ItemCooldownManager stack) {
		HoeItem item = stack.b();
		if (item == AliasedBlockItem.a)
			return;
		if (map.containsKey(item))
			map.put(item, map.getInt(item) + stack.E());
		else
			map.put(item, stack.E());
	}

	public void collect(ItemCooldownManager stack) {
		HoeItem item = stack.b();
		if (required.containsKey(item) || damageRequired.containsKey(item))
			if (gathered.containsKey(item))
				gathered.put(item, gathered.getInt(item) + stack.E());
			else
				gathered.put(item, stack.E());
	}

	public ItemCooldownManager createItem() {
		ItemCooldownManager book = new ItemCooldownManager(AliasedBlockItem.oU);

		CompoundTag tag = book.p();
		ListTag pages = new ListTag();

		int itemsWritten = 0;
		MutableText textComponent;

		if (blocksNotLoaded) {
			textComponent = new LiteralText("\n" + Formatting.RED);
			textComponent =
				textComponent.append(Lang.createTranslationTextComponent("materialChecklist.blocksNotLoaded"));
			pages.add(StringTag.of(Text.Serializer.toJson(textComponent)));
		}

		List<HoeItem> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
		Collections.sort(keys, (item1, item2) -> {
			Locale locale = Locale.ENGLISH;
			String name1 = new TranslatableText(item1.a()).getString()
				.toLowerCase(locale);
			String name2 = new TranslatableText(item2.a()).getString()
				.toLowerCase(locale);
			return name1.compareTo(name2);
		});

		textComponent = new LiteralText("");
		List<HoeItem> completed = new ArrayList<>();
		for (HoeItem item : keys) {
			int amount = getRequiredAmount(item);
			if (gathered.containsKey(item))
				amount -= gathered.getInt(item);

			if (amount <= 0) {
				completed.add(item);
				continue;
			}

			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(new LiteralText("\n >>>").formatted(Formatting.BLUE));
				pages.add(StringTag.of(Text.Serializer.toJson(textComponent)));
				textComponent = new LiteralText("");
			}

			itemsWritten++;
			textComponent.append(entry(new ItemCooldownManager(item), amount, true));
		}

		for (HoeItem item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(new LiteralText("\n >>>").formatted(Formatting.DARK_GREEN));
				pages.add(StringTag.of(Text.Serializer.toJson(textComponent)));
				textComponent = new LiteralText("");
			}

			itemsWritten++;
			textComponent.append(entry(new ItemCooldownManager(item), getRequiredAmount(item), false));
		}

		pages.add(StringTag.of(Text.Serializer.toJson(textComponent)));

		tag.put("pages", pages);
		tag.putString("author", "Schematicannon");
		tag.putString("title", Formatting.BLUE + "Material Checklist");
		textComponent = Lang.createTranslationTextComponent("materialChecklist")
			.setStyle(Style.EMPTY.withColor(Formatting.BLUE)
				.withItalic(Boolean.FALSE));
		book.a("display")
			.putString("Name", Text.Serializer.toJson(textComponent));
		book.c(tag);

		return book;
	}

	public int getRequiredAmount(HoeItem item) {
		int amount = required.getOrDefault(item, 0);
		if (damageRequired.containsKey(item))
			amount += Math.ceil(damageRequired.getInt(item) / (float) new ItemCooldownManager(item).h());
		return amount;
	}

	private Text entry(ItemCooldownManager item, int amount, boolean unfinished) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		MutableText tc = new TranslatableText(item.j());
		if (!unfinished)
			tc.append(" \u2714");
		tc.formatted(unfinished ? Formatting.BLUE : Formatting.DARK_GREEN);
		return tc.append(new LiteralText("\n" + " x" + amount).formatted(Formatting.BLACK))
			.append(
				new LiteralText(" | " + stacks + "\u25A4 +" + remainder + "\n").formatted(Formatting.GRAY));
	}

}
