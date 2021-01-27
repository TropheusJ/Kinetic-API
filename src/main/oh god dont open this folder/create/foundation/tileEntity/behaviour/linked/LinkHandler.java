package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import java.util.Arrays;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;
import dcg;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LinkHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		GameMode world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerAbilities player = event.getPlayer();
		ItemScatterer hand = event.getHand();
		
		if (player.bt())
			return;

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		dcg ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;

		for (boolean first : Arrays.asList(false, true)) {
			if (behaviour.testHit(first, ray.e())) {
				if (event.getSide() != LogicalSide.CLIENT)
					behaviour.setFrequency(first, player.b(hand));
				event.setCanceled(true);
				event.setCancellationResult(Difficulty.SUCCESS);
				world.a(null, pos, MusicType.gF, SoundEvent.e, .25f, .1f);
			}
		}
	}

}
