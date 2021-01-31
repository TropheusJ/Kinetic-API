package com.simibubi.kinetic_api.content.logistics.block.mechanicalArm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ArmInteractionPointHandler {

	static List<ArmInteractionPoint> currentSelection = new ArrayList<>();
	static ItemCooldownManager currentItem;

	static long lastBlockPos = -1;

	@SubscribeEvent
	public static void rightClickingBlocksSelectsThem(PlayerInteractEvent.RightClickBlock event) {
		if (currentItem == null)
			return;
		BlockPos pos = event.getPos();
		GameMode world = event.getWorld();
		if (!world.v)
			return;

		ArmInteractionPoint selected = getSelected(pos);

		if (selected == null) {
			ArmInteractionPoint point = ArmInteractionPoint.createAt(world, pos);
			if (point == null)
				return;
			selected = point;
			put(point);
		}

		selected.cycleMode();
		PlayerAbilities player = event.getPlayer();
		if (player != null) {
			String key = selected.mode == Mode.DEPOSIT ? "mechanical_arm.deposit_to" : "mechanical_arm.extract_from";
			Formatting colour = selected.mode == Mode.DEPOSIT ? Formatting.GOLD : Formatting.AQUA;
			TranslatableText translatedBlock = new TranslatableText(selected.state.b()
				.i());
			player.a((Lang.translate(key, translatedBlock.formatted(Formatting.WHITE, colour)).formatted(colour)),
				true);
		}

		event.setCanceled(true);
		event.setCancellationResult(Difficulty.SUCCESS);
	}

	@SubscribeEvent
	public static void leftClickingBlocksDeselectsThem(PlayerInteractEvent.LeftClickBlock event) {
		if (currentItem == null)
			return;
		if (!event.getWorld().v)
			return;
		BlockPos pos = event.getPos();
		if (remove(pos) != null) {
			event.setCanceled(true);
			event.setCancellationResult(Difficulty.SUCCESS);
		}
	}

	public static void flushSettings(BlockPos pos) {
		if (currentItem == null)
			return;

		int removed = 0;
		for (Iterator<ArmInteractionPoint> iterator = currentSelection.iterator(); iterator.hasNext();) {
			ArmInteractionPoint point = iterator.next();
			if (point.pos.isWithinDistance(pos, ArmTileEntity.getRange()))
				continue;
			iterator.remove();
			removed++;
		}

		FishingParticle player = KeyBinding.B().s;
		if (removed > 0) {
			player.a(Lang.createTranslationTextComponent("mechanical_arm.points_outside_range", removed)
				.formatted(Formatting.RED), true);
		} else {
			int inputs = 0;
			int outputs = 0;
			for (ArmInteractionPoint armInteractionPoint : currentSelection) {
				if (armInteractionPoint.mode == Mode.DEPOSIT)
					outputs++;
				else
					inputs++;
			}
			if (inputs + outputs > 0)
				player.a(Lang.createTranslationTextComponent("mechanical_arm.summary", inputs, outputs)
					.formatted(Formatting.WHITE), true);
		}

		AllPackets.channel.sendToServer(new ArmPlacementPacket(currentSelection, pos));
		currentSelection.clear();
		currentItem = null;
	}

	public static void tick() {
		PlayerAbilities player = KeyBinding.B().s;

		if (player == null)
			return;

		ItemCooldownManager heldItemMainhand = player.dC();
		if (!AllBlocks.MECHANICAL_ARM.isIn(heldItemMainhand)) {
			currentItem = null;
		} else {
			if (heldItemMainhand != currentItem) {
				currentSelection.clear();
				currentItem = heldItemMainhand;
			}

			drawOutlines(currentSelection);
		}

		checkForWrench(heldItemMainhand);
	}

	private static void checkForWrench(ItemCooldownManager heldItem) {
		if (!AllItems.WRENCH.isIn(heldItem)) {
			return;
		}

		Box objectMouseOver = KeyBinding.B().v;
		if (!(objectMouseOver instanceof dcg)) {
			return;
		}

		dcg result = (dcg) objectMouseOver;
		BlockPos pos = result.a();

		BeehiveBlockEntity te = KeyBinding.B().r.c(pos);
		if (!(te instanceof ArmTileEntity)) {
			lastBlockPos = -1;
			currentSelection.clear();
			return;
		}

		if (lastBlockPos == -1 || lastBlockPos != pos.asLong()) {
			currentSelection.clear();
			ArmTileEntity arm = (ArmTileEntity) te;
			arm.inputs.forEach(ArmInteractionPointHandler::put);
			arm.outputs.forEach(ArmInteractionPointHandler::put);
			lastBlockPos = pos.asLong();
		}

		if (lastBlockPos != -1) {
			drawOutlines(currentSelection);
		}
	}

	private static void drawOutlines(Collection<ArmInteractionPoint> selection) {
		GameMode world = KeyBinding.B().r;
		for (Iterator<ArmInteractionPoint> iterator = selection.iterator(); iterator.hasNext();) {
			ArmInteractionPoint point = iterator.next();
			BlockPos pos = point.pos;
			PistonHandler state = world.d_(pos);

			if (!point.isValid(world, pos, state)) {
				iterator.remove();
				continue;
			}

			VoxelShapes shape = state.j(world, pos);
			if (shape.b())
				continue;

			int color = point.mode == Mode.DEPOSIT ? 0xffcb74 : 0x4f8a8b;
			CreateClient.outliner.showAABB(point, shape.a()
				.a(pos))
				.colored(color)
				.lineWidth(1 / 16f);
		}
	}

	private static void put(ArmInteractionPoint point) {
		currentSelection.add(point);
	}

	private static ArmInteractionPoint remove(BlockPos pos) {
		ArmInteractionPoint result = getSelected(pos);
		if (result != null)
			currentSelection.remove(result);
		return result;
	}

	private static ArmInteractionPoint getSelected(BlockPos pos) {
		for (ArmInteractionPoint point : currentSelection) {
			if (point.pos.equals(pos))
				return point;
		}
		return null;
	}

}
