package com.simibubi.create;

import java.util.Random;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/* todo: imports
import com.simibubi.create.content.CreateItemGroup;
import com.simibubi.create.content.contraptions.TorquePropagator;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.content.palettes.PalettesItemGroup;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.command.ChunkUtil;
import com.simibubi.create.foundation.command.ServerLagger;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.LangMerger;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.data.recipe.StandardRecipeGen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.worldgen.AllWorldFeatures;
import com.tterrag.registrate.util.NonNullLazyValue;
*/
import net.minecraft.data.DataGenerator;
import net.minecraft.particle.ParticleType;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Identifier;
import static net.minecraft.block.Blocks.RED_CONCRETE;
/*
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*/

 class Create implements ModInitializer {

 	// strings for things
	 public static final String ID = "create";
	 public static final String NAME = "Create";
	 public static final String VERSION = "0.3";

	 @Override
	public void onInitialize() {

		 Logger logger = LogManager.getLogger();

		 // item group creation
		final ItemGroup CREATE = FabricItemGroupBuilder.build(new Identifier(ID, "group"), () -> new ItemStack(Blocks.DIRT)); //todo: fix block
		/*
		i dont know why this is "ChorusFruitItem" but its probably important
		 ChorusFruitItem baseCreativeTab = new CreateItemGroup();
		 ChorusFruitItem palettesCreativeTab = new PalettesItemGroup();
		*/

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
		 Create() {
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

			modEventBus.addListener(Create::init);
			MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, Create::onBiomeLoad);
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
	}
}
