package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.glue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import apx;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.worldWrappers.RayTraceWorld;
import dcg;
import net.minecraft.block.BellBlock;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box.a;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class SuperGlueHandler {

	public static Map<Direction, SuperGlueEntity> gatherGlue(GrassColors world, BlockPos pos) {
		List<SuperGlueEntity> entities = world.a(SuperGlueEntity.class, new Timer(pos));
		Map<Direction, SuperGlueEntity> map = new HashMap<>();
		for (SuperGlueEntity entity : entities)
			map.put(entity.getAttachedDirection(pos), entity);
		return map;
	}

	@SubscribeEvent
	public static void glueListensForBlockPlacement(EntityPlaceEvent event) {
		GrassColors world = event.getWorld();
		apx entity = event.getEntity();
		BlockPos pos = event.getPos();

		if (entity == null || world == null || pos == null)
			return;
		if (world.s_())
			return;

		Map<Direction, SuperGlueEntity> gatheredGlue = gatherGlue(world, pos);
		for (Direction direction : gatheredGlue.keySet())
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
				new GlueEffectPacket(pos, direction, true));

		if (entity instanceof PlayerAbilities)
			glueInOffHandAppliesOnBlockPlace(event, pos, (PlayerAbilities) entity);
	}

	public static void glueInOffHandAppliesOnBlockPlace(EntityPlaceEvent event, BlockPos pos, PlayerAbilities placer) {
		ItemCooldownManager itemstack = placer.dD();
		TameableEntity reachAttribute = placer.a(ForgeMod.REACH_DISTANCE.get());
		if (!AllItems.SUPER_GLUE.isIn(itemstack) || reachAttribute == null)
			return;
		if (AllItems.WRENCH.isIn(placer.dC()))
			return;

		double distance = reachAttribute.f();
		EntityHitResult start = placer.j(1);
		EntityHitResult look = placer.f(1);
		EntityHitResult end = start.b(look.entity * distance, look.c * distance, look.d * distance);
		GameMode world = placer.l;

		RayTraceWorld rayTraceWorld =
			new RayTraceWorld(world, (p, state) -> p.equals(pos) ? BellBlock.FACING.n() : state);
		dcg ray = rayTraceWorld.a(
			new BlockView(start, end, BlockView.a.b, BlockView.b.a, placer));

		Direction face = ray.b();
		if (face == null || ray.c() == a.a)
			return;

		if (!ray.a()
			.offset(face)
			.equals(pos)) {
			event.setCanceled(true);
			return;
		}

		SuperGlueEntity entity = new SuperGlueEntity(world, ray.a(), face.getOpposite());
		CompoundTag compoundnbt = itemstack.o();
		if (compoundnbt != null)
			EntityDimensions.a(world, placer, entity, compoundnbt);

		if (entity.onValidSurface()) {
			if (!world.v) {
				entity.playPlaceSound();
				world.c(entity);
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
					new GlueEffectPacket(ray.a(), face, true));
			}
			itemstack.a(1, placer, SuperGlueItem::onBroken);
		}
	}

}
