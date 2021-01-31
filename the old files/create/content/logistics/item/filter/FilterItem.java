package com.simibubi.kinetic_api.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import bfs;
import bnx;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.kinetic_api.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.kinetic_api.foundation.item.ItemDescription;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class FilterItem extends HoeItem implements ActionResult {

	private FilterType type;

	private enum FilterType {
		REGULAR, ATTRIBUTE;
	}

	public static FilterItem regular(a properties) {
		return new FilterItem(FilterType.REGULAR, properties);
	}

	public static FilterItem attribute(a properties) {
		return new FilterItem(FilterType.ATTRIBUTE, properties);
	}

	private FilterItem(FilterType type, a properties) {
		super(properties);
		this.type = type;
	}

	@Nonnull
	@Override
	public Difficulty a(bnx context) {
		if (context.n() == null)
			return Difficulty.PASS;
		return a(context.p(), context.n(), context.o()).getGlobalDifficulty();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(ItemCooldownManager stack, GameMode worldIn, List<Text> tooltip, ToolItem flagIn) {
		if (!AllKeys.shiftDown()) {
			List<Text> makeSummary = makeSummary(stack);
			if (makeSummary.isEmpty())
				return;
			ItemDescription.add(tooltip, new LiteralText(" "));
			ItemDescription.add(tooltip, makeSummary);
		}
	}

	private List<Text> makeSummary(ItemCooldownManager filter) {
		List<Text> list = new ArrayList<>();

		if (type == FilterType.REGULAR) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean blacklist = filter.p()
				.getBoolean("Blacklist");

			list.add((blacklist ? Lang.translate("gui.filter.deny_list") : Lang.translate("gui.filter.allow_list")).formatted(Formatting.GOLD));
			int count = 0;
			for (int i = 0; i < filterItems.getSlots(); i++) {
				if (count > 3) {
					list.add(new LiteralText("- ...").formatted(Formatting.DARK_GRAY));
					break;
				}

				ItemCooldownManager filterStack = filterItems.getStackInSlot(i);
				if (filterStack.a())
					continue;
				list.add(new LiteralText("- ").append(filterStack.r()).formatted(Formatting.GRAY));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.ATTRIBUTE) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.p()
				.getInt("WhitelistMode")];
			list.add((whitelistMode == WhitelistMode.WHITELIST_CONJ
				? Lang.translate("gui.attribute_filter.allow_list_conjunctive")
				: whitelistMode == WhitelistMode.WHITELIST_DISJ
					? Lang.translate("gui.attribute_filter.allow_list_disjunctive")
					: Lang.translate("gui.attribute_filter.deny_list")).formatted(Formatting.GOLD));

			int count = 0;
			ListTag attributes = filter.p()
				.getList("MatchedAttributes", NBT.TAG_COMPOUND);
			for (Tag inbt : attributes) {
				CompoundTag compound = (CompoundTag) inbt;
				ItemAttribute attribute = ItemAttribute.fromNBT(compound);
				boolean inverted = compound.getBoolean("Inverted");
				if (count > 3) {
					list.add(new LiteralText("- ...").formatted(Formatting.DARK_GRAY));
					break;
				}
				list.add(new LiteralText("- ").append(attribute.format(inverted)));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		return list;
	}

	@Override
	public LocalDifficulty<ItemCooldownManager> a(GameMode world, PlayerAbilities player, ItemScatterer hand) {
		ItemCooldownManager heldItem = player.b(hand);

		if (!player.bt() && hand == ItemScatterer.RANDOM) {
			if (!world.v && player instanceof ServerPlayerEntity)
				NetworkHooks.openGui((ServerPlayerEntity) player, this, buf -> {
					buf.a(heldItem);
				});
			return LocalDifficulty.a(heldItem);
		}
		return LocalDifficulty.c(heldItem);
	}

	@Override
	public FoodComponent createMenu(int id, bfs inv, PlayerAbilities player) {
		ItemCooldownManager heldItem = player.dC();
		if (type == FilterType.REGULAR)
			return new FilterContainer(id, inv, heldItem);
		if (type == FilterType.ATTRIBUTE)
			return new AttributeFilterContainer(id, inv, heldItem);
		return null;
	}

	@Override
	public Text d() {
		return new LiteralText(a());
	}

	public static ItemStackHandler getFilterItems(ItemCooldownManager stack) {
		ItemStackHandler newInv = new ItemStackHandler(18);
		if (AllItems.FILTER.get() != stack.b())
			throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
		CompoundTag invNBT = stack.a("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static boolean test(GameMode world, ItemCooldownManager stack, ItemCooldownManager filter) {
		return test(world, stack, filter, false);
	}

	public static boolean test(GameMode world, FluidStack stack, ItemCooldownManager filter) {
		return test(world, stack, filter, true);
	}

	private static boolean test(GameMode world, ItemCooldownManager stack, ItemCooldownManager filter, boolean matchNBT) {
		if (filter.a())
			return true;

		if (!(filter.b() instanceof FilterItem))
			return (matchNBT ? ItemHandlerHelper.canItemStacksStack(filter, stack)
				: ItemCooldownManager.c(filter, stack));

		if (AllItems.FILTER.get() == filter.b()) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean respectNBT = filter.p()
				.getBoolean("RespectNBT");
			boolean blacklist = filter.p()
				.getBoolean("Blacklist");
			for (int slot = 0; slot < filterItems.getSlots(); slot++) {
				ItemCooldownManager stackInSlot = filterItems.getStackInSlot(slot);
				if (stackInSlot.a())
					continue;
				boolean matches = test(world, stack, stackInSlot, respectNBT);
				if (matches)
					return !blacklist;
			}
			return blacklist;
		}

		if (AllItems.ATTRIBUTE_FILTER.get() == filter.b()) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.p()
				.getInt("WhitelistMode")];
			ListTag attributes = filter.p()
				.getList("MatchedAttributes", NBT.TAG_COMPOUND);
			for (Tag inbt : attributes) {
				CompoundTag compound = (CompoundTag) inbt;
				ItemAttribute attribute = ItemAttribute.fromNBT(compound);
				boolean matches = attribute.appliesTo(stack, world) != compound.getBoolean("Inverted");

				if (matches) {
					switch (whitelistMode) {
					case BLACKLIST:
						return false;
					case WHITELIST_CONJ:
						continue;
					case WHITELIST_DISJ:
						return true;
					}
				} else {
					switch (whitelistMode) {
					case BLACKLIST:
						continue;
					case WHITELIST_CONJ:
						return false;
					case WHITELIST_DISJ:
						continue;
					}
				}
			}

			switch (whitelistMode) {
			case BLACKLIST:
				return true;
			case WHITELIST_CONJ:
				return true;
			case WHITELIST_DISJ:
				return false;
			}
		}

		return false;
	}

	private static boolean test(GameMode world, FluidStack stack, ItemCooldownManager filter, boolean matchNBT) {
		if (filter.a())
			return true;
		if (stack.isEmpty())
			return false;

		if (!(filter.b() instanceof FilterItem)) {
			if (!EmptyingByBasin.canItemBeEmptied(world, filter))
				return false;
			FluidStack fluidInFilter = EmptyingByBasin.emptyItem(world, filter, true)
				.getFirst();
			if (fluidInFilter == null)
				return false;
			if (!matchNBT)
				return fluidInFilter.getFluid()
					.a(stack.getFluid());
			boolean fluidEqual = fluidInFilter.isFluidEqual(stack);
			return fluidEqual;
		}

		if (AllItems.FILTER.get() == filter.b()) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean respectNBT = filter.p()
				.getBoolean("RespectNBT");
			boolean blacklist = filter.p()
				.getBoolean("Blacklist");
			for (int slot = 0; slot < filterItems.getSlots(); slot++) {
				ItemCooldownManager stackInSlot = filterItems.getStackInSlot(slot);
				if (stackInSlot.a())
					continue;
				boolean matches = test(world, stack, stackInSlot, respectNBT);
				if (matches)
					return !blacklist;
			}
			return blacklist;
		}
		return false;
	}

}
