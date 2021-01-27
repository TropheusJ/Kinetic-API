package com.simibubi.create.content.curiosities.zapper;

import java.util.Objects;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.utility.BlockHelper;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.RailShape;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.a;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.Difficulty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ZapperInteractionHandler {

	@SubscribeEvent
	public static void leftClickingBlocksWithTheZapperSelectsTheBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getWorld().v)
			return;
		ItemCooldownManager heldItem = event.getPlayer().dC();
		if (heldItem.b() instanceof ZapperItem && trySelect(heldItem, event.getPlayer())) {
			event.setCancellationResult(Difficulty.FAIL);
			event.setCanceled(true);
		}
	}

	public static boolean trySelect(ItemCooldownManager stack, PlayerAbilities player) {
		if (player.bt())
			return false;

		EntityHitResult start = player.cz()
			.b(0, player.cd(), 0);
		EntityHitResult range = player.bg()
			.a(getRange(stack));
		dcg raytrace = player.l
			.a(new BlockView(start, start.e(range), a.b, b.a, player));
		BlockPos pos = raytrace.a();
		if (pos == null)
			return false;

		player.l.a(player.X(), pos, -1);
		PistonHandler newState = player.l.d_(pos);

		if (BlockHelper.getRequiredItem(newState)
			.a())
			return false;
		if (newState.hasTileEntity() && !AllBlockTags.SAFE_NBT.matches(newState))
			return false;
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.aa))
			return false;
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.field_12469))
			return false;
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.j))
			return false;
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.aE))
			return false;
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.aL))
			newState = newState.a(BambooLeaves.aL, RailShape.STRAIGHT);
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.v))
			newState = newState.a(BambooLeaves.v, true);
		if (BlockHelper.hasBlockStateProperty(newState, BambooLeaves.C))
			newState = newState.a(BambooLeaves.C, false);

		CompoundTag data = null;
		BeehiveBlockEntity tile = player.l.c(pos);
		if (tile != null) {
			data = tile.a(new CompoundTag());
			data.remove("x");
			data.remove("y");
			data.remove("z");
			data.remove("id");
		}
		CompoundTag tag = stack.p();
		if (tag.contains("BlockUsed")
				&& NbtHelper.c(
						stack.o().getCompound("BlockUsed")) == newState
				&& Objects.equals(data, tag.get("BlockData"))) {
			return false;
		}

		tag.put("BlockUsed", NbtHelper.a(newState));
		if (data == null)
			tag.remove("BlockData");
		else
			tag.put("BlockData", data);
		player.l.a(null, player.cA(), AllSoundEvents.BLOCKZAPPER_CONFIRM.get(),
			SoundEvent.e, 0.5f, 0.8f);

		return true;
	}

	public static int getRange(ItemCooldownManager stack) {
		if (stack.b() instanceof ZapperItem)
			return ((ZapperItem) stack.b()).getZappingRange(stack);
		return 0;
	}
}
