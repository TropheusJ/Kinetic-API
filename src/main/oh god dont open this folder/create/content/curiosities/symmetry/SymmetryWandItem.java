package com.simibubi.create.content.curiosities.symmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import bnx;
import com.simibubi.create.content.curiosities.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.PlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public class SymmetryWandItem extends HoeItem {

	public static final String SYMMETRY = "symmetry";
	private static final String ENABLE = "enable";

	public SymmetryWandItem(a properties) {
		super(properties.a(1)
			.a(SkullItem.b));
	}

	@Nonnull
	@Override
	public Difficulty a(bnx context) {
		PlayerAbilities player = context.n();
		BlockPos pos = context.a();
		if (player == null)
			return Difficulty.PASS;
		player.eS()
			.a(this, 5);
		ItemCooldownManager wand = player.b(context.o());
		checkNBT(wand);

		// Shift -> open GUI
		if (player.bt()) {
			if (player.l.v) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(wand, context.o());
				});
				player.eS()
					.a(this, 5);
			}
			return Difficulty.SUCCESS;
		}

		if (context.p().v || context.o() != ItemScatterer.RANDOM)
			return Difficulty.SUCCESS;

		CompoundTag compound = wand.o()
			.getCompound(SYMMETRY);
		pos = pos.offset(context.j());
		SymmetryMirror previousElement = SymmetryMirror.fromNBT(compound);

		// No Shift -> Make / Move Mirror
		wand.o()
			.putBoolean(ENABLE, true);
		EntityHitResult pos3d = new EntityHitResult(pos.getX(), pos.getY(), pos.getZ());
		SymmetryMirror newElement = new PlaneMirror(pos3d);

		if (previousElement instanceof EmptyMirror) {
			newElement.setOrientation(
				(player.bY() == Direction.NORTH || player.bY() == Direction.SOUTH)
					? PlaneMirror.Align.XY.ordinal()
					: PlaneMirror.Align.YZ.ordinal());
			newElement.enable = true;
			wand.o()
				.putBoolean(ENABLE, true);

		} else {
			previousElement.setPosition(pos3d);

			if (previousElement instanceof PlaneMirror) {
				previousElement.setOrientation(
					(player.bY() == Direction.NORTH || player.bY() == Direction.SOUTH)
						? PlaneMirror.Align.XY.ordinal()
						: PlaneMirror.Align.YZ.ordinal());
			}

			if (previousElement instanceof CrossPlaneMirror) {
				float rotation = player.bJ();
				float abs = Math.abs(rotation % 90);
				boolean diagonal = abs > 22 && abs < 45 + 22;
				previousElement
					.setOrientation(diagonal ? CrossPlaneMirror.Align.D.ordinal() : CrossPlaneMirror.Align.Y.ordinal());
			}

			newElement = previousElement;
		}

		compound = newElement.writeToNbt();
		wand.o()
			.put(SYMMETRY, compound);

		player.a(context.o(), wand);
		return Difficulty.SUCCESS;
	}

	@Override
	public LocalDifficulty<ItemCooldownManager> a(GameMode worldIn, PlayerAbilities playerIn, ItemScatterer handIn) {
		ItemCooldownManager wand = playerIn.b(handIn);
		checkNBT(wand);

		// Shift -> Open GUI
		if (playerIn.bt()) {
			if (worldIn.v) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(playerIn.b(handIn), handIn);
				});
				playerIn.eS()
					.a(this, 5);
			}
			return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, wand);
		}

		// No Shift -> Clear Mirror
		wand.o()
			.putBoolean(ENABLE, false);
		return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, wand);
	}

	@Environment(EnvType.CLIENT)
	private void openWandGUI(ItemCooldownManager wand, ItemScatterer hand) {
		ScreenOpener.open(new SymmetryWandScreen(wand, hand));
	}

	private static void checkNBT(ItemCooldownManager wand) {
		if (!wand.n() || !wand.o()
			.contains(SYMMETRY)) {
			wand.c(new CompoundTag());
			wand.o()
				.put(SYMMETRY, new EmptyMirror(new EntityHitResult(0, 0, 0)).writeToNbt());
			wand.o()
				.putBoolean(ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemCooldownManager stack) {
		checkNBT(stack);
		return stack.o()
			.getBoolean(ENABLE);
	}

	public static SymmetryMirror getMirror(ItemCooldownManager stack) {
		checkNBT(stack);
		return SymmetryMirror.fromNBT((CompoundTag) stack.o()
			.getCompound(SYMMETRY));
	}

	public static void apply(GameMode world, ItemCooldownManager wand, PlayerAbilities player, BlockPos pos, PistonHandler block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;
		if (!BannerItem.e.containsKey(block.b()))
			return;

		Map<BlockPos, PistonHandler> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundTag) wand.o()
			.getCompound(SYMMETRY));

		EntityHitResult mirrorPos = symmetry.getPosition();
		if (mirrorPos.f(EntityHitResult.b(pos)) > AllConfigs.SERVER.curiosities.maxSymmetryWandRange.get())
			return;
		if (!player.b_() && isHoldingBlock(player, block)
			&& BlockHelper.findAndRemoveInInventory(block, player, 1) == 0)
			return;

		symmetry.process(blockSet);
		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();
		targets.add(pos);

		for (BlockPos position : blockSet.keySet()) {
			if (position.equals(pos))
				continue;

			if (world.a(block, position, ArrayVoxelShape.a(player))) {
				PistonHandler blockState = blockSet.get(position);
				for (Direction face : Iterate.directions)
					blockState = blockState.a(face, world.d_(position.offset(face)), world,
						position, position.offset(face));

				if (player.b_()) {
					world.a(position, blockState);
					targets.add(position);
					continue;
				}

				PistonHandler toReplace = world.d_(position);
				if (!toReplace.c()
					.e())
					continue;
				if (toReplace.h(world, position) == -1)
					continue;
				if (BlockHelper.findAndRemoveInInventory(blockState, player, 1) == 0)
					continue;

				world.a(position, blockState);
				targets.add(position);
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

	private static boolean isHoldingBlock(PlayerAbilities player, PistonHandler block) {
		ItemCooldownManager itemBlock = BlockHelper.getRequiredItem(block);
		return player.dC()
			.a(itemBlock)
			|| player.dD()
				.a(itemBlock);
	}

	public static void remove(GameMode world, ItemCooldownManager wand, PlayerAbilities player, BlockPos pos) {
		PistonHandler air = BellBlock.FACING.n();
		PistonHandler ogBlock = world.d_(pos);
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, PistonHandler> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundTag) wand.o()
			.getCompound(SYMMETRY));

		EntityHitResult mirrorPos = symmetry.getPosition();
		if (mirrorPos.f(EntityHitResult.b(pos)) > AllConfigs.SERVER.curiosities.maxSymmetryWandRange.get())
			return;

		symmetry.process(blockSet);

		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (!player.b_() && ogBlock.b() != world.d_(position)
				.b())
				continue;
			if (position.equals(pos))
				continue;

			PistonHandler blockstate = world.d_(position);
			if (!blockstate.isAir(world, position)) {
				targets.add(position);
				world.syncWorldEvent(2001, position, BeetrootsBlock.i(blockstate));
				world.a(position, air, 3);

				if (!player.b_()) {
					if (!player.dC()
						.a())
						player.dC()
							.a(world, blockstate, position, player);
					BeehiveBlockEntity tileentity = blockstate.hasTileEntity() ? world.c(position) : null;
					BeetrootsBlock.a(blockstate, world, pos, tileentity, player, player.dC()); // Add fortune, silk touch and other loot modifiers
				}
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

}
