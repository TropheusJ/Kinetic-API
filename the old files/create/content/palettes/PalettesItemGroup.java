package com.simibubi.kinetic_api.content.palettes;

import java.util.EnumSet;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.collection.DefaultedList;
import com.simibubi.kinetic_api.content.AllSections;
import com.simibubi.kinetic_api.foundation.item.CreateItemGroupBase;

public class PalettesItemGroup extends CreateItemGroupBase {

	public PalettesItemGroup() {
		super("palettes");
	}

	@Override
	protected EnumSet<AllSections> getSections() {
		return EnumSet.of(AllSections.PALETTES);
	}

	@Override
	public void addItems(DefaultedList<ItemCooldownManager> items, boolean specialItems) {}

	@Override
	public ItemCooldownManager e() {
		return new ItemCooldownManager(AllPaletteBlocks.ORNATE_IRON_WINDOW.get());
	}

}
