package com.tropheus_jay.kinetic_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tropheus_jay.kinetic_api.content.contraptions.TorquePropagator;
import com.tropheus_jay.kinetic_api.foundation.utility.WorldAttached;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

 public class KineticAPI implements ModInitializer {

 	// strings for things
	 public static final String ID = "kinetic_api";
	 public static final String NAME = "KineticAPI";
	 //public static final String VERSION = "0.3";

	 // non-string things that also do things
	 public static TorquePropagator torquePropagator = new TorquePropagator();
	 public static Logger logger = LogManager.getLogger();



	 @Override
	public void onInitialize() {

		 AllBlocks.init();

		 // item group creation
		final ItemGroup CREATE = FabricItemGroupBuilder.build(new Identifier(ID, "group"), () -> new ItemStack(Blocks.DIRT)); //todo: fix block
		 // ???
		 Gson GSON = new GsonBuilder().setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
/*
		 ServerSchematicLoader schematicReceiver;
		 RedstoneLinkNetworkHandler redstoneLinkNetworkHandler;
		 TorquePropagator torquePropagator;
		 ServerLagger lagger;
		 ChunkUtil chunkUtil;
		 *///todo: implement other stuff, then fix this
		 Random random;

/*		//final NonNullLazyValue<CreateRegistrate> registrate = CreateRegistrate.lazy(ID);
 todo: figure out what all this means

		 KineticAPI() {
			IEventBus modEventBus = FMLJavaModLoadingContext.get()
					.getModEventBus();

			AllBlocks.register();
			AllItems.register();
			AllFluids.register();
			AllTags.register();
			AllPaletteBlocks.register();
			AllEntityTypes.register();
			AllTileEntities.register();
			AllMovementBehaviours.register();
todo: pretty sure these are events. time to learn mixin.

			modEventBus.addListener(KineticAPI::init);
			MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, KineticAPI::onBiomeLoad);
			modEventBus.addGenericListener(MapExtendingRecipe.class, AllRecipeTypes::register);
			modEventBus.addGenericListener(LecternScreenHandler.class, AllContainerTypes::register);
			modEventBus.addGenericListener(ParticleType.class, AllParticleTypes::register);
			modEventBus.addGenericListener(MusicSound.class, AllSoundEvents::register);
			modEventBus.addListener(AllConfigs::onLoad);
			modEventBus.addListener(AllConfigs::onReload);
			modEventBus.addListener(EventPriority.LOWEST, this::gatherData);

			AllConfigs.register(); todo: configs
			random = new Random();

			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> CreateClient.addClientListeners(modEventBus));
		}

		 void init ( final FMLCommonSetupEvent event){
			CapabilityMinecartController.register();
			schematicReceiver = new ServerSchematicLoader();
			redstoneLinkNetworkHandler = new RedstoneLinkNetworkHandler();
			torquePropagator = new TorquePropagator();
			lagger = new ServerLagger();

			chunkUtil = new ChunkUtil();
			chunkUtil.init();
			MinecraftForge.EVENT_BUS.register(chunkUtil);

			AllPackets.registerPackets();
			AllTriggers.register();
		}

		 void onBiomeLoad (BiomeLoadingEvent event){
			AllWorldFeatures.reload(event);
		}

		 CreateRegistrate registrate () {
			return registrate.get();
		}

		 Identifier asResource (String path){
			return new Identifier(ID, path);
		}

		 void gatherData (GatherDataEvent event){
			DataGenerator gen = event.getGenerator();
			gen.install(new AllAdvancements(gen));
			gen.install(new LangMerger(gen));
			gen.install(AllSoundEvents.BLAZE_MUNCH.generator(gen));
			gen.install(new StandardRecipeGen(gen));
			gen.install(new MechanicalCraftingRecipeGen(gen));
			ProcessingRecipeGen.registerAll(gen);
		}*/

		 //CommonEvents.java has basically been relocated to here.
		 //todo: redstone links
		 //todo: this might work like this? needs testing.
		 // ok it doesnt
		 ServerWorldEvents.LOAD.register((server, world) -> {
			 //WorldAccess world = event.getWorld();
			 //KineticAPI.redstoneLinkNetworkHandler.onLoadWorld(world);
			 KineticAPI.torquePropagator.onLoadWorld(world);
		 });

		 ServerWorldEvents.UNLOAD.register((server, world) -> {
			 //WorldAccess world = event.getWorld();
			 //KineticAPI.redstoneLinkNetworkHandler.onUnloadWorld(world);
			 KineticAPI.torquePropagator.onUnloadWorld(world);
			 WorldAttached.invalidateWorld(world);
		 });
	}
}
