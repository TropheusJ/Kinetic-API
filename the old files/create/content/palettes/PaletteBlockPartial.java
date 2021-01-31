package com.simibubi.kinetic_api.content.palettes;

import java.util.Arrays;
import java.util.function.Supplier;

import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.block.TwistingVinesPlantBlock;
import net.minecraft.block.entity.PistonBlockEntity.c;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class PaletteBlockPartial<B extends BeetrootsBlock> {

	public static final PaletteBlockPartial<SpreadableBlock> STAIR = new Stairs();
	public static final PaletteBlockPartial<AbstractSignBlock> SLAB = new Slab(false);
	public static final PaletteBlockPartial<AbstractSignBlock> UNIQUE_SLAB = new Slab(true);
	public static final PaletteBlockPartial<TwistingVinesPlantBlock> WALL = new Wall();

	public static final PaletteBlockPartial<?>[] AllPartials = { STAIR, SLAB, WALL };
	public static final PaletteBlockPartial<?>[] ForPolished = { STAIR, UNIQUE_SLAB, WALL };

	private String name;

	private PaletteBlockPartial(String name) {
		this.name = name;
	}

	public @NonnullType BlockBuilder<B, CreateRegistrate> create(String variantName, PaletteBlockPatterns pattern,
		Supplier<? extends BeetrootsBlock> block) {
		String patternName = pattern.createName(variantName);
		String blockName = patternName + "_" + this.name;
		return Create.registrate()
			.block(blockName, p -> createBlock(block))
			.blockstate((c, p) -> generateBlockState(c, p, variantName, pattern, block))
			.recipe((c, p) -> createRecipes(block, c, p))
			.transform(b -> transformBlock(b, variantName, pattern))
			.item()
			.transform(b -> transformItem(b, variantName, pattern))
			.build();
	}

	protected Identifier getMainTexture(String variantName, PaletteBlockPatterns pattern) {
		return pattern.toLocation(variantName, pattern.getTextureForPartials());
	}

	protected BlockBuilder<B, CreateRegistrate> transformBlock(BlockBuilder<B, CreateRegistrate> builder,
		String variantName, PaletteBlockPatterns pattern) {
		getBlockTags().forEach(builder::tag);
		return builder;
	}

	protected ItemBuilder<BannerItem, BlockBuilder<B, CreateRegistrate>> transformItem(
		ItemBuilder<BannerItem, BlockBuilder<B, CreateRegistrate>> builder, String variantName,
		PaletteBlockPatterns pattern) {
		getItemTags().forEach(builder::tag);
		return builder;
	}

	protected abstract Iterable<RequiredTagList.e<BeetrootsBlock>> getBlockTags();

	protected abstract Iterable<RequiredTagList.e<HoeItem>> getItemTags();

	protected abstract B createBlock(Supplier<? extends BeetrootsBlock> block);

	protected abstract void createRecipes(Supplier<? extends BeetrootsBlock> block, DataGenContext<BeetrootsBlock, ? extends BeetrootsBlock> c,
		RegistrateRecipeProvider p);

	protected abstract void generateBlockState(DataGenContext<BeetrootsBlock, B> ctx, RegistrateBlockstateProvider prov,
		String variantName, PaletteBlockPatterns pattern, Supplier<? extends BeetrootsBlock> block);

	private static class Stairs extends PaletteBlockPartial<SpreadableBlock> {

		public Stairs() {
			super("stairs");
		}

		@Override
		protected SpreadableBlock createBlock(Supplier<? extends BeetrootsBlock> block) {
			return new SpreadableBlock(() -> block.get()
				.n(), c.a(block.get()));
		}

		@Override
		protected void generateBlockState(DataGenContext<BeetrootsBlock, SpreadableBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPatterns pattern, Supplier<? extends BeetrootsBlock> block) {
			prov.stairsBlock(ctx.get(), getMainTexture(variantName, pattern));
		}

		@Override
		protected Iterable<RequiredTagList.e<BeetrootsBlock>> getBlockTags() {
			return Arrays.asList(StatHandler.D);
		}

		@Override
		protected Iterable<RequiredTagList.e<HoeItem>> getItemTags() {
			return Arrays.asList(EntityTypeTags.B);
		}

		@Override
		protected void createRecipes(Supplier<? extends BeetrootsBlock> block, DataGenContext<BeetrootsBlock, ? extends BeetrootsBlock> c,
			RegistrateRecipeProvider p) {
			DataIngredient source = DataIngredient.items(block.get());
			Supplier<? extends BeetrootsBlock> result = c::get;
			p.stairs(source, result, c.getName(), true);
		}

	}

	private static class Slab extends PaletteBlockPartial<AbstractSignBlock> {

		private boolean customSide;

		public Slab(boolean customSide) {
			super("slab");
			this.customSide = customSide;
		}

		@Override
		protected AbstractSignBlock createBlock(Supplier<? extends BeetrootsBlock> block) {
			return new AbstractSignBlock(c.a(block.get()));
		}

		@Override
		protected void generateBlockState(DataGenContext<BeetrootsBlock, AbstractSignBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPatterns pattern, Supplier<? extends BeetrootsBlock> block) {
			String name = ctx.getName();
			Identifier mainTexture = getMainTexture(variantName, pattern);
			Identifier sideTexture =
				customSide ? new Identifier(mainTexture.getNamespace(), mainTexture.getPath() + "_slab")
					: mainTexture;

			ModelFile bottom = prov.models()
				.slab(name, sideTexture, mainTexture, mainTexture);
			ModelFile top = prov.models()
				.slabTop(name + "_top", sideTexture, mainTexture, mainTexture);
			ModelFile doubleSlab;

			if (customSide) {
				doubleSlab = prov.models()
					.cubeColumn(name + "_double", sideTexture, mainTexture);
			} else {
				doubleSlab = prov.models()
					.getExistingFile(prov.modLoc(name.replace("_slab", "")));
			}

			prov.slabBlock(ctx.get(), bottom, top, doubleSlab);
		}

		@Override
		protected Iterable<RequiredTagList.e<BeetrootsBlock>> getBlockTags() {
			return Arrays.asList(StatHandler.E);
		}

		@Override
		protected Iterable<RequiredTagList.e<HoeItem>> getItemTags() {
			return Arrays.asList(EntityTypeTags.C);
		}

		@Override
		protected void createRecipes(Supplier<? extends BeetrootsBlock> block, DataGenContext<BeetrootsBlock, ? extends BeetrootsBlock> c,
			RegistrateRecipeProvider p) {
			DataIngredient source = DataIngredient.items(block.get());
			Supplier<? extends BeetrootsBlock> result = c::get;
			p.slab(source, result, c.getName(), true);
		}

		@Override
		protected BlockBuilder<AbstractSignBlock, CreateRegistrate> transformBlock(
				BlockBuilder<AbstractSignBlock, CreateRegistrate> builder,
				String variantName, PaletteBlockPatterns pattern) {
			builder.loot((lt, block) -> lt.a(block, lt.e(block)));
			return super.transformBlock(builder, variantName, pattern);
		}

	}

	private static class Wall extends PaletteBlockPartial<TwistingVinesPlantBlock> {

		public Wall() {
			super("wall");
		}

		@Override
		protected TwistingVinesPlantBlock createBlock(Supplier<? extends BeetrootsBlock> block) {
			return new TwistingVinesPlantBlock(c.a(block.get()));
		}

		@Override
		protected ItemBuilder<BannerItem, BlockBuilder<TwistingVinesPlantBlock, CreateRegistrate>> transformItem(
			ItemBuilder<BannerItem, BlockBuilder<TwistingVinesPlantBlock, CreateRegistrate>> builder, String variantName,
			PaletteBlockPatterns pattern) {
			builder.model((c, p) -> p.wallInventory(c.getName(), getMainTexture(variantName, pattern)));
			return super.transformItem(builder, variantName, pattern);
		}

		@Override
		protected void generateBlockState(DataGenContext<BeetrootsBlock, TwistingVinesPlantBlock> ctx, RegistrateBlockstateProvider prov,
			String variantName, PaletteBlockPatterns pattern, Supplier<? extends BeetrootsBlock> block) {
			prov.wallBlock(ctx.get(), pattern.createName(variantName), getMainTexture(variantName, pattern));
		}

		@Override
		protected Iterable<RequiredTagList.e<BeetrootsBlock>> getBlockTags() {
			return Arrays.asList(StatHandler.F);
		}

		@Override
		protected Iterable<RequiredTagList.e<HoeItem>> getItemTags() {
			return Arrays.asList(EntityTypeTags.D);
		}

		@Override
		protected void createRecipes(Supplier<? extends BeetrootsBlock> block, DataGenContext<BeetrootsBlock, ? extends BeetrootsBlock> c,
			RegistrateRecipeProvider p) {
			DataIngredient source = DataIngredient.items(block.get());
			Supplier<? extends BeetrootsBlock> result = c::get;
			p.wall(source, result);
		}

	}

}
