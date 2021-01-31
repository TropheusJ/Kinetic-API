package com.simibubi.kinetic_api.foundation.data;

import static com.simibubi.kinetic_api.foundation.data.CreateRegistrate.connectedTextures;

import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.kinetic_api.AllSpriteShifts;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.content.palettes.ConnectedGlassBlock;
import com.simibubi.kinetic_api.content.palettes.ConnectedGlassPaneBlock;
import com.simibubi.kinetic_api.content.palettes.GlassPaneBlock;
import com.simibubi.kinetic_api.content.palettes.WindowBlock;
import com.simibubi.kinetic_api.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.kinetic_api.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.kinetic_api.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.kinetic_api.foundation.block.connected.HorizontalCTBehaviour;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.PistonBlockEntity.c;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;

public class WindowGen {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static BlockEntry<WindowBlock> woodenWindowBlock(StairShape woodType, BeetrootsBlock planksBlock) {
		return woodenWindowBlock(woodType, planksBlock, () -> VertexConsumerProvider::d);
	}

	public static BlockEntry<WindowBlock> customWindowBlock(String name, Supplier<? extends GameRules> ingredient,
		CTSpriteShiftEntry ct, Supplier<Supplier<VertexConsumerProvider>> renderType) {
		NonNullFunction<String, Identifier> end_texture = n -> Create.asResource(palettesDir() + name + "_end");
		NonNullFunction<String, Identifier> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, ingredient, ct, renderType, end_texture, side_texture);
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(StairShape woodType, BeetrootsBlock planksBlock,
		Supplier<Supplier<VertexConsumerProvider>> renderType) {
		String woodName = woodType.b();
		String name = woodName + "_window";
		NonNullFunction<String, Identifier> end_texture =
			$ -> new Identifier("block/" + woodName + "_planks");
		NonNullFunction<String, Identifier> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, () -> planksBlock, AllSpriteShifts.getWoodenWindow(woodType), renderType, end_texture,
			side_texture);
	}

	public static BlockEntry<WindowBlock> windowBlock(String name, Supplier<? extends GameRules> ingredient,
		CTSpriteShiftEntry ct, Supplier<Supplier<VertexConsumerProvider>> renderType,
		NonNullFunction<String, Identifier> endTexture, NonNullFunction<String, Identifier> sideTexture) {
		return REGISTRATE.block(name, WindowBlock::new)
			.onRegister(connectedTextures(new HorizontalCTBehaviour(ct)))
			.addLayer(renderType)
			.recipe((c, p) -> ShapedRecipeJsonFactory.a(c.get(), 2)
				.pattern(" # ")
				.pattern("#X#")
				.a('#', ingredient.get())
				.a('X', DataIngredient.tag(Tags.Items.GLASS_COLORLESS))
				.criterion("has_ingredient", p.a(ingredient.get()))
				.offerTo(p::accept))
			.initialProperties(() -> BellBlock.ap)
			.loot((t, g) -> t.c(g))
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.cubeColumn(c.getName(), sideTexture.apply(c.getName()), endTexture.apply(c.getName()))))
			.tag(StatHandler.W)
			.simpleItem()
			.register();
	}

	public static BlockEntry<ConnectedGlassBlock> framedGlass(String name, ConnectedTextureBehaviour behaviour) {
		return REGISTRATE.block(name, ConnectedGlassBlock::new)
			.onRegister(connectedTextures(behaviour))
			.addLayer(() -> VertexConsumerProvider::f)
			.initialProperties(() -> BellBlock.ap)
			.loot((t, g) -> t.c(g))
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_COLORLESS), c::get))
			.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/", "framed_glass"))
			.tag(Tags.Blocks.GLASS_COLORLESS, StatHandler.W)
			.item()
			.tag(Tags.Items.GLASS_COLORLESS)
			.model((c, p) -> p.cubeColumn(c.getName(), p.modLoc(palettesDir() + c.getName()),
				p.modLoc("block/palettes/framed_glass")))
			.build()
			.register();
	}

	public static BlockEntry<ConnectedGlassPaneBlock> framedGlassPane(String name, Supplier<? extends BeetrootsBlock> parent,
		CTSpriteShiftEntry ctshift) {
		Identifier sideTexture = Create.asResource(palettesDir() + "framed_glass");
		Identifier itemSideTexture = Create.asResource(palettesDir() + name);
		Identifier topTexture = Create.asResource(palettesDir() + "framed_glass_pane_top");
		Supplier<Supplier<VertexConsumerProvider>> renderType = () -> VertexConsumerProvider::f;
		return connectedGlassPane(name, parent, ctshift, sideTexture, itemSideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> customWindowPane(String name, Supplier<? extends BeetrootsBlock> parent,
		CTSpriteShiftEntry ctshift, Supplier<Supplier<VertexConsumerProvider>> renderType) {
		Identifier topTexture = Create.asResource(palettesDir() + name + "_pane_top");
		Identifier sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, ctshift, sideTexture, sideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(StairShape woodType,
		Supplier<? extends BeetrootsBlock> parent) {
		return woodenWindowPane(woodType, parent, () -> VertexConsumerProvider::d);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(StairShape woodType,
		Supplier<? extends BeetrootsBlock> parent, Supplier<Supplier<VertexConsumerProvider>> renderType) {
		String woodName = woodType.b();
		String name = woodName + "_window";
		Identifier topTexture = new Identifier("block/" + woodName + "_planks");
		Identifier sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, AllSpriteShifts.getWoodenWindow(woodType), sideTexture, sideTexture,
			topTexture, renderType);
	}

	public static BlockEntry<GlassPaneBlock> standardGlassPane(String name, Supplier<? extends BeetrootsBlock> parent,
		Identifier sideTexture, Identifier topTexture, Supplier<Supplier<VertexConsumerProvider>> renderType) {
		NonNullBiConsumer<DataGenContext<BeetrootsBlock, GlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), sideTexture, topTexture);
		return glassPane(name, parent, sideTexture, topTexture, GlassPaneBlock::new, renderType, $ -> {
		}, stateProvider);
	}

	private static BlockEntry<ConnectedGlassPaneBlock> connectedGlassPane(String name, Supplier<? extends BeetrootsBlock> parent,
		CTSpriteShiftEntry ctshift, Identifier sideTexture, Identifier itemSideTexture,
		Identifier topTexture, Supplier<Supplier<VertexConsumerProvider>> renderType) {
		NonNullConsumer<? super ConnectedGlassPaneBlock> connectedTextures =
			connectedTextures(new GlassPaneCTBehaviour(ctshift));
		String CGPparents = "block/connected_glass_pane/";
		String prefix = name + "_pane_";

		Function<RegistrateBlockstateProvider, ModelFile> post =
			getPaneModelProvider(CGPparents, prefix, "post", sideTexture, topTexture),
			side = getPaneModelProvider(CGPparents, prefix, "side", sideTexture, topTexture),
			sideAlt = getPaneModelProvider(CGPparents, prefix, "side_alt", sideTexture, topTexture),
			noSide = getPaneModelProvider(CGPparents, prefix, "noside", sideTexture, topTexture),
			noSideAlt = getPaneModelProvider(CGPparents, prefix, "noside_alt", sideTexture, topTexture);

		NonNullBiConsumer<DataGenContext<BeetrootsBlock, ConnectedGlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), post.apply(p), side.apply(p), sideAlt.apply(p), noSide.apply(p),
				noSideAlt.apply(p));

		return glassPane(name, parent, itemSideTexture, topTexture, ConnectedGlassPaneBlock::new, renderType,
			connectedTextures, stateProvider);
	}

	private static Function<RegistrateBlockstateProvider, ModelFile> getPaneModelProvider(String CGPparents,
		String prefix, String partial, Identifier sideTexture, Identifier topTexture) {
		return p -> p.models()
			.withExistingParent(prefix + partial, Create.asResource(CGPparents + partial))
			.texture("pane", sideTexture)
			.texture("edge", topTexture);
	}

	private static <G extends GlassPaneBlock> BlockEntry<G> glassPane(String name, Supplier<? extends BeetrootsBlock> parent,
		Identifier sideTexture, Identifier topTexture, NonNullFunction<c, G> factory,
		Supplier<Supplier<VertexConsumerProvider>> renderType, NonNullConsumer<? super G> connectedTextures,
		NonNullBiConsumer<DataGenContext<BeetrootsBlock, G>, RegistrateBlockstateProvider> stateProvider) {
		name += "_pane";

		return REGISTRATE.block(name, factory)
			.onRegister(connectedTextures)
			.addLayer(renderType)
			.initialProperties(() -> BellBlock.dJ)
			.blockstate(stateProvider)
			.recipe((c, p) -> ShapedRecipeJsonFactory.a(c.get(), 16)
				.pattern("###")
				.pattern("###")
				.a('#', parent.get())
				.criterion("has_ingredient", p.a(parent.get()))
				.offerTo(p::accept))
			.tag(Tags.Blocks.GLASS_PANES)
			.loot((t, g) -> t.c(g))
			.item()
			.tag(Tags.Items.GLASS_PANES)
			.model((c, p) -> p.withExistingParent(c.getName(), new Identifier(Create.ID, "item/pane"))
				.texture("pane", sideTexture)
				.texture("edge", topTexture))
			.build()
			.register();
	}

	private static String palettesDir() {
		return "block/palettes/";
	}

}
