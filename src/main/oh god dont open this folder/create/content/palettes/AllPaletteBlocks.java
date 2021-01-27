package com.simibubi.create.content.palettes;

import static com.simibubi.create.foundation.data.WindowGen.customWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.customWindowPane;
import static com.simibubi.create.foundation.data.WindowGen.framedGlass;
import static com.simibubi.create.foundation.data.WindowGen.framedGlassPane;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowBlock;
import static com.simibubi.create.foundation.data.WindowGen.woodenWindowPane;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.WindowGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.RootsBlock;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.Tags;

public class AllPaletteBlocks {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.palettesCreativeTab)
		.startSection(AllSections.PALETTES);

	// Windows and Glass

	public static final BlockEntry<FrostedIceBlock> TILED_GLASS = REGISTRATE.block("tiled_glass", FrostedIceBlock::new)
		.initialProperties(() -> BellBlock.ap)
		.addLayer(() -> VertexConsumerProvider::d)
		.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_COLORLESS), c::get))
		.blockstate(palettesCubeAll())
		.tag(Tags.Blocks.GLASS_COLORLESS, StatHandler.W)
		.item()
		.tag(Tags.Items.GLASS_COLORLESS)
		.build()
		.register();

	public static final BlockEntry<ConnectedGlassBlock> FRAMED_GLASS =
		framedGlass("framed_glass", new StandardCTBehaviour(AllSpriteShifts.FRAMED_GLASS)),
		HORIZONTAL_FRAMED_GLASS = framedGlass("horizontal_framed_glass",
			new HorizontalCTBehaviour(AllSpriteShifts.HORIZONTAL_FRAMED_GLASS, AllSpriteShifts.FRAMED_GLASS)),
		VERTICAL_FRAMED_GLASS =
			framedGlass("vertical_framed_glass", new HorizontalCTBehaviour(AllSpriteShifts.VERTICAL_FRAMED_GLASS));

	public static final BlockEntry<GlassPaneBlock> TILED_GLASS_PANE =
		WindowGen.standardGlassPane("tiled_glass", TILED_GLASS, Create.asResource("block/palettes/tiled_glass"),
			new Identifier("block/glass_pane_top"), () -> VertexConsumerProvider::d);

	public static final BlockEntry<ConnectedGlassPaneBlock> FRAMED_GLASS_PANE =
		framedGlassPane("framed_glass", FRAMED_GLASS, AllSpriteShifts.FRAMED_GLASS),
		HORIZONTAL_FRAMED_GLASS_PANE = framedGlassPane("horizontal_framed_glass", HORIZONTAL_FRAMED_GLASS,
			AllSpriteShifts.HORIZONTAL_FRAMED_GLASS),
		VERTICAL_FRAMED_GLASS_PANE =
			framedGlassPane("vertical_framed_glass", VERTICAL_FRAMED_GLASS, AllSpriteShifts.VERTICAL_FRAMED_GLASS);

	public static final BlockEntry<WindowBlock> OAK_WINDOW = woodenWindowBlock(StairShape.field_12710, BellBlock.n),
		SPRUCE_WINDOW = woodenWindowBlock(StairShape.field_12712, BellBlock.EAST_WALL_SHAPE),
		BIRCH_WINDOW = woodenWindowBlock(StairShape.field_12713, BellBlock.NORTH_WALL_SHAPE, () -> VertexConsumerProvider::f),
		JUNGLE_WINDOW = woodenWindowBlock(StairShape.field_12709, BellBlock.SOUTH_WALL_SHAPE),
		ACACIA_WINDOW = woodenWindowBlock(StairShape.field_12708, BellBlock.HANGING_SHAPE),
		DARK_OAK_WINDOW = woodenWindowBlock(StairShape.name, BellBlock.s),
		CRIMSON_WINDOW = woodenWindowBlock(StairShape.field_12711, BellBlock.mC),
		WARPED_WINDOW = woodenWindowBlock(StairShape.h, BellBlock.mD),
		ORNATE_IRON_WINDOW = customWindowBlock("ornate_iron_window", AllItems.ANDESITE_ALLOY,
			AllSpriteShifts.ORNATE_IRON_WINDOW, () -> VertexConsumerProvider::d);

	public static final BlockEntry<ConnectedGlassPaneBlock> OAK_WINDOW_PANE =
		woodenWindowPane(StairShape.field_12710, OAK_WINDOW),
		SPRUCE_WINDOW_PANE = woodenWindowPane(StairShape.field_12712, SPRUCE_WINDOW),
		BIRCH_WINDOW_PANE = woodenWindowPane(StairShape.field_12713, BIRCH_WINDOW, () -> VertexConsumerProvider::f),
		JUNGLE_WINDOW_PANE = woodenWindowPane(StairShape.field_12709, JUNGLE_WINDOW),
		ACACIA_WINDOW_PANE = woodenWindowPane(StairShape.field_12708, ACACIA_WINDOW),
		DARK_OAK_WINDOW_PANE = woodenWindowPane(StairShape.name, DARK_OAK_WINDOW),
		CRIMSON_WINDOW_PANE = woodenWindowPane(StairShape.field_12711, CRIMSON_WINDOW),
		WARPED_WINDOW_PANE = woodenWindowPane(StairShape.h, WARPED_WINDOW),
		ORNATE_IRON_WINDOW_PANE = customWindowPane("ornate_iron_window", ORNATE_IRON_WINDOW,
			AllSpriteShifts.ORNATE_IRON_WINDOW, () -> VertexConsumerProvider::d);

	// Vanilla stone variant patterns

	public static final PalettesVariantEntry GRANITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.GRANITE, PaletteBlockPatterns.vanillaRange, () -> BellBlock.POWERED);

	public static final PalettesVariantEntry DIORITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DIORITE, PaletteBlockPatterns.vanillaRange, () -> BellBlock.EAST_WEST_SHAPE);

	public static final PalettesVariantEntry ANDESITE_VARIANTS = new PalettesVariantEntry(PaletteStoneVariants.ANDESITE,
		PaletteBlockPatterns.vanillaRange, () -> BellBlock.BELL_LIP_SHAPE);

	// Create stone variants

	public static final BlockEntry<RootsBlock> LIMESAND = REGISTRATE.block("limesand", p -> new RootsBlock(0xD7D7C7, p))
		.initialProperties(() -> BellBlock.C)
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final BlockEntry<BeetrootsBlock> LIMESTONE =
		REGISTRATE.baseBlock("limestone", BeetrootsBlock::new, () -> BellBlock.at, true)
			.register();

	public static final PalettesVariantEntry LIMESTONE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.LIMESTONE, PaletteBlockPatterns.standardRange, LIMESTONE);

	public static final BlockEntry<BeetrootsBlock> WEATHERED_LIMESTONE =
		REGISTRATE.baseBlock("weathered_limestone", BeetrootsBlock::new, () -> BellBlock.at, true)
			.register();

	public static final PalettesVariantEntry WEATHERED_LIMESTONE_VARIANTS = new PalettesVariantEntry(
		PaletteStoneVariants.WEATHERED_LIMESTONE, PaletteBlockPatterns.standardRange, WEATHERED_LIMESTONE);

	public static final BlockEntry<BeetrootsBlock> DOLOMITE =
		REGISTRATE.baseBlock("dolomite", BeetrootsBlock::new, () -> BellBlock.fz, true)
			.register();

	public static final PalettesVariantEntry DOLOMITE_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DOLOMITE, PaletteBlockPatterns.standardRange, DOLOMITE);

	public static final BlockEntry<BeetrootsBlock> GABBRO =
		REGISTRATE.baseBlock("gabbro", BeetrootsBlock::new, () -> BellBlock.BELL_LIP_SHAPE, true)
			.register();

	public static final PalettesVariantEntry GABBRO_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.GABBRO, PaletteBlockPatterns.standardRange, GABBRO);

	public static final BlockEntry<BeetrootsBlock> SCORIA =
		REGISTRATE.baseBlock("scoria", BeetrootsBlock::new, () -> BellBlock.BELL_LIP_SHAPE, true)
			.register();

	public static final BlockEntry<BeetrootsBlock> NATURAL_SCORIA = REGISTRATE.block("natural_scoria", BeetrootsBlock::new)
		.initialProperties(() -> BellBlock.BELL_LIP_SHAPE)
		.onRegister(CreateRegistrate.blockVertexColors(new ScoriaVertexColor()))
		.loot((p, g) -> p.a(g, RegistrateBlockLootTables.b(g, SCORIA.get())))
		.blockstate(palettesCubeAll())
		.simpleItem()
		.register();

	public static final PalettesVariantEntry SCORIA_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.SCORIA, PaletteBlockPatterns.standardRange, SCORIA);

	public static final BlockEntry<BeetrootsBlock> DARK_SCORIA =
		REGISTRATE.baseBlock("dark_scoria", BeetrootsBlock::new, () -> BellBlock.BELL_LIP_SHAPE, false)
			.register();

	public static final PalettesVariantEntry DARK_SCORIA_VARIANTS =
		new PalettesVariantEntry(PaletteStoneVariants.DARK_SCORIA, PaletteBlockPatterns.standardRange, DARK_SCORIA);

	public static void register() {}

	private static <T extends BeetrootsBlock> NonNullBiConsumer<DataGenContext<BeetrootsBlock, T>, RegistrateBlockstateProvider> palettesCubeAll() {
		return (c, p) -> BlockStateGen.cubeAll(c, p, "palettes/");
	}
}
