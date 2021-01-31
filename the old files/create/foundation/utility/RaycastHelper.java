package com.simibubi.kinetic_api.foundation.utility;

import afj;
import dcg;
import java.util.function.Predicate;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.a;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.GameMode;

public class RaycastHelper {

	public static dcg rayTraceRange(GameMode worldIn, PlayerAbilities playerIn, double range) {
		EntityHitResult origin = getTraceOrigin(playerIn);
		EntityHitResult target = getTraceTarget(playerIn, range, origin);
		BlockView context = new BlockView(origin, target, a.a, b.a, playerIn);
		return worldIn.a(context);
	}

	public static PredicateTraceResult rayTraceUntil(PlayerAbilities playerIn, double range,
			Predicate<BlockPos> predicate) {
		EntityHitResult origin = getTraceOrigin(playerIn);
		EntityHitResult target = getTraceTarget(playerIn, range, origin);
		return rayTraceUntil(origin, target, predicate);
	}

	public static EntityHitResult getTraceTarget(PlayerAbilities playerIn, double range, EntityHitResult origin) {
		float f = playerIn.q;
		float f1 = playerIn.p;
		float f2 = afj.b(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = afj.a(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -afj.b(-f * 0.017453292F);
		float f5 = afj.a(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = range;
		EntityHitResult Vector3d1 = origin.b((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
		return Vector3d1;
	}

	public static EntityHitResult getTraceOrigin(PlayerAbilities playerIn) {
		double d0 = playerIn.cC();
		double d1 = playerIn.cD() + (double) playerIn.cd();
		double d2 = playerIn.cG();
		EntityHitResult Vector3d = new EntityHitResult(d0, d1, d2);
		return Vector3d;
	}

	public static PredicateTraceResult rayTraceUntil(EntityHitResult start, EntityHitResult end, Predicate<BlockPos> predicate) {
		if (Double.isNaN(start.entity) || Double.isNaN(start.c) || Double.isNaN(start.d))
			return null;
		if (Double.isNaN(end.entity) || Double.isNaN(end.c) || Double.isNaN(end.d))
			return null;

		int dx = afj.c(end.entity);
		int dy = afj.c(end.c);
		int dz = afj.c(end.d);
		int x = afj.c(start.entity);
		int y = afj.c(start.c);
		int z = afj.c(start.d);

		BlockPos currentPos = new BlockPos(x, y, z);

		if (predicate.test(currentPos))
			return new PredicateTraceResult(currentPos, Direction.getFacing(dx - x, dy - y, dz - z));

		int remainingDistance = 200;

		while (remainingDistance-- >= 0) {
			if (Double.isNaN(start.entity) || Double.isNaN(start.c) || Double.isNaN(start.d)) {
				return null;
			}

			if (x == dx && y == dy && z == dz) {
				return new PredicateTraceResult();
			}

			boolean flag2 = true;
			boolean flag = true;
			boolean flag1 = true;
			double d0 = 999.0D;
			double d1 = 999.0D;
			double d2 = 999.0D;

			if (dx > x) {
				d0 = (double) x + 1.0D;
			} else if (dx < x) {
				d0 = (double) x + 0.0D;
			} else {
				flag2 = false;
			}

			if (dy > y) {
				d1 = (double) y + 1.0D;
			} else if (dy < y) {
				d1 = (double) y + 0.0D;
			} else {
				flag = false;
			}

			if (dz > z) {
				d2 = (double) z + 1.0D;
			} else if (dz < z) {
				d2 = (double) z + 0.0D;
			} else {
				flag1 = false;
			}

			double d3 = 999.0D;
			double d4 = 999.0D;
			double d5 = 999.0D;
			double d6 = end.entity - start.entity;
			double d7 = end.c - start.c;
			double d8 = end.d - start.d;

			if (flag2) {
				d3 = (d0 - start.entity) / d6;
			}

			if (flag) {
				d4 = (d1 - start.c) / d7;
			}

			if (flag1) {
				d5 = (d2 - start.d) / d8;
			}

			if (d3 == -0.0D) {
				d3 = -1.0E-4D;
			}

			if (d4 == -0.0D) {
				d4 = -1.0E-4D;
			}

			if (d5 == -0.0D) {
				d5 = -1.0E-4D;
			}

			Direction enumfacing;

			if (d3 < d4 && d3 < d5) {
				enumfacing = dx > x ? Direction.WEST : Direction.EAST;
				start = new EntityHitResult(d0, start.c + d7 * d3, start.d + d8 * d3);
			} else if (d4 < d5) {
				enumfacing = dy > y ? Direction.DOWN : Direction.UP;
				start = new EntityHitResult(start.entity + d6 * d4, d1, start.d + d8 * d4);
			} else {
				enumfacing = dz > z ? Direction.NORTH : Direction.SOUTH;
				start = new EntityHitResult(start.entity + d6 * d5, start.c + d7 * d5, d2);
			}

			x = afj.c(start.entity) - (enumfacing == Direction.EAST ? 1 : 0);
			y = afj.c(start.c) - (enumfacing == Direction.UP ? 1 : 0);
			z = afj.c(start.d) - (enumfacing == Direction.SOUTH ? 1 : 0);
			currentPos = new BlockPos(x, y, z);

			if (predicate.test(currentPos))
				return new PredicateTraceResult(currentPos, enumfacing);
		}

		return new PredicateTraceResult();
	}

	public static class PredicateTraceResult {
		private BlockPos pos;
		private Direction facing;

		public PredicateTraceResult(BlockPos pos, Direction facing) {
			this.pos = pos;
			this.facing = facing;
		}

		public PredicateTraceResult() {
			// missed, no result
		}

		public Direction getFacing() {
			return facing;
		}

		public BlockPos getPos() {
			return pos;
		}

		public boolean missed() {
			return this.pos == null;
		}
	}

}
