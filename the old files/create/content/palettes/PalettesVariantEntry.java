package com.simibubi.kinetic_api.content.palettes;

import static com.simibubi.kinetic_api.foundation.data.CreateRegistrate.connectedTextures;

import com.google.common.collect.ImmutableList;
import com.simibubi.kinetic_api.AllColorHandlers;
import com.simibubi.kinetic_api.AllTags;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.data.CreateRegistrate;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.client.render.VertexConsumerProvider;

public class PalettesVariantEntry {

	public ImmutableList<BlockEntry<? extends BeetrootsBlock>> registeredBlocks;
	public ImmutableList<BlockEntry<? extends BeetrootsBlock>> registeredPartials;

	public PalettesVariantEntry(PaletteStoneVariants variant, PaletteBlockPatterns[] patterns,
		NonNullSupplier<? extends BeetrootsBlock> initialProperties) {

		String name = Lang.asId(variant.name());
		ImmutableList.Builder<BlockEntry<? extends BeetrootsBlock>> registeredBlocks = ImmutableList.builder();
		ImmutableList.Builder<BlockEntry<? extends BeetrootsBlock>> registeredPartials = ImmutableList.builder();
		for (PaletteBlockPatterns pattern : patterns) {

			CreateRegistrate registrate = Create.registrate();
			BlockBuilder<? extends BeetrootsBlock, CreateRegistrate> builder =
				registrate.block(pattern.createName(name), pattern.getBlockFactory())
					.initialProperties(initialProperties)
					.blockstate(pattern.getBlockStateGenerator()
						.apply(pattern)
						.apply(name)::accept);

			if (pattern.isTranslucent())
				builder.addLayer(() -> VertexConsumerProvider::f);
			if (pattern == PaletteBlockPatterns.COBBLESTONE)
				builder.item().tag(AllTags.AllItemTags.COBBLESTONE.tag);
			if (pattern.hasFoliage())
				builder.onRegister(CreateRegistrate.blockColors(() -> AllColorHandlers::getGrassyBlock));
			pattern.createCTBehaviour(variant)
				.ifPresent(b -> builder.onRegister(connectedTextures(b)));

			builder.recipe((c, p) -> {
				p.stonecutting(DataIngredient.items(variant.getBaseBlock()
					.get()), c::get);
				pattern.addRecipes(variant, c, p);
			});

			if (pattern.hasFoliage())
				builder.item()
					.onRegister(CreateRegistrate.itemColors(() -> AllColorHandlers::getGrassyItem))
					.build();
			else
				builder.simpleItem();

			BlockEntry<? extends BeetrootsBlock> block = builder.register();
			registeredBlocks.add(block);

			for (PaletteBlockPartial<? extends BeetrootsBlock> partialBlock : pattern.getPartials())
				registeredPartials.add(partialBlock.create(name, pattern, block)
					.register());

		}
		this.registeredBlocks = registeredBlocks.build();
		this.registeredPartials = registeredPartials.build();

	}

}
