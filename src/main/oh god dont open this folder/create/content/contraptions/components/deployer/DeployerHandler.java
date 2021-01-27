package com.simibubi.create.content.contraptions.components.deployer;

import static net.minecraftforge.eventbus.api.Event.Result.DEFAULT;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

import apx;
import aqc;
import bnx;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import cut;
import dcg;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.CoralParentBlock;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.enchantment.EfficiencyEnchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.StatFormatter;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.a;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;

public class DeployerHandler {

	private static final class ItemUseWorld extends WrappedWorld {
		private final Direction face;
		private final BlockPos pos;
		boolean rayMode = false;

		private ItemUseWorld(GameMode world, Direction face, BlockPos pos) {
			super(world);
			this.face = face;
			this.pos = pos;
		}

		@Override
		public dcg a(BlockView context) {
			rayMode = true;
			dcg rayTraceBlocks = super.a(context);
			rayMode = false;
			return rayTraceBlocks;
		}

		@Override
		public PistonHandler d_(BlockPos position) {
			if (rayMode && (pos.offset(face.getOpposite(), 3)
				.equals(position)
				|| pos.offset(face.getOpposite(), 1)
					.equals(position)))
				return BellBlock.z.n();
			return world.d_(position);
		}
	}

	static boolean shouldActivate(ItemCooldownManager held, GameMode world, BlockPos targetPos) {
		if (held.b() instanceof BannerItem)
			if (world.d_(targetPos)
				.b() == ((BannerItem) held.b()).e())
				return false;

		if (held.b() instanceof GlassBottleItem) {
			GlassBottleItem bucketItem = (GlassBottleItem) held.b();
			cut fluid = bucketItem.getFluid();
			if (fluid != FlowableFluid.FALLING && world.b(targetPos)
				.a() == fluid)
				return false;
		}

		return true;
	}

	static void activate(DeployerFakePlayer player, EntityHitResult vec, BlockPos clickedPos, EntityHitResult extensionVector, Mode mode) {
		Multimap<SpawnRestriction, EntityAttribute> attributeModifiers = player.dC()
			.a(aqc.a);
		player.dA()
			.b(attributeModifiers);
		activateInner(player, vec, clickedPos, extensionVector, mode);
		player.dA()
			.b(attributeModifiers);
	}

	private static void activateInner(DeployerFakePlayer player, EntityHitResult vec, BlockPos clickedPos, EntityHitResult extensionVector,
		Mode mode) {

		EntityHitResult rayOrigin = vec.e(extensionVector.a(3 / 2f + 1 / 64f));
		EntityHitResult rayTarget = vec.e(extensionVector.a(5 / 2f - 1 / 64f));
		player.d(rayOrigin.entity, rayOrigin.c, rayOrigin.d);
		BlockPos pos = new BlockPos(vec);
		ItemCooldownManager stack = player.dC();
		HoeItem item = stack.b();

		// Check for entities
		final ServerWorld world = player.getServerWorld();
		List<apx> entities = world.a(apx.class, new Timer(clickedPos));
		ItemScatterer hand = ItemScatterer.RANDOM;
		if (!entities.isEmpty()) {
			apx entity = entities.get(world.t.nextInt(entities.size()));
			List<PaintingEntity> capturedDrops = new ArrayList<>();
			boolean success = false;
			entity.captureDrops(capturedDrops);

			// Use on entity
			if (mode == Mode.USE) {
				Difficulty cancelResult = ForgeHooks.onInteractEntity(player, entity, hand);
				if (cancelResult == Difficulty.FAIL) {
					entity.captureDrops(null);
					return;
				}
				if (cancelResult == null) {
					if (entity.a(player, hand).a())
						success = true;
					else if (entity instanceof SaddledComponent
						&& stack.a(player, (SaddledComponent) entity, hand).a())
						success = true;
				}
				if (!success && stack.F() && entity instanceof PlayerAbilities) {
					PlayerAbilities playerEntity = (PlayerAbilities) entity;
					if (playerEntity.q(item.t()
						.d())) {
						playerEntity.a(world, stack);
						player.spawnedItemEffects = stack.i();
						success = true;
					}
				}
			}

			// Punch entity
			if (mode == Mode.PUNCH) {
				player.eR();
				player.f(entity);
				success = true;
			}

			entity.captureDrops(null);
			capturedDrops.forEach(e -> player.bm.a(world, e.g()));
			if (success)
				return;
		}

		// Shoot ray
		BlockView rayTraceContext =
			new BlockView(rayOrigin, rayTarget, a.b, b.a, player);
		dcg result = world.a(rayTraceContext);
		if (result.a() != clickedPos)
			result = new dcg(result.e(), result.b(), clickedPos, result.d());
		PistonHandler clickedState = world.d_(clickedPos);
		Direction face = result.b();
		if (face == null)
			face = Direction.getFacing(extensionVector.entity, extensionVector.c, extensionVector.d)
				.getOpposite();

		// Left click
		if (mode == Mode.PUNCH) {
			if (!world.a(player, clickedPos))
				return;
			if (clickedState.j(world, clickedPos)
				.b()) {
				player.blockBreakingProgress = null;
				return;
			}
			LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, clickedPos, face);
			if (event.isCanceled())
				return;
			if (BlockHelper.extinguishFire(world, player, clickedPos, face)) // FIXME: is there an equivalent in world, as there was in 1.15?
				return;
			if (event.getUseBlock() != DENY)
				clickedState.a(world, clickedPos, player);
			if (stack.a())
				return;

			float progress = clickedState.a(player, world, clickedPos) * 16;
			float before = 0;
			Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
			if (blockBreakingProgress != null)
				before = blockBreakingProgress.getValue();
			progress += before;

			if (progress >= 1) {
				safeTryHarvestBlock(player.interactionManager, clickedPos);
				world.a(player.X(), clickedPos, -1);
				player.blockBreakingProgress = null;
				return;
			}
			if (progress <= 0) {
				player.blockBreakingProgress = null;
				return;
			}

			if ((int) (before * 10) != (int) (progress * 10))
				world.a(player.X(), clickedPos, (int) (progress * 10));
			player.blockBreakingProgress = Pair.of(clickedPos, progress);
			return;
		}

		// Right click
		bnx itemusecontext = new bnx(player, hand, result);
		Event.Result useBlock = DEFAULT;
		Event.Result useItem = DEFAULT;
		if (!clickedState.j(world, clickedPos)
			.b()) {
			RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, clickedPos, face);
			useBlock = event.getUseBlock();
			useItem = event.getUseItem();
		}

		// Item has custom active use
		if (useItem != DENY) {
			Difficulty actionresult = stack.onItemUseFirst(itemusecontext);
			if (actionresult != Difficulty.PASS)
				return;
		}

		boolean holdingSomething = !player.dC()
			.a();
		boolean flag1 =
			!(player.bt() && holdingSomething) || (stack.doesSneakBypassUse(world, clickedPos, player));

		if (clickedState.b() instanceof Stainable)
			return; // Beehives assume a lot about the usage context. Crashes to side-effects

		// Use on block
		if (useBlock != DENY && flag1 && clickedState.a(world, player, hand, result) == Difficulty.SUCCESS)
			return;
		if (stack.a())
			return;
		if (useItem == DENY)
			return;
		if (item instanceof BannerItem && !clickedState.a(new PotionUtil(itemusecontext)))
			return;

		// Reposition fire placement for convenience
		if (item == AliasedBlockItem.ka) {
			Direction newFace = result.b();
			BlockPos newPos = result.a();
			if (!CoralParentBlock.a(world, clickedPos, newFace))
				newFace = Direction.UP;
			if (clickedState.c() == FluidState.CODEC)
				newPos = newPos.offset(face.getOpposite());
			result = new dcg(result.e(), newFace, newPos, result.d());
			itemusecontext = new bnx(player, hand, result);
		}

		// 'Inert' item use behaviour & block placement
		Difficulty onItemUse = stack.a(itemusecontext);
		if (onItemUse == Difficulty.SUCCESS)
			return;
		if (item == AliasedBlockItem.nq)
			return;

		// buckets create their own ray, We use a fake wall to contain the active area
		GameMode itemUseWorld = world;
		if (item instanceof GlassBottleItem || item instanceof SandPaperItem)
			itemUseWorld = new ItemUseWorld(world, face, pos);

		LocalDifficulty<ItemCooldownManager> onItemRightClick = item.a(itemUseWorld, player, hand);
		ItemCooldownManager resultStack = onItemRightClick.b();
		if (resultStack != stack || resultStack.E() != stack.E() || resultStack.k() > 0 || resultStack.g() != stack.g()) {
			player.a(hand, onItemRightClick.b());
		}

		CompoundTag tag = stack.o();
		if (tag != null && stack.b() instanceof SandPaperItem && tag.contains("Polishing"))
			player.spawnedItemEffects = ItemCooldownManager.a(tag.getCompound("Polishing"));

		if (!player.dX()
			.a())
			player.a(hand, stack.a(world, player));

		player.eb();
	}

	private static boolean safeTryHarvestBlock(ServerPlayerInteractionManager interactionManager, BlockPos clickedPos) {
		PistonHandler state = interactionManager.world.d_(clickedPos);
		if (!(state.b() instanceof Stainable))
			return interactionManager.tryBreakBlock(clickedPos);
		else {
			harvestBeehive(interactionManager, state, clickedPos);
		}
		return true;
	}

	private static void harvestBeehive(ServerPlayerInteractionManager interactionManager, PistonHandler state,
		BlockPos clickedPos) {
		// Modified code from PlayerInteractionManager, Block and BeehiveBlock to handle
		// deployers breaking beehives without crash.
		ItemCooldownManager itemstack = interactionManager.player.dC();
		ItemCooldownManager itemstack1 = itemstack.i();

		boolean flag1 = state.canHarvestBlock(interactionManager.world, clickedPos, interactionManager.player);
		itemstack.a(interactionManager.world, state, clickedPos, interactionManager.player);
		if (itemstack.a() && !itemstack1.a())
			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(interactionManager.player, itemstack1,
				ItemScatterer.RANDOM);

		boolean flag = state.removedByPlayer(interactionManager.world, clickedPos, interactionManager.player, flag1,
			interactionManager.world.b(clickedPos));
		if (flag)
			state.b()
				.a(interactionManager.world, clickedPos, state);

		if (flag && flag1) {
			interactionManager.player.b(StatFormatter.DECIMAL_FORMAT.b(state.b()));
			interactionManager.player.t(0.005F);
			BeehiveBlockEntity te = interactionManager.world.c(clickedPos);
			ItemCooldownManager heldItem = interactionManager.player.dC();
			BeetrootsBlock.a(state, interactionManager.world, clickedPos, te, interactionManager.player, heldItem);

			if (!interactionManager.world.v && te instanceof LockableContainerBlockEntity) {
				LockableContainerBlockEntity beehivetileentity = (LockableContainerBlockEntity) te;
				if (EfficiencyEnchantment.a(EnchantmentTarget.u, heldItem) == 0) {
					interactionManager.world.c(clickedPos, state.b());
				}

				Criteria.BEE_NEST_DESTROYED.a(interactionManager.player,
					state.b(), heldItem, beehivetileentity.getBeeCount());
			}
		}
	}
}
