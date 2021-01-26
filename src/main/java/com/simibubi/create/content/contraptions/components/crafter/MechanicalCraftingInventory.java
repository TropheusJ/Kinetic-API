package com.simibubi.create.content.contraptions.components.crafter;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.components.crafter.RecipeGridHandler.GroupedItems;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.screen.PropertyDelegate;

public class MechanicalCraftingInventory extends PropertyDelegate {

	private static FoodComponent dummyContainer = new FoodComponent(null, -1) {
		public boolean a(PlayerAbilities playerIn) {
			return false;
		}
	};

	public MechanicalCraftingInventory(GroupedItems items) {
		super(dummyContainer, items.width, items.height);
		for (int y = 0; y < items.height; y++) {
			for (int x = 0; x < items.width; x++) {
				ItemCooldownManager stack = items.grid.get(Pair.of(x + items.minX, y + items.minY));
				a(x + (items.height - y - 1) * items.width,
						stack == null ? ItemCooldownManager.tick : stack.i());
			}
		}
	}

}
