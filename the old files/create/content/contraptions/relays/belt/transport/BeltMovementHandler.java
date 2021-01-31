package com.simibubi.kinetic_api.content.contraptions.relays.belt.transport;

import static net.minecraft.entity.SpawnGroup.SELF;
import static net.minecraft.util.math.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.math.Direction.AxisDirection.POSITIVE;

import apx;
import java.util.List;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltPart;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltTileEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;

public class BeltMovementHandler {

	public static class TransportedEntityInfo {
		int ticksSinceLastCollision;
		BlockPos lastCollidedPos;
		PistonHandler lastCollidedState;

		public TransportedEntityInfo(BlockPos collision, PistonHandler belt) {
			refresh(collision, belt);
		}

		public void refresh(BlockPos collision, PistonHandler belt) {
			ticksSinceLastCollision = 0;
			lastCollidedPos = new BlockPos(collision).toImmutable();
			lastCollidedState = belt;
		}

		public TransportedEntityInfo tick() {
			ticksSinceLastCollision++;
			return this;
		}

		public int getTicksSinceLastCollision() {
			return ticksSinceLastCollision;
		}
	}

	public static boolean canBeTransported(apx entity) {
		if (!entity.aW())
			return false;
		if (entity instanceof PlayerAbilities && ((PlayerAbilities) entity).bt())
			return false;
		return true;
	}

	public static void transportEntity(BeltTileEntity beltTe, apx entityIn, TransportedEntityInfo info) {
		BlockPos pos = info.lastCollidedPos;
		GameMode world = beltTe.v();
		BeehiveBlockEntity te = world.c(pos);
		BeehiveBlockEntity tileEntityBelowPassenger = world.c(entityIn.cA());
		PistonHandler blockState = info.lastCollidedState;
		Direction movementFacing =
			Direction.from(blockState.c(BambooLeaves.O)
				.getAxis(), beltTe.getSpeed() < 0 ? POSITIVE : NEGATIVE);

		boolean collidedWithBelt = te instanceof BeltTileEntity;
		boolean betweenBelts = tileEntityBelowPassenger instanceof BeltTileEntity && tileEntityBelowPassenger != te;

		// Don't fight other Belts
		if (!collidedWithBelt || betweenBelts) {
			return;
		}

		// Too slow
		boolean notHorizontal = beltTe.p()
			.c(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL;
		if (Math.abs(beltTe.getSpeed()) < 1)
			return;

		// Not on top
		if (entityIn.cD() - .25f < pos.getY())
			return;

		// Lock entities in place
		boolean isPlayer = entityIn instanceof PlayerAbilities;
		if (entityIn instanceof SaddledComponent && !isPlayer) {
			((SaddledComponent) entityIn).c(new InstantStatusEffect(StatusEffectType.field_18272, 10, 1, false, false));
		}

		final Direction beltFacing = blockState.c(BambooLeaves.O);
		final BeltSlope slope = blockState.c(BeltBlock.SLOPE);
		final Axis axis = beltFacing.getAxis();
		float movementSpeed = beltTe.getBeltMovementSpeed();
		final Direction movementDirection = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);

		Vec3i centeringDirection = Direction.get(POSITIVE, beltFacing.rotateYClockwise()
			.getAxis())
			.getVector();
		EntityHitResult movement = EntityHitResult.b(movementDirection.getVector())
			.a(movementSpeed);

		double diffCenter =
			axis == Axis.Z ? (pos.getX() + .5f - entityIn.cC()) : (pos.getZ() + .5f - entityIn.cG());
		if (Math.abs(diffCenter) > 48 / 64f)
			return;

		BeltPart part = blockState.c(BeltBlock.PART);
		float top = 13 / 16f;
		boolean onSlope = notHorizontal && (part == BeltPart.MIDDLE || part == BeltPart.PULLEY
			|| part == (slope == BeltSlope.UPWARD ? BeltPart.END : BeltPart.START) && entityIn.cD() - pos.getY() < top
			|| part == (slope == BeltSlope.UPWARD ? BeltPart.START : BeltPart.END)
				&& entityIn.cD() - pos.getY() > top);

		boolean movingDown = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

		if (beltFacing.getAxis() == Axis.Z) {
			boolean b = movingDown;
			movingDown = movingUp;
			movingUp = b;
		}

		if (movingUp)
			movement = movement.b(0, Math.abs(axis.choose(movement.entity, movement.c, movement.d)), 0);
		if (movingDown)
			movement = movement.b(0, -Math.abs(axis.choose(movement.entity, movement.c, movement.d)), 0);

		EntityHitResult centering = EntityHitResult.b(centeringDirection)
			.a(diffCenter * Math.min(Math.abs(movementSpeed), .1f) * 4);
		movement = movement.e(centering);

		float step = entityIn.G;
		if (!isPlayer)
			entityIn.G = 1;

		// Entity Collisions
		if (Math.abs(movementSpeed) < .5f) {
			EntityHitResult checkDistance = movement.d()
				.a(0.5);
			Timer bb = entityIn.cb();
			Timer checkBB = new Timer(bb.LOGGER, bb.callback, bb.events, bb.eventCounter, bb.eventsByName, bb.f);
			checkBB = checkBB.c(checkDistance)
				.c(-Math.abs(checkDistance.entity), -Math.abs(checkDistance.c), -Math.abs(checkDistance.d));
			List<apx> list = world.a(entityIn, checkBB);
			list.removeIf(e -> shouldIgnoreBlocking(entityIn, e));
			if (!list.isEmpty()) {
				entityIn.n(0, 0, 0);
				info.ticksSinceLastCollision--;
				return;
			}
		}

		entityIn.C = 0;

		if (movingUp) {
			float minVelocity = .13f;
			float yMovement = (float) -(Math.max(Math.abs(movement.c), minVelocity));
			entityIn.a(SELF, new EntityHitResult(0, yMovement, 0));
			entityIn.a(SELF, movement.d(1, 0, 1));
		} else if (movingDown) {
			entityIn.a(SELF, movement.d(1, 0, 1));
			entityIn.a(SELF, movement.d(0, 1, 0));
		} else {
			entityIn.a(SELF, movement);
		}

		if (!isPlayer)
			entityIn.G = step;

		boolean movedPastEndingSlope = onSlope && (AllBlocks.BELT.has(world.d_(entityIn.cA()))
			|| AllBlocks.BELT.has(world.d_(entityIn.cA()
				.down())));

		if (movedPastEndingSlope && !movingDown && Math.abs(movementSpeed) > 0)
			entityIn.d(entityIn.cC(), entityIn.cD() + movement.c, entityIn.cG());
		if (movedPastEndingSlope) {
			entityIn.f(movement);
			entityIn.w = true;
		}
	}

	public static boolean shouldIgnoreBlocking(apx me, apx other) {
		if (other instanceof AbstractContraptionEntity)
			return true;
		if (other instanceof WitherEntity)
			return true;
		return isRidingOrBeingRiddenBy(me, other);
	}

	public static boolean isRidingOrBeingRiddenBy(apx me, apx other) {
		for (apx entity : me.cm()) {
			if (entity.equals(other))
				return true;
			if (isRidingOrBeingRiddenBy(entity, other))
				return true;
		}
		return false;
	}

}
