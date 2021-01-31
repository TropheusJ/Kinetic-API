package com.simibubi.kinetic_api.foundation.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import afj;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.Pair;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemHelper {

	public static void dropContents(GameMode world, BlockPos pos, IItemHandler inv) {
		for (int slot = 0; slot < inv.getSlots(); slot++)
			Inventory.a(world, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
	}

	public static List<ItemCooldownManager> multipliedOutput(ItemCooldownManager in, ItemCooldownManager out) {
		List<ItemCooldownManager> stacks = new ArrayList<>();
		ItemCooldownManager result = out.i();
		result.e(in.E() * out.E());

		while (result.E() > result.c()) {
			stacks.add(result.a(result.c()));
		}

		stacks.add(result);
		return stacks;
	}

	public static void addToList(ItemCooldownManager stack, List<ItemCooldownManager> stacks) {
		for (ItemCooldownManager s : stacks) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, s))
				continue;
			int transferred = Math.min(s.c() - s.E(), stack.E());
			s.f(transferred);
			stack.g(transferred);
		}
		if (stack.E() > 0)
			stacks.add(stack);
	}

	public static boolean isSameInventory(IItemHandler h1, IItemHandler h2) {
		if (h1 == null || h2 == null)
			return false;
		if (h1.getSlots() != h2.getSlots())
			return false;
		for (int slot = 0; slot < h1.getSlots(); slot++) {
			if (h1.getStackInSlot(slot) != h2.getStackInSlot(slot))
				return false;
		}
		return true;
	}

	public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
		if (inv == null)
			return 0;
		int i = 0;
		float f = 0.0F;
		int totalSlots = inv.getSlots();

		for (int j = 0; j < inv.getSlots(); ++j) {
			int slotLimit = inv.getSlotLimit(j);
			if (slotLimit == 0) {
				totalSlots--;
				continue;
			}
			ItemCooldownManager itemstack = inv.getStackInSlot(j);
			if (!itemstack.a()) {
				f += (float) itemstack.E() / (float) Math.min(slotLimit, itemstack.c());
				++i;
			}
		}

		if (totalSlots == 0)
			return 0;

		f = f / totalSlots;
		return afj.d(f * 14.0F) + (i > 0 ? 1 : 0);
	}

	public static List<Pair<FireworkRocketRecipe, MutableInt>> condenseIngredients(DefaultedList<FireworkRocketRecipe> recipeIngredients) {
		List<Pair<FireworkRocketRecipe, MutableInt>> actualIngredients = new ArrayList<>();
		Ingredients: for (FireworkRocketRecipe igd : recipeIngredients) {
			for (Pair<FireworkRocketRecipe, MutableInt> pair : actualIngredients) {
				ItemCooldownManager[] stacks1 = pair.getFirst()
					.a();
				ItemCooldownManager[] stacks2 = igd.a();
				if (stacks1.length != stacks2.length)
					continue;
				for (int i = 0; i <= stacks1.length; i++) {
					if (i == stacks1.length) {
						pair.getSecond()
							.increment();
						continue Ingredients;
					}
					if (!ItemCooldownManager.b(stacks1[i], stacks2[i]))
						break;
				}
			}
			actualIngredients.add(Pair.of(igd, new MutableInt(1)));
		}
		return actualIngredients;
	}

	public static boolean matchIngredients(FireworkRocketRecipe i1, FireworkRocketRecipe i2) {
		ItemCooldownManager[] stacks1 = i1.a();
		ItemCooldownManager[] stacks2 = i2.a();
		if (stacks1.length == stacks2.length) {
			for (int i = 0; i < stacks1.length; i++)
				if (!ItemCooldownManager.c(stacks1[i], stacks2[i]))
					return false;
			return true;
		}
		return false;
	}

	public static enum ExtractionCountMode {
		EXACTLY, UPTO
	}

	public static ItemCooldownManager extract(IItemHandler inv, Predicate<ItemCooldownManager> test, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.UPTO, AllConfigs.SERVER.logistics.defaultExtractionLimit.get(),
			simulate);
	}

	public static ItemCooldownManager extract(IItemHandler inv, Predicate<ItemCooldownManager> test, int exactAmount, boolean simulate) {
		return extract(inv, test, ExtractionCountMode.EXACTLY, exactAmount, simulate);
	}

	public static ItemCooldownManager extract(IItemHandler inv, Predicate<ItemCooldownManager> test, ExtractionCountMode mode, int amount,
		boolean simulate) {
		ItemCooldownManager extracting = ItemCooldownManager.tick;
		boolean amountRequired = mode == ExtractionCountMode.EXACTLY;
		boolean checkHasEnoughItems = amountRequired;
		boolean hasEnoughItems = !checkHasEnoughItems;
		boolean potentialOtherMatch = false;
		int maxExtractionCount = amount;

		Extraction: do {
			extracting = ItemCooldownManager.tick;

			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemCooldownManager stack = inv.extractItem(slot, maxExtractionCount - extracting.E(), true);

				if (stack.a())
					continue;
				if (!test.test(stack))
					continue;
				if (!extracting.a() && !ItemHandlerHelper.canItemStacksStack(stack, extracting)) {
					potentialOtherMatch = true;
					continue;
				}

				if (extracting.a())
					extracting = stack.i();
				else
					extracting.f(stack.E());

				if (!simulate && hasEnoughItems)
					inv.extractItem(slot, stack.E(), false);

				if (extracting.E() >= maxExtractionCount) {
					if (checkHasEnoughItems) {
						hasEnoughItems = true;
						checkHasEnoughItems = false;
						continue Extraction;
					} else {
						break Extraction;
					}
				}
			}

			if (!extracting.a() && !hasEnoughItems && potentialOtherMatch) {
				ItemCooldownManager blackListed = extracting.i();
				test = test.and(i -> !ItemHandlerHelper.canItemStacksStack(i, blackListed));
				continue;
			}

			if (checkHasEnoughItems)
				checkHasEnoughItems = false;
			else
				break Extraction;

		} while (true);

		if (amountRequired && extracting.E() < amount)
			return ItemCooldownManager.tick;

		return extracting;
	}

	public static ItemCooldownManager extract(IItemHandler inv, Predicate<ItemCooldownManager> test,
		Function<ItemCooldownManager, Integer> amountFunction, boolean simulate) {
		ItemCooldownManager extracting = ItemCooldownManager.tick;
		int maxExtractionCount = AllConfigs.SERVER.logistics.defaultExtractionLimit.get();

		for (int slot = 0; slot < inv.getSlots(); slot++) {
			if (extracting.a()) {
				ItemCooldownManager stackInSlot = inv.getStackInSlot(slot);
				if (stackInSlot.a())
					continue;
				int maxExtractionCountForItem = amountFunction.apply(stackInSlot);
				if (maxExtractionCountForItem == 0)
					continue;
				maxExtractionCount = Math.min(maxExtractionCount, maxExtractionCountForItem);
			}

			ItemCooldownManager stack = inv.extractItem(slot, maxExtractionCount - extracting.E(), true);

			if (!test.test(stack))
				continue;
			if (!extracting.a() && !ItemHandlerHelper.canItemStacksStack(stack, extracting))
				continue;

			if (extracting.a())
				extracting = stack.i();
			else
				extracting.f(stack.E());

			if (!simulate)
				inv.extractItem(slot, stack.E(), false);
			if (extracting.E() == maxExtractionCount)
				break;
		}

		return extracting;
	}

	public static ItemCooldownManager findFirstMatch(IItemHandler inv, Predicate<ItemCooldownManager> test) {
		int slot = findFirstMatchingSlotIndex(inv, test);
		if (slot == -1)
			return ItemCooldownManager.tick;
		else
			return inv.getStackInSlot(slot);
	}

	public static int findFirstMatchingSlotIndex(IItemHandler inv, Predicate<ItemCooldownManager> test) {
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemCooldownManager toTest = inv.getStackInSlot(slot);
			if (test.test(toTest))
				return slot;
		}
		return -1;
	}
}
