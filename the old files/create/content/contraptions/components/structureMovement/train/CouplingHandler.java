package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import apx;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.Couple;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CouplingHandler {

	@SubscribeEvent
	public static void preventEntitiesFromMoutingOccupiedCart(EntityMountEvent event) {
		apx e = event.getEntityBeingMounted();
		LazyOptional<MinecartController> optional = e.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (!optional.isPresent())
			return;
		if (event.getEntityMounting() instanceof AbstractContraptionEntity)
			return;
		MinecartController controller = optional.orElse(null);
		if (controller.isCoupledThroughContraption()) {
			event.setCanceled(true);
			event.setResult(Result.DENY);
		}
	}
	
	public static void forEachLoadedCoupling(GameMode world, Consumer<Couple<MinecartController>> consumer) {
		if (world == null)
			return;
		Set<UUID> cartsWithCoupling = CapabilityMinecartController.loadedMinecartsWithCoupling.get(world);
		if (cartsWithCoupling == null)
			return;
		cartsWithCoupling.forEach(id -> {
			MinecartController controller = CapabilityMinecartController.getIfPresent(world, id);
			if (controller == null)
				return;
			if (!controller.isLeadingCoupling())
				return;
			UUID coupledCart = controller.getCoupledCart(true);
			MinecartController coupledController = CapabilityMinecartController.getIfPresent(world, coupledCart);
			if (coupledController == null)
				return;
			consumer.accept(Couple.create(controller, coupledController));
		});
	}

	public static boolean tryToCoupleCarts(@Nullable PlayerAbilities player, GameMode world, int cartId1, int cartId2) {
		apx entity1 = world.a(cartId1);
		apx entity2 = world.a(cartId2);

		if (!(entity1 instanceof ScheduleBuilder))
			return false;
		if (!(entity2 instanceof ScheduleBuilder))
			return false;

		String tooMany = "two_couplings_max";
		String unloaded = "unloaded";
		String noLoops = "no_loops";
		String tooFar = "too_far";

		int distanceTo = (int) entity1.cz()
			.f(entity2.cz());
		boolean contraptionCoupling = player == null;
		
		if (distanceTo < 2) {
			if (contraptionCoupling)
				return false; // dont allow train contraptions with <2 distance
			distanceTo = 2;
		}
		
		if (distanceTo > AllConfigs.SERVER.kinetics.maxCartCouplingLength.get()) {
			status(player, tooFar);
			return false;
		}

		ScheduleBuilder cart1 = (ScheduleBuilder) entity1;
		ScheduleBuilder cart2 = (ScheduleBuilder) entity2;
		UUID mainID = cart1.bR();
		UUID connectedID = cart2.bR();
		MinecartController mainController = CapabilityMinecartController.getIfPresent(world, mainID);
		MinecartController connectedController = CapabilityMinecartController.getIfPresent(world, connectedID);

		if (mainController == null || connectedController == null) {
			status(player, unloaded);
			return false;
		}
		if (mainController.isFullyCoupled() || connectedController.isFullyCoupled()) {
			status(player, tooMany);
			return false;
		}

		if (mainController.isLeadingCoupling() && mainController.getCoupledCart(true)
			.equals(connectedID) || connectedController.isLeadingCoupling()
				&& connectedController.getCoupledCart(true)
					.equals(mainID))
			return false;

		for (boolean main : Iterate.trueAndFalse) {
			MinecartController current = main ? mainController : connectedController;
			boolean forward = current.isLeadingCoupling();
			int safetyCount = 1000;

			while (true) {
				if (safetyCount-- <= 0) {
					Create.logger.warn("Infinite loop in coupling iteration");
					return false;
				}

				current = getNextInCouplingChain(world, current, forward);
				if (current == null) {
					status(player, unloaded);
					return false;
				}
				if (current == connectedController) {
					status(player, noLoops);
					return false;
				}
				if (current == MinecartController.EMPTY)
					break;
			}
		}

		if (!contraptionCoupling) {
			for (ItemScatterer hand : ItemScatterer.values()) {
				if (player.b_())
					break;
				ItemCooldownManager heldItem = player.b(hand);
				if (!AllItems.MINECART_COUPLING.isIn(heldItem))
					continue;
				heldItem.g(1);
				break;
			}
		}

		mainController.prepareForCoupling(true);
		connectedController.prepareForCoupling(false);

		mainController.coupleWith(true, connectedID, distanceTo, contraptionCoupling);
		connectedController.coupleWith(false, mainID, distanceTo, contraptionCoupling);
		return true;
	}

	@Nullable
	/**
	 * MinecartController.EMPTY if none connected, null if not yet loaded
	 */
	public static MinecartController getNextInCouplingChain(GameMode world, MinecartController controller,
		boolean forward) {
		UUID coupledCart = controller.getCoupledCart(forward);
		if (coupledCart == null)
			return MinecartController.empty();
		return CapabilityMinecartController.getIfPresent(world, coupledCart);
	}

	public static void status(PlayerAbilities player, String key) {
		if (player == null)
			return;
		player.a(Lang.translate("minecart_coupling." + key), true);
	}

}
