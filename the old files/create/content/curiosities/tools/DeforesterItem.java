package com.simibubi.kinetic_api.content.curiosities.tools;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllTags;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.TreeCutter;
import com.simibubi.kinetic_api.foundation.utility.TreeCutter.Tree;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class DeforesterItem extends ArmorMaterials {

	public DeforesterItem(a builder) {
		super(AllToolTiers.RADIANT, 5.0F, -3.1F, builder);
	}

	// Moved away from Item#onBlockDestroyed as it does not get called in Creative
	public static void destroyTree(ItemCooldownManager stack, GrassColors iWorld, PistonHandler state, BlockPos pos,
			PlayerAbilities player) {
		if (!(state.a(StatHandler.s) || AllTags.AllBlockTags.SLIMY_LOGS.matches(state)) || player.bt() || !(iWorld instanceof  GameMode))
			return;
		GameMode worldIn = (GameMode) iWorld;
		Tree tree = TreeCutter.cutTree(worldIn, pos);
		if (tree == null)
			return;
		boolean dropBlock = !player.b_();

		EntityHitResult vec = player.bg();
		for (BlockPos log : tree.logs)
			BlockHelper.destroyBlock(worldIn, log, 1 / 2f, item -> {
				if (dropBlock) {
					dropItemFromCutTree(worldIn, pos, vec, log, item);
					stack.a(1, player, p -> p.d(ItemScatterer.RANDOM));
				}
			});
		for (BlockPos leaf : tree.leaves)
			BlockHelper.destroyBlock(worldIn, leaf, 1 / 8f, item -> {
				if (dropBlock)
					dropItemFromCutTree(worldIn, pos, vec, leaf, item);
			});
	}

	@SubscribeEvent
	public static void onBlockDestroyed(BlockEvent.BreakEvent event) {
		ItemCooldownManager heldItemMainhand = event.getPlayer().dC();
		if (!AllItems.DEFORESTER.isIn(heldItemMainhand))
			return;
		destroyTree(heldItemMainhand, event.getWorld(), event.getState(), event.getPos(), event.getPlayer());
	}

	public static void dropItemFromCutTree(GameMode world, BlockPos breakingPos, EntityHitResult fallDirection, BlockPos pos,
			ItemCooldownManager stack) {
		float distance = (float) Math.sqrt(pos.getSquaredDistance(breakingPos));
		EntityHitResult dropPos = VecHelper.getCenterOf(pos);
		PaintingEntity entity = new PaintingEntity(world, dropPos.entity, dropPos.c, dropPos.d, stack);
		entity.f(fallDirection.a(distance / 20f));
		world.c(entity);
	}

}
