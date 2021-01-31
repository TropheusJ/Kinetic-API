package com.simibubi.kinetic_api.content.logistics.item.filter;

import bfs;
import com.simibubi.kinetic_api.AllContainerTypes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FilterContainer extends AbstractFilterContainer {

	boolean respectNBT;
	boolean blacklist;

	public FilterContainer(int id, bfs inv, PacketByteBuf extraData) {
		super(AllContainerTypes.FILTER.type, id, inv, extraData);
	}

	public FilterContainer(int id, bfs inv, ItemCooldownManager stack) {
		super(AllContainerTypes.FILTER.type, id, inv, stack);
	}

	@Override
	protected void addFilterSlots() {
		int x = 23;
		int y = 20;

		for (int row = 0; row < 2; ++row)
			for (int col = 0; col < 9; ++col)
				this.a(new SlotItemHandler(filterInventory, col + row * 9, x + col * 18, y + row * 18));
	}
	
	@Override
	protected ItemStackHandler createFilterInventory() {
		return FilterItem.getFilterItems(filterItem);
	}

	@Override
	protected int getInventoryOffset() {
		return 97;
	}
	
	@Override
	protected void readData(ItemCooldownManager filterItem) {
		CompoundTag tag = filterItem.p();
		respectNBT = tag.getBoolean("RespectNBT");
		blacklist = tag.getBoolean("Blacklist");
	}
	
	@Override
	protected void saveData(ItemCooldownManager filterItem) {
		CompoundTag tag = filterItem.p();
		tag.putBoolean("RespectNBT", respectNBT);
		tag.putBoolean("Blacklist", blacklist);
	}

}
