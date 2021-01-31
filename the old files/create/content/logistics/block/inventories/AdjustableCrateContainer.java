package com.simibubi.kinetic_api.content.logistics.block.inventories;

import bfs;
import com.simibubi.kinetic_api.AllContainerTypes;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AdjustableCrateContainer extends FoodComponent {

	public AdjustableCrateTileEntity te;
	public bfs playerInventory;
	public boolean doubleCrate;

	public AdjustableCrateContainer(int id, bfs inv, PacketByteBuf extraData) {
		super(AllContainerTypes.FLEXCRATE.type, id);
		DragonHeadEntityModel world = KeyBinding.B().r;
		BeehiveBlockEntity tileEntity = world.c(extraData.readBlockPos());
		this.playerInventory = inv;
		if (tileEntity instanceof AdjustableCrateTileEntity) {
			this.te = (AdjustableCrateTileEntity) tileEntity;
			this.te.handleUpdateTag(te.p(), extraData.readCompoundTag());
			init();
		}
	}

	public AdjustableCrateContainer(int id, bfs inv, AdjustableCrateTileEntity te) {
		super(AllContainerTypes.FLEXCRATE.type, id);
		this.te = te;
		this.playerInventory = inv;
		init();
	}

	private void init() {
		doubleCrate = te.isDoubleCrate();
		int x = doubleCrate ? 51 : 123;
		int maxCol = doubleCrate ? 8 : 4;
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < maxCol; ++col) {
				this.a(new SlotItemHandler(te.inventory, col + row * maxCol, x + col * 18, 20 + row * 18));
			}
		}

		// player Slots
		int xOffset = 58;
		int yOffset = 155;
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.a(new ShulkerBoxScreenHandler(playerInventory, col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.a(new ShulkerBoxScreenHandler(playerInventory, hotbarSlot, xOffset + hotbarSlot * 18, yOffset + 58));
		}

		c();
	}

	@Override
	public ItemCooldownManager b(PlayerAbilities playerIn, int index) {
		ShulkerBoxScreenHandler clickedSlot = a(index);
		if (!clickedSlot.f())
			return ItemCooldownManager.tick;

		ItemCooldownManager stack = clickedSlot.e();
		int crateSize = doubleCrate ? 32 : 16;
		if (index < crateSize) {
			a(stack, crateSize, hunger.size(), false);
			te.inventory.onContentsChanged(index);
		} else
			a(stack, 0, crateSize - 1, false);

		return ItemCooldownManager.tick;
	}

	@Override
	public boolean a(PlayerAbilities playerIn) {
		return true;
	}

}
