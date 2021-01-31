package com.simibubi.kinetic_api.content.logistics.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class CardboardBoxItem extends HoeItem {

	static final int SLOTS = 9;
	static final List<CardboardBoxItem> ALL_BOXES = new ArrayList<>();

	public CardboardBoxItem(a properties) {
		super(properties);
		ALL_BOXES.add(this);
	}

	@Override
	public LocalDifficulty<ItemCooldownManager> a(GameMode worldIn, PlayerAbilities playerIn, ItemScatterer handIn) {
		if (!playerIn.bt())
			return super.a(worldIn, playerIn, handIn);

		ItemCooldownManager box = playerIn.b(handIn);
		for (ItemCooldownManager stack : getContents(box))
			playerIn.bm.a(worldIn, stack);

		if (!playerIn.b_()) {
			box.g(1);
		}
		return new LocalDifficulty<>(Difficulty.SUCCESS, box);
	}

	public static ItemCooldownManager containing(List<ItemCooldownManager> stacks) {
		ItemCooldownManager box = new ItemCooldownManager(randomBox());
		CompoundTag compound = new CompoundTag();

		DefaultedList<ItemCooldownManager> list = DefaultedList.of();
		list.addAll(stacks);
		Clearable.a(compound, list);

		box.c(compound);
		return box;
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {
	}
	
	public static void addAddress(ItemCooldownManager box, String address) {
		box.p().putString("Address", address);
	}

	public static boolean matchAddress(ItemCooldownManager box, String other) {
		String address = box.o().getString("Address");
		if (address == null || address.isEmpty())
			return false;
		if (address.equals("*"))
			return true;
		if (address.equals(other))
			return true;
		if (address.endsWith("*") && other.startsWith(address.substring(0, address.length() - 1)))
			return true;

		return false;
	}

	public static List<ItemCooldownManager> getContents(ItemCooldownManager box) {
		DefaultedList<ItemCooldownManager> list = DefaultedList.ofSize(SLOTS, ItemCooldownManager.tick);
		Clearable.b(box.p(), list);
		return list;
	}

	public static CardboardBoxItem randomBox() {
		return ALL_BOXES.get(new Random().nextInt(ALL_BOXES.size()));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(ItemCooldownManager stack, GameMode worldIn, List<Text> tooltip, ToolItem flagIn) {
		super.a(stack, worldIn, tooltip, flagIn);
		CompoundTag compoundnbt = stack.p();

		if (compoundnbt.contains("Address", Constants.NBT.TAG_STRING)) {
			tooltip.add(new LiteralText("-> " + compoundnbt.getString("Address"))
					.formatted(Formatting.GOLD));
		}

		if (!compoundnbt.contains("Items", Constants.NBT.TAG_LIST))
			return;

		int i = 0;
		int j = 0;

		for (ItemCooldownManager itemstack : getContents(stack)) {
			if (itemstack.a())
				continue;

			++j;
			if (i <= 4) {
				++i;
				Text itextcomponent = itemstack.r();
				tooltip.add(itextcomponent.copy().append(" x").append(String.valueOf(itemstack.E()))
					.formatted(Formatting.GRAY));
			}
		}

		if (j - i > 0) {
			tooltip.add((new TranslatableText("container.shulkerBox.more", j - i))
					.formatted(Formatting.ITALIC));
		}
	}

}
