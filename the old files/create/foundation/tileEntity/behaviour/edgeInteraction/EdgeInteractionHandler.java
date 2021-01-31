package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.edgeInteraction;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.RaycastHelper;
import dcg;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class EdgeInteractionHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		GameMode world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerAbilities player = event.getPlayer();
		ItemScatterer hand = event.getHand();
		ItemCooldownManager heldItem = player.b(hand);

		if (player.bt())
			return;
		EdgeInteractionBehaviour behaviour = TileEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
		if (behaviour == null)
			return;
		dcg ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (behaviour.requiredItem.orElse(heldItem.b()) != heldItem.b())
			return;

		Direction activatedDirection = getActivatedDirection(world, pos, ray.b(), ray.e(), behaviour);
		if (activatedDirection == null)
			return;

		if (event.getSide() != LogicalSide.CLIENT)
			behaviour.connectionCallback.apply(world, pos, pos.offset(activatedDirection));
		event.setCanceled(true);
		event.setCancellationResult(Difficulty.SUCCESS);
		world.a(null, pos, MusicType.gF, SoundEvent.e, .25f, .1f);
	}

	public static List<Direction> getConnectiveSides(GameMode world, BlockPos pos, Direction face,
		EdgeInteractionBehaviour behaviour) {
		List<Direction> sides = new ArrayList<>(6);
		if (BlockHelper.hasBlockSolidSide(world.d_(pos.offset(face)), world, pos.offset(face), face.getOpposite()))
			return sides;

		for (Direction direction : Iterate.directions) {
			if (direction.getAxis() == face.getAxis())
				continue;
			BlockPos neighbourPos = pos.offset(direction);
			if (BlockHelper.hasBlockSolidSide(world.d_(neighbourPos.offset(face)), world, neighbourPos.offset(face),
				face.getOpposite()))
				continue;
			if (!behaviour.connectivityPredicate.test(world, pos, face, direction))
				continue;
			sides.add(direction);
		}

		return sides;
	}

	public static Direction getActivatedDirection(GameMode world, BlockPos pos, Direction face, EntityHitResult hit,
		EdgeInteractionBehaviour behaviour) {
		for (Direction facing : getConnectiveSides(world, pos, face, behaviour)) {
			Timer bb = getBB(pos, facing);
			if (bb.d(hit))
				return facing;
		}
		return null;
	}

	static Timer getBB(BlockPos pos, Direction direction) {
		Timer bb = new Timer(pos);
		Vec3i vec = direction.getVector();
		int x = vec.getX();
		int y = vec.getY();
		int z = vec.getZ();
		double margin = 12 / 16f;
		double absX = Math.abs(x) * margin;
		double absY = Math.abs(y) * margin;
		double absZ = Math.abs(z) * margin;

		bb = bb.a(absX, absY, absZ);
		bb = bb.d(absX / 2d, absY / 2d, absZ / 2d);
		bb = bb.d(x / 2d, y / 2d, z / 2d);
		bb = bb.g(1 / 256f);
		return bb;
	}

}
