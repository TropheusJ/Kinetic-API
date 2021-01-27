package com.simibubi.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionRenderer;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.foundation.ResourceReloadHandler;
import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.item.CustomItemModels;
import com.simibubi.create.foundation.item.CustomRenderedItems;
import com.simibubi.create.foundation.utility.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.outliner.Outliner;
import elg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.metadata.TextureResourceMetadataReader;
import net.minecraft.item.HoeItem;
import net.minecraft.resource.ProfilingResourceReloader;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {

	public static ClientSchematicLoader schematicSender;
	public static SchematicHandler schematicHandler;
	public static SchematicAndQuillHandler schematicAndQuillHandler;
	public static SuperByteBufferCache bufferCache;
	public static final Outliner outliner = new Outliner();

	private static CustomBlockModels customBlockModels;
	private static CustomItemModels customItemModels;
	private static CustomRenderedItems customRenderedItems;
	private static AllColorHandlers colorHandlers;
	private static CasingConnectivity casingConnectivity;

	public static void addClientListeners(IEventBus modEventBus) {
		modEventBus.addListener(CreateClient::clientInit);
		modEventBus.addListener(CreateClient::onModelBake);
		modEventBus.addListener(CreateClient::onModelRegistry);
		modEventBus.addListener(CreateClient::onTextureStitch);
		modEventBus.addListener(AllParticleTypes::registerFactories);
	}

	public static void clientInit(FMLClientSetupEvent event) {
		schematicSender = new ClientSchematicLoader();
		schematicHandler = new SchematicHandler();
		schematicAndQuillHandler = new SchematicAndQuillHandler();

		bufferCache = new SuperByteBufferCache();
		bufferCache.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		bufferCache.registerCompartment(ContraptionRenderer.CONTRAPTION, 20);

		AllKeys.register();
		AllContainerTypes.registerScreenFactories();
		//AllTileEntities.registerRenderers();
		AllEntityTypes.registerRenderers();
		getColorHandler().init();
		AllFluids.assignRenderLayers();

		ReloadableResourceManager resourceManager = KeyBinding.B()
			.M();
		if (resourceManager instanceof ProfilingResourceReloader)
			((ProfilingResourceReloader) resourceManager).a(new ResourceReloadHandler());
	}

	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap()
			.g()
			.equals(GrindstoneScreenHandler.result))
			return;
		SpriteShifter.getAllTargetSprites()
			.forEach(event::addSprite);
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<Identifier, elg> modelRegistry = event.getModelRegistry();
		AllBlockPartials.onModelBake(event);

		getCustomBlockModels()
			.foreach((block, modelFunc) -> swapModels(modelRegistry, getAllBlockStateModelLocations(block), modelFunc));
		getCustomItemModels()
			.foreach((item, modelFunc) -> swapModels(modelRegistry, getItemModelLocation(item), modelFunc));
		getCustomRenderedItems().foreach((item, modelFunc) -> {
			swapModels(modelRegistry, getItemModelLocation(item), m -> modelFunc.apply(m)
				.loadPartials(event));
		});
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		AllBlockPartials.onModelRegistry(event);

		getCustomRenderedItems().foreach((item, modelFunc) -> modelFunc.apply(null)
			.getModelLocations()
			.forEach(ModelLoader::addSpecialModel));
	}

	protected static TextureResourceMetadataReader getItemModelLocation(HoeItem item) {
		return new TextureResourceMetadataReader(item.getRegistryName(), "inventory");
	}

	protected static List<TextureResourceMetadataReader> getAllBlockStateModelLocations(BeetrootsBlock block) {
		List<TextureResourceMetadataReader> models = new ArrayList<>();
		block.m()
			.a()
			.forEach(state -> {
				models.add(getBlockModelLocation(block, RenderLayer.a(state.s())));
			});
		return models;
	}

	protected static TextureResourceMetadataReader getBlockModelLocation(BeetrootsBlock block, String suffix) {
		return new TextureResourceMetadataReader(block.getRegistryName(), suffix);
	}

	protected static <T extends elg> void swapModels(Map<Identifier, elg> modelRegistry,
		List<TextureResourceMetadataReader> locations, Function<elg, T> factory) {
		locations.forEach(location -> {
			swapModels(modelRegistry, location, factory);
		});
	}
	
	protected static <T extends elg> void swapModels(Map<Identifier, elg> modelRegistry,
		TextureResourceMetadataReader location, Function<elg, T> factory) {
		modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
	}

	public static CustomItemModels getCustomItemModels() {
		if (customItemModels == null)
			customItemModels = new CustomItemModels();
		return customItemModels;
	}

	public static CustomRenderedItems getCustomRenderedItems() {
		if (customRenderedItems == null)
			customRenderedItems = new CustomRenderedItems();
		return customRenderedItems;
	}

	public static CustomBlockModels getCustomBlockModels() {
		if (customBlockModels == null)
			customBlockModels = new CustomBlockModels();
		return customBlockModels;
	}

	public static AllColorHandlers getColorHandler() {
		if (colorHandlers == null)
			colorHandlers = new AllColorHandlers();
		return colorHandlers;
	}
	
	public static CasingConnectivity getCasingConnectivity() {
		if (casingConnectivity == null)
			casingConnectivity = new CasingConnectivity();
		return casingConnectivity;
	}

}
