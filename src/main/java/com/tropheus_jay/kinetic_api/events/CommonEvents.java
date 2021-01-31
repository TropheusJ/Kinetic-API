package com.tropheus_jay.kinetic_api.events;
/*
import apx;
import com.simibubi.kinetic_api.AllFluids;
import KineticAPI;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.kinetic_api.content.contraptions.fluids.recipe.FluidTransferRecipes;
import com.simibubi.kinetic_api.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.kinetic_api.content.contraptions.wrench.WrenchItem;
import com.simibubi.kinetic_api.content.curiosities.zapper.ZapperInteractionHandler;
import com.simibubi.kinetic_api.content.curiosities.zapper.ZapperItem;
import com.simibubi.kinetic_api.content.schematics.ServerSchematicLoader;
import com.simibubi.kinetic_api.foundation.command.AllCommands;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import Iterate;
import com.simibubi.kinetic_api.foundation.utility.ServerSpeedProvider;
import WorldAttached;
import com.simibubi.kinetic_api.foundation.utility.recipe.RecipeFinder;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.WorldAccess;

import java.util.logging.Level;


import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent.FluidPlaceBlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
*/
//@EventBusSubscriber
public class CommonEvents {
    /* THIS FILE IS REDUNDANT
    ACTUAL CODE IS IN KineticAPI.java
     */
/*
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (KineticAPI.schematicReceiver == null)
			KineticAPI.schematicReceiver = new ServerSchematicLoader();
		KineticAPI.schematicReceiver.tick();
		KineticAPI.lagger.tick();
		ServerSpeedProvider.serverTick();
	}

	@SubscribeEvent
	public static void onChunkUnloaded(ChunkEvent.Unload event) {
		CapabilityMinecartController.onChunkUnloaded(event);
	}

	@SubscribeEvent
	public static void whenFluidsMeet(FluidPlaceBlockEvent event) {
		PistonHandler blockState = event.getOriginalState();
		EmptyFluid fluidState = blockState.m();
		BlockPos pos = event.getPos();
		GrassColors world = event.getWorld();

		if (fluidState.b() && FluidHelper.isLava(fluidState.a()))
			return;

		for (Direction direction : Iterate.directions) {
			EmptyFluid metFluidState = fluidState.b() ? fluidState : world.b(pos.offset(direction));
			if (!metFluidState.a(BlockTags.field_15481))
				continue;
			PistonHandler lavaInteraction = AllFluids.getLavaInteraction(metFluidState);
			if (lavaInteraction == null)
				continue;
			event.setNewState(lavaInteraction);
			break;
		}
	}

	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if (event.phase == Phase.START)
			return;
		GameMode world = event.world;
		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);
	}

	@SubscribeEvent
	public static void onUpdateLivingEntity(LivingUpdateEvent event) {
		SaddledComponent entityLiving = event.getEntityLiving();
		GameMode world = entityLiving.l;
		if (world == null)
			return;
		ContraptionHandler.entitiesWhoJustDismountedGetSentToTheRightLocation(entityLiving, world);
	}

	@SubscribeEvent
	public static void onEntityAdded(EntityJoinWorldEvent event) {
		apx entity = event.getEntity();
		GameMode world = event.getWorld();
		ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, world);
	}

	@SubscribeEvent
	public static void onEntityAttackedByPlayer(AttackEntityEvent event) {
		WrenchItem.wrenchInstaKillsMinecarts(event);
	}

	@SubscribeEvent
	public static void serverStarted(FMLServerStartingEvent event) {
		AllCommands.register(event.getServer().aC().getDispatcher());
	}

	@SubscribeEvent
	public static void registerReloadListeners(AddReloadListenerEvent event) {
		event.addListener(RecipeFinder.LISTENER);
		event.addListener(PotionMixingRecipeManager.LISTENER);
		event.addListener(FluidTransferRecipes.LISTENER);
	}

	@SubscribeEvent
	public static void serverStopped(FMLServerStoppingEvent event) {
		KineticAPI.schematicReceiver.shutdown();
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<apx> event) {
		CapabilityMinecartController.attach(event);
	}

	@SubscribeEvent
	public static void startTracking(PlayerEvent.StartTracking event) {
		CapabilityMinecartController.startTracking(event);
	}

	public static void leftClickEmpty(ServerPlayerEntity player) {
		ItemCooldownManager stack = player.dC();
		if (stack.b() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}
*/
}
