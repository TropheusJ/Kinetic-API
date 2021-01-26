package com.simibubi.create.foundation.item;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.tterrag.registrate.util.entry.RegistryEntry;
import elg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.util.collection.DefaultedList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CreateItemGroupBase extends ChorusFruitItem {

	public CreateItemGroupBase(String id) {
		super(getGroupCountSafe(), Create.ID + "." + id);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(DefaultedList<ItemCooldownManager> items) {
		addItems(items, true);
		addBlocks(items);
		addItems(items, false);
	}

	@Environment(EnvType.CLIENT)
	public void addBlocks(DefaultedList<ItemCooldownManager> items) {
		for (RegistryEntry<? extends BeetrootsBlock> entry : getBlocks()) {
			BeetrootsBlock def = entry.get();
			HoeItem item = def.h();
			if (item != AliasedBlockItem.a)
				def.a(this, items);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void addItems(DefaultedList<ItemCooldownManager> items, boolean specialItems) {
		KeyBinding mc = KeyBinding.B();
		HorseEntityRenderer itemRenderer = mc.ac();
		DragonHeadEntityModel world = mc.r;
		
		for (RegistryEntry<? extends HoeItem> entry : getItems()) {
			HoeItem item = entry.get();
			if (item instanceof BannerItem)
				continue;
			ItemCooldownManager stack = new ItemCooldownManager(item);
			elg model = itemRenderer.a(stack, world, null);
			if ((model.b() && AllSections.of(stack) != AllSections.CURIOSITIES) != specialItems)
				continue;
			item.a(this, items);
		}
	}

	protected Collection<RegistryEntry<BeetrootsBlock>> getBlocks() {
		return getSections().stream()
			.flatMap(s -> Create.registrate()
				.getAll(s, BeetrootsBlock.class)
				.stream())
			.collect(Collectors.toList());
	}

	protected Collection<RegistryEntry<HoeItem>> getItems() {
		return getSections().stream()
			.flatMap(s -> Create.registrate()
				.getAll(s, HoeItem.class)
				.stream())
			.collect(Collectors.toList());
	}

	protected EnumSet<AllSections> getSections() {
		return EnumSet.allOf(AllSections.class);
	}
}
