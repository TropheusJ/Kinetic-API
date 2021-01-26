package com.simibubi.create.events;

import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.tools.ExtendoGripRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperRenderHandler;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import cut;
import ejo;
import net.minecraft.client.options.AoMode;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

	private static final String itemPrefix = "item." + Create.ID;
	private static final String blockPrefix = "block." + Create.ID;

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		GameMode world = KeyBinding.B().r;
		if (event.phase == Phase.START)
			return;

		AnimationTickHolder.tick();

		if (!isGameActive())
			return;

		CreateClient.schematicSender.tick();
		CreateClient.schematicAndQuillHandler.tick();
		CreateClient.schematicHandler.tick();

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		WorldshaperRenderHandler.tick();
		BlockzapperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ZapperRenderHandler.tick();
		ExtendoGripRenderHandler.tick();
//		CollisionDebugger.tick();
		ArmInteractionPointHandler.tick();
		PlacementHelpers.tick();
		CreateClient.outliner.tickOutlines();
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		CreateClient.bufferCache.invalidate();
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		BufferVertexConsumer ms = event.getMatrixStack();
		AoMode info = KeyBinding.B().boundKey.k();
		EntityHitResult view = info.b();
		ms.a();
		ms.a(-view.getX(), -view.getY(), -view.getZ());
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

		CouplingRenderer.renderAll(ms, buffer);
		CreateClient.schematicHandler.render(ms, buffer);
		CreateClient.outliner.renderOutlines(ms, buffer);
//		CollisionDebugger.render(ms, buffer);
		buffer.draw();

		ms.b();
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR)
			return;

		onRenderHotbar(event.getMatrixStack(), KeyBinding.B()
			.aC()
			.b(), 0xF000F0, ejo.a, event.getPartialTicks());
	}

	public static void onRenderHotbar(BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay, float partialTicks) {
		CreateClient.schematicHandler.renderOverlay(ms, buffer, light, overlay, partialTicks);
	}

	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;
		if (event.getPlayer() == null)
			return;

		ItemCooldownManager stack = event.getItemStack();
		String translationKey = stack.b()
				.f(stack);
		if (!translationKey.startsWith(itemPrefix) && !translationKey.startsWith(blockPrefix))
			return;

		if (TooltipHelper.hasTooltip(stack, event.getPlayer())) {
			List<Text> itemTooltip = event.getToolTip();
			List<Text> toolTip = new ArrayList<>();
			toolTip.add(itemTooltip.remove(0));
			TooltipHelper.getTooltip(stack)
					.addInformation(toolTip);
			itemTooltip.addAll(0, toolTip);
		}

	}

	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	protected static boolean isGameActive() {
		return !(KeyBinding.B().r == null || KeyBinding.B().s == null);
	}

	@SubscribeEvent
	public static void getFogDensity(EntityViewRenderEvent.FogDensity event) {
		AoMode info = event.getInfo();
		EmptyFluid fluidState = info.k();
		if (fluidState.c())
			return;
		cut fluid = fluidState.a();

		if (fluid.a(AllFluids.CHOCOLATE.get())) {
			event.setDensity(5f);
			event.setCanceled(true);
		}

		if (fluid.a(AllFluids.HONEY.get())) {
			event.setDensity(1.5f);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void getFogColor(EntityViewRenderEvent.FogColors event) {
		AoMode info = event.getInfo();
		EmptyFluid fluidState = info.k();
		if (fluidState.c())
			return;
		cut fluid = fluidState.a();

		if (fluid.a(AllFluids.CHOCOLATE.get())) {
			event.setRed(98 / 256f);
			event.setGreen(32 / 256f);
			event.setBlue(32 / 256f);
		}

		if (fluid.a(AllFluids.HONEY.get())) {
			event.setRed(234 / 256f);
			event.setGreen(174 / 256f);
			event.setBlue(47 / 256f);
		}
	}

	@SubscribeEvent
	public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		ItemCooldownManager stack = event.getItemStack();
		if (stack.b() instanceof ZapperItem) {
			AllPackets.channel.sendToServer(new LeftClickPacket());
		}
	}

}
