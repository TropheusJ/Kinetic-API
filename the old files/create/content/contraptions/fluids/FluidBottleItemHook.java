package com.simibubi.kinetic_api.content.contraptions.fluids;

import com.simibubi.kinetic_api.Create;
import dcg;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.BoatItem;
import net.minecraft.item.HoeItem;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FluidBottleItemHook extends HoeItem {

	public FluidBottleItemHook(a p_i48487_1_) {
		super(p_i48487_1_);
	}

	@SubscribeEvent
	public static void preventWaterBottlesFromCreatesFluids(PlayerInteractEvent.RightClickItem event) {
		ItemCooldownManager itemStack = event.getItemStack();
		if (itemStack.a())
			return;
		if (!(itemStack.b() instanceof BoatItem))
			return;

		GameMode world = event.getWorld();
		PlayerAbilities player = event.getPlayer();
		Box raytraceresult = a(world, player, BlockView.b.b);
		if (raytraceresult.c() != Box.a.b)
			return;
		BlockPos blockpos = ((dcg) raytraceresult).a();
		if (!world.a(player, blockpos))
			return;

		EmptyFluid fluidState = world.b(blockpos);
		if (fluidState.a(BlockTags.field_15481) && fluidState.a()
			.getRegistryName()
			.getNamespace()
			.equals(Create.ID)) {
			event.setCancellationResult(Difficulty.PASS);
			event.setCanceled(true);
			return;
		}

		return;
	}

}
