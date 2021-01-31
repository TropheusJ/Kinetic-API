package com.simibubi.kinetic_api;

import com.simibubi.kinetic_api.foundation.data.CreateRegistrate;


import static com.simibubi.kinetic_api.AllTags.NameSpace.FORGE;
import static com.simibubi.kinetic_api.AllTags.NameSpace.MC;
import static com.simibubi.kinetic_api.AllTags.NameSpace.MOD;
import static com.simibubi.kinetic_api.AllTags.NameSpace.TIC;

import java.util.function.Function;

import com.simibubi.kinetic_api.foundation.utility.EmptyNamedTag;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import cut;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.ModList;

public class AllTags {
	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.baseCreativeTab);

	public static <T extends BeetrootsBlock, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BannerItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String tagName) {
		return b -> b.tag(forgeBlockTag(tagName))
			.item()
			.tag(forgeItemTag(tagName));
	}

	public static RequiredTagList.e<BeetrootsBlock> forgeBlockTag(String name) {
		return forgeTag(StatHandler::a, name);
	}

	public static RequiredTagList.e<HoeItem> forgeItemTag(String name) {
		return forgeTag(EntityTypeTags::a, name);
	}
	
	public static RequiredTagList.e<cut> forgeFluidTag(String name) {
		return forgeTag(BlockTags::a, name);
	}

	public static <T> RequiredTagList.e<T> forgeTag(Function<String, RequiredTagList.e<T>> wrapperFactory, String name) {
		return tag(wrapperFactory, "forge", name);
	}

	public static <T> RequiredTagList.e<T> tag(Function<String, RequiredTagList.e<T>> wrapperFactory, String domain, String name) {
		return wrapperFactory.apply(new Identifier(domain, name).toString());
	}

	public static enum NameSpace {

		MOD(Create.ID), FORGE("forge"), MC("minecraft"), TIC("tconstruct")

		;
		String id;

		private NameSpace(String id) {
			this.id = id;
		}
	}

	public static enum AllItemTags {
		CRUSHED_ORES(MOD),
		SEATS(MOD),
		VALVE_HANDLES(MOD),
		UPRIGHT_ON_BELT(MOD),
		CREATE_INGOTS(MOD),
		BEACON_PAYMENT(FORGE),
		INGOTS(FORGE),
		NUGGETS(FORGE),
		PLATES(FORGE),
		COBBLESTONE(FORGE)

		;

		public RequiredTagList.e<HoeItem> tag;

		private AllItemTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllItemTags(NameSpace namespace, String path) {
			tag = EntityTypeTags.a(
				new Identifier(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())).toString());
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.a(tag));
		}

		public boolean matches(ItemCooldownManager stack) {
			return tag.a(stack.b());
		}

		public void add(HoeItem... values) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.a(tag)
				.add(values));
		}

		public void includeIn(AllItemTags parent) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.a(parent.tag)
				.a(tag));
		}
	}
	
	public static enum AllFluidTags {
		NO_INFINITE_DRAINING
		
		;
		public RequiredTagList.e<cut> tag;
		
		private AllFluidTags() {
			this(MOD, "");
		}

		private AllFluidTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllFluidTags(NameSpace namespace, String path) {
			tag = BlockTags.createOptional(
				new Identifier(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())));
		}
		
		public boolean matches(cut fluid) {
			return fluid != null && fluid.a(tag);
		}
		
		static void loadClass() {
		}
	}

	public static enum AllBlockTags {
		WINDMILL_SAILS, FAN_HEATERS, WINDOWABLE, NON_MOVABLE, BRITTLE, SEATS, SAILS, VALVE_HANDLES, FAN_TRANSPARENT, SAFE_NBT, SLIMY_LOGS(TIC), BEACON_BASE_BLOCKS(MC)

		;

		public RequiredTagList.e<BeetrootsBlock> tag;

		private AllBlockTags() {
			this(MOD, "");
		}

		private AllBlockTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllBlockTags(NameSpace namespace, String path) {
			Identifier id = new Identifier(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name()));
			if (ModList.get().isLoaded(namespace.id)) {
				tag = StatHandler.a(id.toString());
				REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.a(tag));
			} else {
				tag = new EmptyNamedTag<>(id);
			}
		}

		public boolean matches(PistonHandler block) {
			return tag.a(block.b());
		}

		public void includeIn(AllBlockTags parent) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.a(parent.tag)
				.a(tag));
		}

		public void includeAll(RequiredTagList.e<BeetrootsBlock> child) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.a(tag)
				.a(child));
		}
		
		public void add(BeetrootsBlock ...values) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.a(tag).add(values));
		}
	}

	public static void register() {
		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.BEACON_PAYMENT);
		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.INGOTS);

		AllItemTags.UPRIGHT_ON_BELT.add(AliasedBlockItem.nw, AliasedBlockItem.nv, AliasedBlockItem.qj, AliasedBlockItem.qm);

		AllBlockTags.WINDMILL_SAILS.includeAll(StatHandler.b);
		
		AllBlockTags.BRITTLE.includeAll(StatHandler.p);
		AllBlockTags.BRITTLE.add(BellBlock.ev, BellBlock.mb, BellBlock.eh);

		AllBlockTags.FAN_TRANSPARENT.includeAll(StatHandler.M);
		AllBlockTags.FAN_TRANSPARENT.add(BellBlock.dH);

		AllBlockTags.FAN_HEATERS.add(BellBlock.iJ, BellBlock.me, BellBlock.B, BellBlock.bN);
		
		AllFluidTags.loadClass();
	}
}
