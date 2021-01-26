package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import aqc;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.Outliner.OutlineEntry;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class GoggleOverlayRenderer {

	private static final Map<Object, OutlineEntry> outlines = CreateClient.outliner.getOutlines();

	@SubscribeEvent
	public static void lookingAtBlocksThroughGogglesShowsTooltip(RenderGameOverlayEvent.Post event) {
		BufferVertexConsumer ms = event.getMatrixStack();
		if (event.getType() != ElementType.HOTBAR)
			return;

		Box objectMouseOver = KeyBinding.B().v;
		if (!(objectMouseOver instanceof dcg))
			return;

		for (OutlineEntry entry : outlines.values()) {
			if (!entry.isAlive())
				continue;
			Outline outline = entry.getOutline();
			if (outline instanceof ValueBox && !((ValueBox) outline).isPassive) {
				return;
			}
		}

		dcg result = (dcg) objectMouseOver;
		KeyBinding mc = KeyBinding.B();
		DragonHeadEntityModel world = mc.r;
		BlockPos pos = result.a();
		ItemCooldownManager headSlot = mc.s.b(aqc.f);
		BeehiveBlockEntity te = world.c(pos);

		boolean wearingGoggles = AllItems.GOGGLES.isIn(headSlot);

		boolean hasGoggleInformation = te instanceof IHaveGoggleInformation;
		boolean hasHoveringInformation = te instanceof IHaveHoveringInformation;

		boolean goggleAddedInformation = false;
		boolean hoverAddedInformation = false;

		List<Text> tooltip = new ArrayList<>();

		if (hasGoggleInformation && wearingGoggles) {
			IHaveGoggleInformation gte = (IHaveGoggleInformation) te;
			goggleAddedInformation = gte.addToGoggleTooltip(tooltip, mc.s.bt());
		}

		if (hasHoveringInformation) {
			if (!tooltip.isEmpty())
				tooltip.add(LiteralText.EMPTY);
			IHaveHoveringInformation hte = (IHaveHoveringInformation) te;
			hoverAddedInformation = hte.addToTooltip(tooltip, mc.s.bt());

			if (goggleAddedInformation && !hoverAddedInformation)
				tooltip.remove(tooltip.size() - 1);
		}

		// break early if goggle or hover returned false when present
		if ((hasGoggleInformation && !goggleAddedInformation) && (hasHoveringInformation && !hoverAddedInformation))
			return;

		// check for piston poles if goggles are worn
		PistonHandler state = world.d_(pos);
		if (wearingGoggles && AllBlocks.PISTON_EXTENSION_POLE.has(state)) {
			Direction[] directions = Iterate.directionsInAxis(state.c(PistonExtensionPoleBlock.SHAPE)
				.getAxis());
			int poles = 1;
			boolean pistonFound = false;
			for (Direction dir : directions) {
				int attachedPoles = PistonExtensionPoleBlock.PlacementHelper.get().attachedPoles(world, pos, dir);
				poles += attachedPoles;
				pistonFound |= world.d_(pos.offset(dir, attachedPoles + 1))
					.b() instanceof MechanicalPistonBlock;
			}

			if (!pistonFound)
				return;
			if (!tooltip.isEmpty())
				tooltip.add(LiteralText.EMPTY);

			tooltip.add(IHaveGoggleInformation.componentSpacing.copy()
				.append(Lang.translate("gui.goggles.pole_length"))
				.append(new LiteralText(" " + poles)));
		}

		if (tooltip.isEmpty())
			return;

		ms.a();
		PresetsScreen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.b(mc, mc.aB()
			.o(),
			mc.aB()
				.p());
		int posX = tooltipScreen.k / 2 + AllConfigs.CLIENT.overlayOffsetX.get();
		int posY = tooltipScreen.l / 2 + AllConfigs.CLIENT.overlayOffsetY.get();
		// tooltipScreen.renderTooltip(tooltip, tooltipScreen.width / 2,
		// tooltipScreen.height / 2);
		tooltipScreen.b(ms, tooltip, posX, posY);

		ItemCooldownManager item = AllItems.GOGGLES.asStack();
		// GuiGameElement.of(item).at(tooltipScreen.width / 2 + 10, tooltipScreen.height
		// / 2 - 16).render();
		GuiGameElement.of(item)
			.atLocal(posX + 10, posY, 450)
			.render(ms);
		ms.b();
	}

	private static final class TooltipScreen extends PresetsScreen {
		private TooltipScreen(Text p_i51108_1_) {
			super(p_i51108_1_);
		}

		@Override
		public void b(KeyBinding mc, int width, int height) {
			this.i = mc;
			this.j = mc.ac();
			this.o = mc.category;
			this.k = width;
			this.l = height;
		}
	}

}
