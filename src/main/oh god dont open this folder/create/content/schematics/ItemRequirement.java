package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;
import net.minecraft.state.property.Property;
import apx;
import bck;
import bcm;
import bhk;
import com.simibubi.create.foundation.utility.BlockHelper;

public class ItemRequirement {

	public enum ItemUseType {
		CONSUME, DAMAGE
	}

	ItemUseType usage;
	List<ItemCooldownManager> requiredItems;

	public static ItemRequirement INVALID = new ItemRequirement();
	public static ItemRequirement NONE = new ItemRequirement();

	private ItemRequirement() {
	}

	public ItemRequirement(ItemUseType usage, HoeItem item) {
		this(usage, Arrays.asList(new ItemCooldownManager(item)));
	}

	public ItemRequirement(ItemUseType usage, List<ItemCooldownManager> requiredItems) {
		this.usage = usage;
		this.requiredItems = requiredItems;
	}

	public static ItemRequirement of(PistonHandler state) {
		BeetrootsBlock block = state.b();
		if (block == BellBlock.FACING)
			return NONE;
		if (block instanceof ISpecialBlockItemRequirement)
			return ((ISpecialBlockItemRequirement) block).getRequiredItems(state);

		HoeItem item = BannerItem.e.getOrDefault(state.b(), AliasedBlockItem.a);

		// double slab needs two items
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.aK) && state.c(BambooLeaves.aK) == Property.hashCodeCache)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemCooldownManager(item, 2)));
		if (block instanceof TrappedChestBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemCooldownManager(item, state.c(TrappedChestBlock.b).intValue())));
		if (block instanceof SandBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemCooldownManager(item, state.c(SandBlock.color).intValue())));
		if (block instanceof SlimeBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemCooldownManager(item, state.c(SlimeBlock.a).intValue())));
		if (block instanceof GlassBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemCooldownManager(AliasedBlockItem.i)));
		if (block instanceof BlockEntityProvider)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemCooldownManager(AliasedBlockItem.j)));

		return item == AliasedBlockItem.a ? INVALID : new ItemRequirement(ItemUseType.CONSUME, item);
	}

	public static ItemRequirement of(apx entity) {
		EntityDimensions<?> type = entity.W();

		if (entity instanceof ISpecialEntityItemRequirement)
			return ((ISpecialEntityItemRequirement) entity).getRequiredItems();

		if (type == EntityDimensions.M) {
			bcm ife = (bcm) entity;
			ItemCooldownManager frame = new ItemCooldownManager(AliasedBlockItem.oW);
			ItemCooldownManager displayedItem = ife.o();
			if (displayedItem.a())
				return new ItemRequirement(ItemUseType.CONSUME, AliasedBlockItem.oW);
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(frame, displayedItem));
		}

		if (type == EntityDimensions.ad)
			return new ItemRequirement(ItemUseType.CONSUME, AliasedBlockItem.lz);

		if (type == EntityDimensions.height) {
			List<ItemCooldownManager> requirements = new ArrayList<>();
			bck armorStandEntity = (bck) entity;
			armorStandEntity.bo().forEach(requirements::add);
			requirements.add(new ItemCooldownManager(AliasedBlockItem.pC));
			return new ItemRequirement(ItemUseType.CONSUME, requirements);
		}

		if (entity instanceof ScheduleBuilder) {
			ScheduleBuilder minecartEntity = (ScheduleBuilder) entity;
			return new ItemRequirement(ItemUseType.CONSUME, minecartEntity.getCartItem().b());
		}

		if (entity instanceof bhk) {
			bhk boatEntity = (bhk) entity;
			return new ItemRequirement(ItemUseType.CONSUME, boatEntity.g().getItem());
		}

		if (type == EntityDimensions.s)
			return new ItemRequirement(ItemUseType.CONSUME, AliasedBlockItem.qc);

		return INVALID;
	}

	public boolean isEmpty() {
		return NONE == this;
	}

	public boolean isInvalid() {
		return INVALID == this;
	}

	public List<ItemCooldownManager> getRequiredItems() {
		return requiredItems;
	}

	public ItemUseType getUsage() {
		return usage;
	}

	public static boolean validate(ItemCooldownManager required, ItemCooldownManager present) {
		return required.a() || required.b() == present.b();
	}

}
