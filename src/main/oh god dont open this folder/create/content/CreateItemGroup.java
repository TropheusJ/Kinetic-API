package com.simibubi.create.content;

import java.util.EnumSet;
import net.minecraft.entity.player.ItemCooldownManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.item.CreateItemGroupBase;

public class CreateItemGroup extends CreateItemGroupBase {

	public CreateItemGroup() {
		super("base");
	}

	@Override
	protected EnumSet<AllSections> getSections() {
		return EnumSet.complementOf(EnumSet.of(AllSections.PALETTES));
	}

	@Override
	public ItemCooldownManager e() {
		return AllBlocks.COGWHEEL.asStack();
	}

}
