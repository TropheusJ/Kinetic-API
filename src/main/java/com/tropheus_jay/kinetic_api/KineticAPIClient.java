package com.tropheus_jay.kinetic_api;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.metadata.TextureResourceMetadataReader;
import net.minecraft.item.HoeItem;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Identifier;

public class KineticAPIClient implements ClientModInitializer {
/* todo
	public static ClientSchematicLoader schematicSender;
	public static SchematicHandler schematicHandler;
	public static SchematicAndQuillHandler schematicAndQuillHandler;
	public static SuperByteBufferCache bufferCache;
	*/public static final Outliner outliner = new Outliner();
/*
	private static CustomBlockModels customBlockModels;
	private static CustomItemModels customItemModels;
	private static CustomRenderedItems customRenderedItems;
	private static AllColorHandlers colorHandlers;
	private static CasingConnectivity casingConnectivity;
todo: ugh

	public static void addClientListeners(IEventBus modEventBus) {
		modEventBus.addListener(KineticAPIClient::clientInit);
		modEventBus.addListener(KineticAPIClient::onModelBake);
		modEventBus.addListener(KineticAPIClient::onModelRegistry);
		modEventBus.addListener(KineticAPIClient::onTextureStitch);
		modEventBus.addListener(AllParticleTypes::registerFactories);
	}
*/
	public void onInitializeClient() {
		/* I am REALLY hoping none of this is important yet
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
		*/
	}
/* todo: i have ZERO clue what to do with all this
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
*/
}
