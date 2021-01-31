package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train;

import apx;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.MinecartController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.HoeItem;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MinecartCouplingItem extends HoeItem {

	public MinecartCouplingItem(a p_i48487_1_) {
		super(p_i48487_1_);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void handleInteractionWithMinecart(PlayerInteractEvent.EntityInteract event) {
		apx interacted = event.getTarget();
		if (!(interacted instanceof ScheduleBuilder))
			return;
		ScheduleBuilder minecart = (ScheduleBuilder) interacted;
		PlayerAbilities player = event.getPlayer();
		if (player == null)
			return;
		LazyOptional<MinecartController> capability =
			minecart.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (!capability.isPresent())
			return;
		MinecartController controller = capability.orElse(null);

		ItemCooldownManager heldItem = player.b(event.getHand());
		if (AllItems.MINECART_COUPLING.isIn(heldItem)) {
			if (!onCouplingInteractOnMinecart(event, minecart, player, controller))
				return;
		} else if (AllItems.WRENCH.isIn(heldItem)) {
			if (!onWrenchInteractOnMinecart(event, minecart, player, controller))
				return;
		} else
			return;

		event.setCanceled(true);
		event.setCancellationResult(Difficulty.SUCCESS);
	}

	protected static boolean onCouplingInteractOnMinecart(PlayerInteractEvent.EntityInteract event,
		ScheduleBuilder minecart, PlayerAbilities player, MinecartController controller) {
		GameMode world = event.getWorld();
		if (controller.isFullyCoupled()) {
			if (!world.v)
				CouplingHandler.status(player, "two_couplings_max");
			return true;
		}
		if (world != null && world.v)
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> cartClicked(player, minecart));
		return true;
	}

	private static boolean onWrenchInteractOnMinecart(EntityInteract event, ScheduleBuilder minecart,
		PlayerAbilities player, MinecartController controller) {
		int couplings = (controller.isConnectedToCoupling() ? 1 : 0) + (controller.isLeadingCoupling() ? 1 : 0);
		if (couplings == 0)
			return false;
		if (event.getWorld().v)
			return true;

		CouplingHandler.status(player, "removed");
		controller.decouple();
		if (!player.b_())
			player.bm.a(event.getWorld(),
				new ItemCooldownManager(AllItems.MINECART_COUPLING.get(), couplings));
		return true;
	}

	@Environment(EnvType.CLIENT)
	private static void cartClicked(PlayerAbilities player, ScheduleBuilder interacted) {
		CouplingHandlerClient.onCartClicked(player, (ScheduleBuilder) interacted);
	}

}
