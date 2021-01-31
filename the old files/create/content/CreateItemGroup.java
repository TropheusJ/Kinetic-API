package com.simibubi.kinetic_api.content;

import java.util.EnumSet;
import net.minecraft.entity.player.ItemCooldownManager;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.item.CreateItemGroupBase;

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
