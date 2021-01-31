package com.simibubi.kinetic_api.foundation.item;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class TagDependentIngredientItem extends HoeItem {

	private Identifier tag;

	public TagDependentIngredientItem(a p_i48487_1_, Identifier tag) {
		super(p_i48487_1_);
		this.tag = tag;
	}

	@Override
	public void a(ChorusFruitItem p_150895_1_, DefaultedList<ItemCooldownManager> p_150895_2_) {
		if (!shouldHide())
			super.a(p_150895_1_, p_150895_2_);
	}

	public boolean shouldHide() {
		RequiredTagList<?> tag = EntityTypeTags.a()
			.a(this.tag);
		return tag == null || tag.b()
			.isEmpty();
	}

}
