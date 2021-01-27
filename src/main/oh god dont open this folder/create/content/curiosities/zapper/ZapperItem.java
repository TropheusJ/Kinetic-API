package com.simibubi.create.content.curiosities.zapper;

import java.util.List;

import javax.annotation.Nonnull;
import bnx;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.HoeItem;
import net.minecraft.item.SkullItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public abstract class ZapperItem extends HoeItem {

	public ZapperItem(a properties) {
		super(properties.a(1)
			.a(SkullItem.b));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(ItemCooldownManager stack, GameMode worldIn, List<Text> tooltip, ToolItem flagIn) {
		if (stack.n() && stack.o()
			.contains("BlockUsed")) {
			String usedblock = NbtHelper.c(stack.o()
				.getCompound("BlockUsed"))
				.b()
				.i();
			ItemDescription.add(tooltip,
				Lang.translate("blockzapper.usingBlock",
					new TranslatableText(usedblock).formatted(Formatting.GRAY))
					.formatted(Formatting.DARK_GRAY));
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemCooldownManager oldStack, ItemCooldownManager newStack, boolean slotChanged) {
		boolean differentBlock = false;
		if (oldStack.n() && newStack.n() && oldStack.o()
			.contains("BlockUsed")
			&& newStack.o()
				.contains("BlockUsed"))
			differentBlock = NbtHelper.c(oldStack.o()
				.getCompound("BlockUsed")) != NbtHelper.c(
					newStack.o()
						.getCompound("BlockUsed"));
		return slotChanged || !isZapper(newStack) || differentBlock;
	}

	public boolean isZapper(ItemCooldownManager newStack) {
		return newStack.b() instanceof ZapperItem;
	}

	@Nonnull
	@Override
	public Difficulty a(bnx context) {
		// Shift -> open GUI
		if (context.n() != null && context.n()
			.bt()) {
			if (context.p().v) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(context.m(), context.o() == ItemScatterer.b);
				});
				applyCooldown(context.n(), context.m(), false);
			}
			return Difficulty.SUCCESS;
		}
		return super.a(context);
	}

	@Override
	public LocalDifficulty<ItemCooldownManager> a(GameMode world, PlayerAbilities player, ItemScatterer hand) {
		ItemCooldownManager item = player.b(hand);
		CompoundTag nbt = item.p();

		// Shift -> Open GUI
		if (player.bt()) {
			if (world.v) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(item, hand == ItemScatterer.b);
				});
				applyCooldown(player, item, false);
			}
			return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, item);
		}

		boolean mainHand = hand == ItemScatterer.RANDOM;
		boolean isSwap = item.o()
			.contains("_Swap");
		boolean gunInOtherHand = isZapper(player.b(mainHand ? ItemScatterer.b : ItemScatterer.RANDOM));

		// Pass To Offhand
		if (mainHand && isSwap && gunInOtherHand)
			return new LocalDifficulty<ItemCooldownManager>(Difficulty.FAIL, item);
		if (mainHand && !isSwap && gunInOtherHand)
			item.o()
				.putBoolean("_Swap", true);
		if (!mainHand && isSwap)
			item.o()
				.remove("_Swap");
		if (!mainHand && gunInOtherHand)
			player.b(ItemScatterer.RANDOM)
				.o()
				.remove("_Swap");
		player.c(hand);

		// Check if can be used
		Text msg = validateUsage(item);
		if (msg != null) {
			world.a(player, player.cA(), AllSoundEvents.BLOCKZAPPER_DENY.get(), SoundEvent.e,
				1f, 0.5f);
			player.a(msg.copy().formatted(Formatting.RED), true);
			return new LocalDifficulty<ItemCooldownManager>(Difficulty.FAIL, item);
		}

		PistonHandler stateToUse = BellBlock.FACING.n();
		if (nbt.contains("BlockUsed"))
			stateToUse = NbtHelper.c(nbt.getCompound("BlockUsed"));
		stateToUse = BlockHelper.setZeroAge(stateToUse);
		CompoundTag data = null;
		if (AllBlockTags.SAFE_NBT.matches(stateToUse) && nbt.contains("BlockData", NBT.TAG_COMPOUND)) {
			data = nbt.getCompound("BlockData");
		}

		// Raytrace - Find the target
		EntityHitResult start = player.cz()
			.b(0, player.cd(), 0);
		EntityHitResult range = player.bg()
			.a(getZappingRange(item));
		dcg raytrace = world
			.a(new BlockView(start, start.e(range), net.minecraft.world.BlockView.a.b, b.a, player));
		BlockPos pos = raytrace.a();
		PistonHandler stateReplaced = world.d_(pos);

		// No target
		if (pos == null || stateReplaced.b() == BellBlock.FACING) {
			applyCooldown(player, item, gunInOtherHand);
			return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, item);
		}

		// Find exact position of gun barrel for VFX
		float yaw = (float) ((player.p) / -180 * Math.PI);
		float pitch = (float) ((player.q) / -180 * Math.PI);
		EntityHitResult barrelPosNoTransform =
			new EntityHitResult(mainHand == (player.dU() == EquipmentSlot.RIGHT) ? -.35f : .35f, -0.1f, 1);
		EntityHitResult barrelPos = start.e(barrelPosNoTransform.a(pitch)
			.b(yaw));

		// Client side
		if (world.v) {
			ZapperRenderHandler.dontAnimateItem(hand);
			return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, item);
		}

		// Server side
		if (activate(world, player, item, stateToUse, raytrace, data)) {
			applyCooldown(player, item, gunInOtherHand);
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
				new ZapperBeamPacket(barrelPos, raytrace.e(), hand, false));
			AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
				new ZapperBeamPacket(barrelPos, raytrace.e(), hand, true));
		}

		return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, item);
	}

	public Text validateUsage(ItemCooldownManager item) {
		CompoundTag tag = item.p();
		if (!canActivateWithoutSelectedBlock(item) && !tag.contains("BlockUsed"))
			return Lang.createTranslationTextComponent("blockzapper.leftClickToSet");
		return null;
	}

	protected abstract boolean activate(GameMode world, PlayerAbilities player, ItemCooldownManager item, PistonHandler stateToUse,
		dcg raytrace, CompoundTag data);

	@Environment(EnvType.CLIENT)
	protected abstract void openHandgunGUI(ItemCooldownManager item, boolean b);

	protected abstract int getCooldownDelay(ItemCooldownManager item);

	protected abstract int getZappingRange(ItemCooldownManager stack);

	protected boolean canActivateWithoutSelectedBlock(ItemCooldownManager stack) {
		return false;
	}

	protected void applyCooldown(PlayerAbilities playerIn, ItemCooldownManager item, boolean dual) {
		int delay = getCooldownDelay(item);
		playerIn.eS()
			.a(item.b(), dual ? delay * 2 / 3 : delay);
	}

	@Override
	public boolean onEntitySwing(ItemCooldownManager stack, SaddledComponent entity) {
		return true;
	}

	@Override
	public boolean a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player) {
		return false;
	}

	@Override
	public TippedArrowItem d_(ItemCooldownManager stack) {
		return TippedArrowItem.a;
	}

	public static void setTileData(GameMode world, BlockPos pos, PistonHandler state, CompoundTag data) {
		if (data != null) {
			BeehiveBlockEntity tile = world.c(pos);
			if (tile != null && !tile.t()) {
				data.putInt("x", pos.getX());
				data.putInt("y", pos.getY());
				data.putInt("z", pos.getZ());
				tile.a(state, data);
			}
		}
	}

}
