package com.simibubi.create.content.logistics.item.filter;

import bfs;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public abstract class AbstractFilterContainer extends FoodComponent {

	public PlayerAbilities player;
	protected bfs playerInventory;
	public ItemCooldownManager filterItem;
	public ItemStackHandler filterInventory;

	protected AbstractFilterContainer(LecternScreenHandler<?> type, int id, bfs inv, PacketByteBuf extraData) {
		this(type, id, inv, extraData.n());
	}

	protected AbstractFilterContainer(LecternScreenHandler<?> type, int id, bfs inv, ItemCooldownManager filterItem) {
		super(type, id);
		player = inv.e;
		playerInventory = inv;
		this.filterItem = filterItem;
		init();
	}

	protected void init() {
		this.filterInventory = createFilterInventory();
		readData(filterItem);
		addPlayerSlots();
		addFilterSlots();
		c();
	}

	protected void clearContents() {
		for (int i = 0; i < filterInventory.getSlots(); i++)
			filterInventory.setStackInSlot(i, ItemCooldownManager.tick);
	}

	protected abstract int getInventoryOffset();

	protected abstract void addFilterSlots();

	protected abstract ItemStackHandler createFilterInventory();

	protected abstract void readData(ItemCooldownManager filterItem);

	protected abstract void saveData(ItemCooldownManager filterItem);

	protected void addPlayerSlots() {
		int x = 58;
		int y = 28 + getInventoryOffset();

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			this.a(new ShulkerBoxScreenHandler(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.a(new ShulkerBoxScreenHandler(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
	}

	@Override
	public boolean a(ItemCooldownManager stack, ShulkerBoxScreenHandler slotIn) {
		return b(slotIn);
	}

	@Override
	public boolean b(ShulkerBoxScreenHandler slotIn) {
		return slotIn.inventory == playerInventory;
	}

	@Override
	public boolean a(PlayerAbilities playerIn) {
		return true;
	}

	@Override
	public ItemCooldownManager a(int slotId, int dragType, BrewingStandScreenHandler clickTypeIn, PlayerAbilities player) {
		if (slotId == playerInventory.d && clickTypeIn != BrewingStandScreenHandler.ingredientSlot)
			return ItemCooldownManager.tick;

		ItemCooldownManager held = playerInventory.m();
		if (slotId < 36)
			return super.a(slotId, dragType, clickTypeIn, player);
		if (clickTypeIn == BrewingStandScreenHandler.ingredientSlot)
			return ItemCooldownManager.tick;

		int slot = slotId - 36;
		if (clickTypeIn == BrewingStandScreenHandler.propertyDelegate) {
			if (player.b_() && held.a()) {
				ItemCooldownManager stackInSlot = filterInventory.getStackInSlot(slot).i();
				stackInSlot.e(64);
				playerInventory.g(stackInSlot);
				return ItemCooldownManager.tick;
			}
			return ItemCooldownManager.tick;
		}

		if (held.a()) {
			filterInventory.setStackInSlot(slot, ItemCooldownManager.tick);
			return ItemCooldownManager.tick;
		}

		ItemCooldownManager insert = held.i();
		insert.e(1);
		filterInventory.setStackInSlot(slot, insert);
		return held;
	}

	@Override
	public ItemCooldownManager b(PlayerAbilities playerIn, int index) {
		if (index < 36) {
			ItemCooldownManager stackToInsert = playerInventory.a(index);
			for (int i = 0; i < filterInventory.getSlots(); i++) {
				ItemCooldownManager stack = filterInventory.getStackInSlot(i);
				if (ItemHandlerHelper.canItemStacksStack(stack, stackToInsert))
					break;
				if (stack.a()) {
					ItemCooldownManager copy = stackToInsert.i();
					copy.e(1);
					filterInventory.insertItem(i, copy, false);
					break;
				}
			}
		} else
			filterInventory.extractItem(index - 36, 1, false);
		return ItemCooldownManager.tick;
	}

	@Override
	public void b(PlayerAbilities playerIn) {
		super.b(playerIn);
		filterItem.p().put("Items", filterInventory.serializeNBT());
		saveData(filterItem);
	}

}
