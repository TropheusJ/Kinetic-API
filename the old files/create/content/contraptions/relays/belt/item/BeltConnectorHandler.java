package com.simibubi.kinetic_api.content.contraptions.relays.belt.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import dcg;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public class BeltConnectorHandler {

	private static Random r = new Random();

	public static void tick() {
		PlayerAbilities player = KeyBinding.B().s;
		GameMode world = KeyBinding.B().r;

		if (player == null || world == null)
			return;
		if (KeyBinding.B().y != null)
			return;

		for (ItemScatterer hand : ItemScatterer.values()) {
			ItemCooldownManager heldItem = player.b(hand);

			if (!AllItems.BELT_CONNECTOR.isIn(heldItem))
				continue;
			if (!heldItem.n())
				continue;

			CompoundTag tag = heldItem.o();
			if (!tag.contains("FirstPulley"))
				continue;

			BlockPos first = NbtHelper.toBlockPos(tag.getCompound("FirstPulley"));

			if (!BlockHelper.hasBlockStateProperty(world.d_(first), BambooLeaves.F))
				continue;
			Axis axis = world.d_(first)
				.c(BambooLeaves.F);

			Box rayTrace = KeyBinding.B().v;
			if (rayTrace == null || !(rayTrace instanceof dcg)) {
				if (r.nextInt(50) == 0) {
					world.addParticle(new DustParticleEffect(.3f, .9f, .5f, 1),
						first.getX() + .5f + randomOffset(.25f), first.getY() + .5f + randomOffset(.25f),
						first.getZ() + .5f + randomOffset(.25f), 0, 0, 0);
				}
				return;
			}

			BlockPos selected = ((dcg) rayTrace).a();

			if (world.d_(selected)
				.c()
				.e())
				return;
			if (!ShaftBlock.isShaft(world.d_(selected)))
				selected = selected.offset(((dcg) rayTrace).b());
			if (!selected.isWithinDistance(first, AllConfigs.SERVER.kinetics.maxBeltLength.get()))
				return;

			boolean canConnect =
				BeltConnectorItem.validateAxis(world, selected) && BeltConnectorItem.canConnect(world, first, selected);

			EntityHitResult start = EntityHitResult.b(first);
			EntityHitResult end = EntityHitResult.b(selected);
			EntityHitResult actualDiff = end.d(start);
			end = end.a(axis.choose(actualDiff.entity, 0, 0), axis.choose(0, actualDiff.c, 0),
				axis.choose(0, 0, actualDiff.d));
			EntityHitResult diff = end.d(start);

			double x = Math.abs(diff.entity);
			double y = Math.abs(diff.c);
			double z = Math.abs(diff.d);
			float length = (float) Math.max(x, Math.max(y, z));
			EntityHitResult step = diff.d();

			int sames = ((x == y) ? 1 : 0) + ((y == z) ? 1 : 0) + ((z == x) ? 1 : 0);
			if (sames == 0) {
				List<EntityHitResult> validDiffs = new LinkedList<>();
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
						for (int k = -1; k <= 1; k++) {
							if (axis.choose(i, j, k) != 0)
								continue;
							if (axis == Axis.Y && i != 0 && k != 0)
								continue;
							if (i == 0 && j == 0 && k == 0)
								continue;
							validDiffs.add(new EntityHitResult(i, j, k));
						}
				int closestIndex = 0;
				float closest = Float.MAX_VALUE;
				for (EntityHitResult validDiff : validDiffs) {
					double distanceTo = step.f(validDiff);
					if (distanceTo < closest) {
						closest = (float) distanceTo;
						closestIndex = validDiffs.indexOf(validDiff);
					}
				}
				step = validDiffs.get(closestIndex);
			}

			if (axis == Axis.Y && step.entity != 0 && step.d != 0)
				return;

			step = new EntityHitResult(Math.signum(step.entity), Math.signum(step.c), Math.signum(step.d));
			for (float f = 0; f < length; f += .0625f) {
				EntityHitResult position = start.e(step.a(f));
				if (r.nextInt(10) == 0) {
					world.addParticle(new DustParticleEffect(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1),
						position.entity + .5f, position.c + .5f, position.d + .5f, 0, 0, 0);
				}
			}

			return;
		}
	}

	private static float randomOffset(float range) {
		return (r.nextFloat() - .5f) * 2 * range;
	}

}
