package com.simibubi.create.content.contraptions.components.structureMovement.train;

import static apx.c;

import afj;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Useful methods for dealing with Minecarts
 *
 */
public class MinecartSim2020 {
	private static final Map<Instrument, Pair<Vec3i, Vec3i>> MATRIX =
		Util.make(Maps.newEnumMap(Instrument.class), (map) -> {
			Vec3i west = Direction.WEST.getVector();
			Vec3i east = Direction.EAST.getVector();
			Vec3i north = Direction.NORTH.getVector();
			Vec3i south = Direction.SOUTH.getVector();
			map.put(Instrument.NORTH_SOUTH, Pair.of(north, south));
			map.put(Instrument.EAST_WEST, Pair.of(west, east));
			map.put(Instrument.ASCENDING_EAST, Pair.of(west.down(), east));
			map.put(Instrument.ASCENDING_WEST, Pair.of(west, east.down()));
			map.put(Instrument.ASCENDING_NORTH, Pair.of(north, south.down()));
			map.put(Instrument.ASCENDING_SOUTH, Pair.of(north.down(), south));
			map.put(Instrument.SOUTH_EAST, Pair.of(south, east));
			map.put(Instrument.SOUTH_WEST, Pair.of(south, west));
			map.put(Instrument.NORTH_WEST, Pair.of(north, west));
			map.put(Instrument.NORTH_EAST, Pair.of(north, east));
		});
	
	public static EntityHitResult predictNextPositionOf(ScheduleBuilder cart) {
		EntityHitResult position = cart.cz();
		EntityHitResult motion = cart.cB();
		return position.e(motion);
	}

	public static boolean canAddMotion(ScheduleBuilder c) {
		if (c instanceof MinecartEntity)
			return afj.b(((MinecartEntity) c).b, 0)
				&& afj.b(((MinecartEntity) c).c, 0);
		LazyOptional<MinecartController> capability =
			c.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (capability.isPresent() && capability.orElse(null)
			.isStalled())
			return false;
		return true;
	}

	public static void moveCartAlongTrack(ScheduleBuilder cart, EntityHitResult forcedMovement, BlockPos cartPos,
		PistonHandler trackState) {

		if (forcedMovement.equals(EntityHitResult.a))
			return;

		EntityHitResult previousMotion = cart.cB();
		cart.C = 0.0F;

		double x = cart.cC();
		double y = cart.cD();
		double z = cart.cG();

		double actualX = x;
		double actualY = y;
		double actualZ = z;

		EntityHitResult actualVec = cart.p(actualX, actualY, actualZ);
		actualY = cartPos.getY() + 1;

		BlockWithEntity abstractrailblock = (BlockWithEntity) trackState.b();
		Instrument railshape = abstractrailblock.getRailDirection(trackState, cart.l, cartPos, cart);
		switch (railshape) {
		case ASCENDING_EAST:
			forcedMovement = forcedMovement.b(-1 * cart.getSlopeAdjustment(), 0.0D, 0.0D);
			actualY++;
			break;
		case ASCENDING_WEST:
			forcedMovement = forcedMovement.b(cart.getSlopeAdjustment(), 0.0D, 0.0D);
			actualY++;
			break;
		case ASCENDING_NORTH:
			forcedMovement = forcedMovement.b(0.0D, 0.0D, cart.getSlopeAdjustment());
			actualY++;
			break;
		case ASCENDING_SOUTH:
			forcedMovement = forcedMovement.b(0.0D, 0.0D, -1 * cart.getSlopeAdjustment());
			actualY++;
		default:
			break;
		}

		Pair<Vec3i, Vec3i> pair = MATRIX.get(railshape);
		Vec3i vec3i = pair.getFirst();
		Vec3i vec3i1 = pair.getSecond();
		double d4 = (double) (vec3i1.getX() - vec3i.getX());
		double d5 = (double) (vec3i1.getZ() - vec3i.getZ());
//		double d6 = Math.sqrt(d4 * d4 + d5 * d5);
		double d7 = forcedMovement.entity * d4 + forcedMovement.d * d5;
		if (d7 < 0.0D) {
			d4 = -d4;
			d5 = -d5;
		}

		double d23 = (double) cartPos.getX() + 0.5D + (double) vec3i.getX() * 0.5D;
		double d10 = (double) cartPos.getZ() + 0.5D + (double) vec3i.getZ() * 0.5D;
		double d12 = (double) cartPos.getX() + 0.5D + (double) vec3i1.getX() * 0.5D;
		double d13 = (double) cartPos.getZ() + 0.5D + (double) vec3i1.getZ() * 0.5D;
		d4 = d12 - d23;
		d5 = d13 - d10;
		double d14;
		if (d4 == 0.0D) {
			d14 = actualZ - (double) cartPos.getZ();
		} else if (d5 == 0.0D) {
			d14 = actualX - (double) cartPos.getX();
		} else {
			double d15 = actualX - d23;
			double d16 = actualZ - d10;
			d14 = (d15 * d4 + d16 * d5) * 2.0D;
		}

		actualX = d23 + d4 * d14;
		actualZ = d10 + d5 * d14;

		cart.d(actualX, actualY, actualZ);
		cart.f(forcedMovement);
		cart.moveMinecartOnRail(cartPos);

		x = cart.cC();
		y = cart.cD();
		z = cart.cG();

		if (vec3i.getY() != 0 && afj.c(x) - cartPos.getX() == vec3i.getX()
			&& afj.c(z) - cartPos.getZ() == vec3i.getZ()) {
			cart.d(x, y + (double) vec3i.getY(), z);
		} else if (vec3i1.getY() != 0 && afj.c(x) - cartPos.getX() == vec3i1.getX()
			&& afj.c(z) - cartPos.getZ() == vec3i1.getZ()) {
			cart.d(x, y + (double) vec3i1.getY(), z);
		}

		x = cart.cC();
		y = cart.cD();
		z = cart.cG();

		EntityHitResult Vector3d3 = cart.p(x, y, z);
		if (Vector3d3 != null && actualVec != null) {
			double d17 = (actualVec.c - Vector3d3.c) * 0.05D;
			EntityHitResult Vector3d4 = cart.cB();
			double d18 = Math.sqrt(c(Vector3d4));
			if (d18 > 0.0D) {
				cart.f(Vector3d4.d((d18 + d17) / d18, 1.0D, (d18 + d17) / d18));
			}

			cart.d(x, Vector3d3.c, z);
		}

		x = cart.cC();
		y = cart.cD();
		z = cart.cG();

		int j = afj.c(x);
		int i = afj.c(z);
		if (j != cartPos.getX() || i != cartPos.getZ()) {
			EntityHitResult Vector3d5 = cart.cB();
			double d26 = Math.sqrt(c(Vector3d5));
			cart.n(d26 * (double) (j - cartPos.getX()), Vector3d5.c, d26 * (double) (i - cartPos.getZ()));
		}

		cart.f(previousMotion);
	}

	public static EntityHitResult getRailVec(Instrument shape) {
		switch (shape) {
		case ASCENDING_NORTH:
		case ASCENDING_SOUTH:
		case NORTH_SOUTH:
			return new EntityHitResult(0, 0, 1);
		case ASCENDING_EAST:
		case ASCENDING_WEST:
		case EAST_WEST:
			return new EntityHitResult(1, 0, 0);
		case NORTH_EAST:
		case SOUTH_WEST:
			return new EntityHitResult(1, 0, 1).d();
		case NORTH_WEST:
		case SOUTH_EAST:
			return new EntityHitResult(1, 0, -1).d();
		default:
			return new EntityHitResult(0, 1, 0);
		}
	}

}
