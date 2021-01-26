package com.simibubi.create.content.schematics.block;

import bfs;
import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.AllItems;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SchematicTableContainer extends FoodComponent {

	private SchematicTableTileEntity te;
	private ShulkerBoxScreenHandler inputSlot;
	private ShulkerBoxScreenHandler outputSlot;
	private PlayerAbilities player;

	public SchematicTableContainer(int id, bfs inv, PacketByteBuf extraData) {
		super(AllContainerTypes.SCHEMATIC_TABLE.type, id);
		player = inv.e;
		DragonHeadEntityModel world = KeyBinding.B().r;
		BeehiveBlockEntity tileEntity = world.c(extraData.readBlockPos());
		if (tileEntity instanceof SchematicTableTileEntity) {
			this.te = (SchematicTableTileEntity) tileEntity;
			this.te.handleUpdateTag(te.p(), extraData.readCompoundTag());
			init();
		}
	}

	public SchematicTableContainer(int id, bfs inv, SchematicTableTileEntity te) {
		super(AllContainerTypes.SCHEMATIC_TABLE.type, id);
		this.player = inv.e;
		this.te = te;
		init();
	}

	protected void init() {
		inputSlot = new SlotItemHandler(te.inventory, 0, -35, 41) {
			@Override
			public boolean a(ItemCooldownManager stack) {
				return AllItems.EMPTY_SCHEMATIC.isIn(stack) || AllItems.SCHEMATIC_AND_QUILL.isIn(stack)
					|| AllItems.SCHEMATIC.isIn(stack);
			}
		};

		outputSlot = new SlotItemHandler(te.inventory, 1, 110, 41) {
			@Override
			public boolean a(ItemCooldownManager stack) {
				return false;
			}
		};

		a(inputSlot);
		a(outputSlot);

		// player Slots
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.a(new ShulkerBoxScreenHandler(player.bm, col + row * 9 + 9, 12 + col * 18, 102 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.a(new ShulkerBoxScreenHandler(player.bm, hotbarSlot, 12 + hotbarSlot * 18, 160));
		}

		c();
	}

	public boolean canWrite() {
		return inputSlot.f() && !outputSlot.f();
	}

	@Override
	public boolean a(PlayerAbilities playerIn) {
		return true;
	}

	@Override
	public ItemCooldownManager b(PlayerAbilities playerIn, int index) {
		ShulkerBoxScreenHandler clickedSlot = a(index);
		if (!clickedSlot.f())
			return ItemCooldownManager.tick;

		ItemCooldownManager stack = clickedSlot.e();
		if (index < 2)
			a(stack, 2, hunger.size(), false);
		else
			a(stack, 0, 1, false);

		return ItemCooldownManager.tick;
	}

	public SchematicTableTileEntity getTileEntity() {
		return te;
	}

}
