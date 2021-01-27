package com.simibubi.create.content.schematics.block;

import bfs;
import com.simibubi.create.AllContainerTypes;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SchematicannonContainer extends FoodComponent {

	private SchematicannonTileEntity te;
	private PlayerAbilities player;

	public SchematicannonContainer(int id, bfs inv, PacketByteBuf buffer) {
		super(AllContainerTypes.SCHEMATICANNON.type, id);
		player = inv.e;
		DragonHeadEntityModel world = KeyBinding.B().r;
		BeehiveBlockEntity tileEntity = world.c(buffer.readBlockPos());
		if (tileEntity instanceof SchematicannonTileEntity) {
			this.te = (SchematicannonTileEntity) tileEntity;
			this.te.handleUpdateTag(te.p(), buffer.readCompoundTag());
			init();
		}
	}

	public SchematicannonContainer(int id, bfs inv, SchematicannonTileEntity te) {
		super(AllContainerTypes.SCHEMATICANNON.type, id);
		player = inv.e;
		this.te = te;
		init();
	}

	protected void init() {
		int x = 20;
		int y = 0;

		a(new SlotItemHandler(te.inventory, 0, x + 15, y + 65));
		a(new SlotItemHandler(te.inventory, 1, x + 171, y + 65));
		a(new SlotItemHandler(te.inventory, 2, x + 134, y + 19));
		a(new SlotItemHandler(te.inventory, 3, x + 174, y + 19));
		a(new SlotItemHandler(te.inventory, 4, x + 15, y + 19));

		// player Slots
		for (int row = 0; row < 3; ++row) 
			for (int col = 0; col < 9; ++col) 
				a(new ShulkerBoxScreenHandler(player.bm, col + row * 9 + 9, -2 + col * 18, 163 + row * 18));
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) 
			a(new ShulkerBoxScreenHandler(player.bm, hotbarSlot, -2 + hotbarSlot * 18, 221));

		c();
	}

	@Override
	public boolean a(PlayerAbilities playerIn) {
		return true;
	}

	@Override
	public void b(PlayerAbilities playerIn) {
		super.b(playerIn);
	}

	public SchematicannonTileEntity getTileEntity() {
		return te;
	}

	@Override
	public ItemCooldownManager b(PlayerAbilities playerIn, int index) {
		ShulkerBoxScreenHandler clickedSlot = a(index);
		if (!clickedSlot.f())
			return ItemCooldownManager.tick;
		ItemCooldownManager stack = clickedSlot.e();

		if (index < 5) {
			a(stack, 5, hunger.size(), false);
		} else {
			if (a(stack, 0, 1, false) || a(stack, 2, 3, false)
					|| a(stack, 4, 5, false))
				;
		}

		return ItemCooldownManager.tick;
	}

}
