package com.simibubi.create.foundation.item;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.util.collection.DefaultedList;

public class HiddenIngredientItem extends HoeItem {

	public HiddenIngredientItem(a p_i48487_1_) {
		super(p_i48487_1_);
	}
	
	@Override
	public void a(ChorusFruitItem p_150895_1_, DefaultedList<ItemCooldownManager> p_150895_2_) {
		if (p_150895_1_ != ChorusFruitItem.g)
			return;
		super.a(p_150895_1_, p_150895_2_);
	}

}
