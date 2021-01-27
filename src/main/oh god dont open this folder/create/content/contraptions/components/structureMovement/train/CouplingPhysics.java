package com.simibubi.create.content.contraptions.components.structureMovement.train;

import afj;
import apx;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class CouplingPhysics {

	public static void tick(GameMode world) {
		CouplingHandler.forEachLoadedCoupling(world, c -> tickCoupling(world, c));
	}

	public static void tickCoupling(GameMode world, Couple<MinecartController> c) {
		Couple<ScheduleBuilder> carts = c.map(MinecartController::cart);
		float couplingLength = c.getFirst()
			.getCouplingLength(true);
		softCollisionStep(world, carts, couplingLength);
		if (world.v)
			return;
		hardCollisionStep(world, carts, couplingLength);
	}

	public static void hardCollisionStep(GameMode world, Couple<ScheduleBuilder> carts, double couplingLength) {
		if (!MinecartSim2020.canAddMotion(carts.get(false)) && MinecartSim2020.canAddMotion(carts.get(true)))
			carts = carts.swap();

		Couple<EntityHitResult> corrections = Couple.create(null, null);
		Couple<Float> maxSpeed = carts.map(ScheduleBuilder::getMaxCartSpeedOnRail);
		boolean firstLoop = true;
		for (boolean current : new boolean[] { true, false, true }) {
			ScheduleBuilder cart = carts.get(current);
			ScheduleBuilder otherCart = carts.get(!current);

			float stress = (float) (couplingLength - cart.cz()
				.f(otherCart.cz()));

			if (Math.abs(stress) < 1 / 8f)
				continue;

			Instrument shape = null;
			BlockPos railPosition = cart.getCurrentRailPosition();
			PistonHandler railState = world.d_(railPosition.up());

			if (railState.b() instanceof BlockWithEntity) {
				BlockWithEntity block = (BlockWithEntity) railState.b();
				shape = block.getRailDirection(railState, world, railPosition, cart);
			}

			EntityHitResult correction = EntityHitResult.a;
			EntityHitResult pos = cart.cz();
			EntityHitResult link = otherCart.cz()
				.d(pos);
			float correctionMagnitude = firstLoop ? -stress / 2f : -stress;
			
			if (!MinecartSim2020.canAddMotion(cart))
				correctionMagnitude /= 2;
			
			correction = shape != null
				? followLinkOnRail(link, pos, correctionMagnitude, MinecartSim2020.getRailVec(shape)).d(pos)
				: link.d()
					.a(correctionMagnitude);

			float maxResolveSpeed = 1.75f;
			correction = VecHelper.clamp(correction, Math.min(maxResolveSpeed, maxSpeed.get(current)));

			if (corrections.get(current) == null)
				corrections.set(current, correction);

			if (shape != null)
				MinecartSim2020.moveCartAlongTrack(cart, correction, railPosition, railState);
			else {
				cart.a(SpawnGroup.SELF, correction);
				cart.f(cart.cB()
					.a(0.95f));
			}
			firstLoop = false;
		}
	}

	public static void softCollisionStep(GameMode world, Couple<ScheduleBuilder> carts, double couplingLength) {
		Couple<Float> maxSpeed = carts.map(ScheduleBuilder::getMaxCartSpeedOnRail);
		Couple<Boolean> canAddmotion = carts.map(MinecartSim2020::canAddMotion);
		Couple<EntityHitResult> motions = carts.map(apx::cB);
		Couple<EntityHitResult> nextPositions = carts.map(MinecartSim2020::predictNextPositionOf);

		Couple<Instrument> shapes = carts.mapWithContext((cart, current) -> {
			ScheduleBuilder minecart = cart.getMinecart();
			EntityHitResult vec = nextPositions.get(current);
			int x = afj.c(vec.getX());
	        int y = afj.c(vec.getY());
	        int z = afj.c(vec.getZ());
	        BlockPos pos = new BlockPos(x, y - 1, z);
	        if (minecart.l.d_(pos).a(StatHandler.H)) pos = pos.down();
			BlockPos railPosition = pos;
			PistonHandler railState = world.d_(railPosition.up());
			if (!(railState.b() instanceof BlockWithEntity))
				return null;
			BlockWithEntity block = (BlockWithEntity) railState.b();
			return block.getRailDirection(railState, world, railPosition, cart);
		});

		float futureStress = (float) (couplingLength - nextPositions.getFirst()
			.f(nextPositions.getSecond()));
		if (afj.b(futureStress, 0D))
			return;

		for (boolean current : Iterate.trueAndFalse) {
			EntityHitResult correction = EntityHitResult.a;
			EntityHitResult pos = nextPositions.get(current);
			EntityHitResult link = nextPositions.get(!current)
				.d(pos);
			float correctionMagnitude = -futureStress / 2f;

			if (canAddmotion.get(current) != canAddmotion.get(!current))
				correctionMagnitude = !canAddmotion.get(current) ? 0 : correctionMagnitude * 2;
			if (!canAddmotion.get(current))
				continue;

			Instrument shape = shapes.get(current);
			if (shape != null) {
				EntityHitResult railVec = MinecartSim2020.getRailVec(shape);
				correction = followLinkOnRail(link, pos, correctionMagnitude, railVec).d(pos);
			} else
				correction = link.d()
					.a(correctionMagnitude);

			correction = VecHelper.clamp(correction, maxSpeed.get(current));
			
			motions.set(current, motions.get(current)
				.e(correction));
		}

		motions.replaceWithParams(VecHelper::clamp, maxSpeed);
		carts.forEachWithParams(apx::f, motions);
	}

	public static EntityHitResult followLinkOnRail(EntityHitResult link, EntityHitResult cart, float diffToReduce, EntityHitResult railAxis) {
		double dotProduct = railAxis.b(link);
		if (Double.isNaN(dotProduct) || dotProduct == 0 || diffToReduce == 0)
			return cart;

		EntityHitResult axis = railAxis.a(-Math.signum(dotProduct));
		EntityHitResult center = cart.e(link);
		double radius = link.f() - diffToReduce;
		EntityHitResult intersectSphere = VecHelper.intersectSphere(cart, axis, center, radius);

		// Cannot satisfy on current rail vector
		if (intersectSphere == null)
			return cart.e(VecHelper.project(link, axis));

		return intersectSphere;
	}

}
