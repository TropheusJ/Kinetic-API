package com.simibubi.kinetic_api.content.curiosities;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;

public class CombustibleItem extends HoeItem {
	private int burnTime = -1;

	public CombustibleItem(a properties) {
		super(properties);
	}

	public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	}

	@Override
	public int getBurnTime(ItemCooldownManager itemStack) {
		return this.burnTime;
	}
}