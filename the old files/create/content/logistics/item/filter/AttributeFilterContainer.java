package com.simibubi.kinetic_api.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.List;
import bfs;
import com.simibubi.kinetic_api.AllContainerTypes;
import com.simibubi.kinetic_api.foundation.utility.Pair;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AttributeFilterContainer extends AbstractFilterContainer {

	public enum WhitelistMode {
		WHITELIST_DISJ, WHITELIST_CONJ, BLACKLIST;
	}

	WhitelistMode whitelistMode;
	List<Pair<ItemAttribute, Boolean>> selectedAttributes;

	public AttributeFilterContainer(int id, bfs inv, PacketByteBuf extraData) {
		super(AllContainerTypes.ATTRIBUTE_FILTER.type, id, inv, extraData);
	}

	public AttributeFilterContainer(int id, bfs inv, ItemCooldownManager stack) {
		super(AllContainerTypes.ATTRIBUTE_FILTER.type, id, inv, stack);
	}

	public void appendSelectedAttribute(ItemAttribute itemAttribute, boolean inverted) {
		selectedAttributes.add(Pair.of(itemAttribute, inverted));
	}

	@Override
	protected void clearContents() {
		selectedAttributes.clear();
	}

	@Override
	protected void init() {
		super.init();
		ItemCooldownManager stack = new ItemCooldownManager(AliasedBlockItem.pI);
		stack.a(
			new LiteralText("Selected Tags").formatted(Formatting.RESET, Formatting.BLUE));
		filterInventory.setStackInSlot(1, stack);
	}

	@Override
	protected ItemStackHandler createFilterInventory() {
		return new ItemStackHandler(2);
	}

	protected void addFilterSlots() {
		this.a(new SlotItemHandler(filterInventory, 0, 16, 22));
		this.a(new SlotItemHandler(filterInventory, 1, 22, 57) {
			@Override
			public boolean a(PlayerAbilities playerIn) {
				return false;
			}
		});
	}

	@Override
	public ItemCooldownManager a(int slotId, int dragType, BrewingStandScreenHandler clickTypeIn, PlayerAbilities player) {
		if (slotId == 37)
			return ItemCooldownManager.tick;
		return super.a(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public boolean b(ShulkerBoxScreenHandler slotIn) {
		if (slotIn.d == 37)
			return false;
		return super.b(slotIn);
	}

	@Override
	public boolean a(ItemCooldownManager stack, ShulkerBoxScreenHandler slotIn) {
		if (slotIn.d == 37)
			return false;
		return super.a(stack, slotIn);
	}

	@Override
	public ItemCooldownManager b(PlayerAbilities playerIn, int index) {
		if (index == 37)
			return ItemCooldownManager.tick;
		if (index == 36) {
			filterInventory.setStackInSlot(37, ItemCooldownManager.tick);
			return ItemCooldownManager.tick;
		}
		if (index < 36) {
			ItemCooldownManager stackToInsert = playerInventory.a(index);
			ItemCooldownManager copy = stackToInsert.i();
			copy.e(1);
			filterInventory.setStackInSlot(0, copy);
		}
		return ItemCooldownManager.tick;
	}

	@Override
	protected int getInventoryOffset() {
		return 83;
	}

	@Override
	protected void readData(ItemCooldownManager filterItem) {
		selectedAttributes = new ArrayList<>();
		whitelistMode = WhitelistMode.values()[filterItem.p()
			.getInt("WhitelistMode")];
		ListTag attributes = filterItem.p()
			.getList("MatchedAttributes", NBT.TAG_COMPOUND);
		attributes.forEach(inbt -> {
			CompoundTag compound = (CompoundTag) inbt;
			selectedAttributes.add(Pair.of(ItemAttribute.fromNBT(compound), compound.getBoolean("Inverted")));
		});
	}

	@Override
	protected void saveData(ItemCooldownManager filterItem) {
		filterItem.p()
			.putInt("WhitelistMode", whitelistMode.ordinal());
		ListTag attributes = new ListTag();
		selectedAttributes.forEach(at -> {
			if (at == null)
				return;
			CompoundTag compoundNBT = new CompoundTag();
			at.getFirst().serializeNBT(compoundNBT);
			compoundNBT.putBoolean("Inverted", at.getSecond());
			attributes.add(compoundNBT);
		});
		filterItem.p()
			.put("MatchedAttributes", attributes);
	}

}
