package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import static apx.a;
import static apx.c;

import afj;
import apx;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.base.Predicates;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllMovementBehaviours;
import com.simibubi.kinetic_api.content.contraptions.components.actors.BlockBreakingMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AbstractContraptionEntity.ContraptionRotationState;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.ContraptionCollider.PlayerType;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.sync.ClientMotionPacket;
import com.simibubi.kinetic_api.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;
import com.simibubi.kinetic_api.foundation.collision.Matrix3d;
import com.simibubi.kinetic_api.foundation.collision.OrientedBB;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import dco;
import ddb;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ContraptionCollider {

	enum PlayerType {
		NONE, CLIENT, REMOTE, SERVER
	}

	static void collideEntities(AbstractContraptionEntity contraptionEntity) {
		GameMode world = contraptionEntity.cf();
		Contraption contraption = contraptionEntity.getContraption();
		Timer bounds = contraptionEntity.cb();

		if (contraption == null)
			return;
		if (bounds == null)
			return;

		EntityHitResult contraptionPosition = contraptionEntity.cz();
		EntityHitResult contraptionMotion = contraptionPosition.d(contraptionEntity.getPrevPositionVec());
		EntityHitResult anchorVec = contraptionEntity.getAnchorVec();
		EntityHitResult centerOfBlock = VecHelper.CENTER_OF_ORIGIN;
		ContraptionRotationState rotation = null;

		// After death, multiple refs to the client player may show up in the area
		boolean skipClientPlayer = false;

		List<apx> entitiesWithinAABB = world.a(apx.class, bounds.g(2)
			.b(0, 32, 0), contraptionEntity::canCollideWith);
		for (apx entity : entitiesWithinAABB) {

			PlayerType playerType = getPlayerType(entity);
			if (playerType == PlayerType.REMOTE)
				continue;

			if (playerType == PlayerType.SERVER && entity instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) entity).networkHandler.floatingTicks = 0;
				continue;
			}

			if (playerType == PlayerType.CLIENT)
				if (skipClientPlayer)
					continue;
				else
					skipClientPlayer = true;

			// Init matrix
			if (rotation == null)
				rotation = contraptionEntity.getRotationState();
			Matrix3d rotationMatrix = rotation.asMatrix();

			// Transform entity position and motion to local space
			EntityHitResult entityPosition = entity.cz();
			Timer entityBounds = entity.cb();
			EntityHitResult centerY = new EntityHitResult(0, entityBounds.c() / 2, 0);
			EntityHitResult motion = entity.cB();
			float yawOffset = rotation.getYawOffset();

			EntityHitResult position = entityPosition;
			position = position.e(centerY);
			position = position.d(centerOfBlock);
			position = position.d(anchorVec);
			position = VecHelper.rotate(position, -yawOffset, Axis.Y);
			position = rotationMatrix.transform(position);
			position = position.e(centerOfBlock);
			position = position.d(centerY);
			position = position.d(entityPosition);

			// Find all potential block shapes to collide with
			Timer localBB = entityBounds.c(position)
				.g(1.0E-7D);
			LowercaseEnumTypeAdapterFactory<VoxelShapes> potentialHits =
				getPotentiallyCollidedShapes(world, contraption, localBB.b(motion));
			if (potentialHits.a()
				.count() == 0)
				continue;

			// Prepare entity bounds
			OrientedBB obb = new OrientedBB(localBB);
			obb.setRotation(rotationMatrix);
			motion = motion.d(contraptionMotion);
			motion = rotationMatrix.transform(motion);

			MutableObject<EntityHitResult> collisionResponse = new MutableObject<>(EntityHitResult.a);
			MutableBoolean surfaceCollision = new MutableBoolean(false);
			MutableFloat temporalResponse = new MutableFloat(1);
			EntityHitResult obbCenter = obb.getCenter();

			// Apply separation maths
			List<Timer> bbs = new ArrayList<>();
			potentialHits.a()
				.forEach(shape -> shape.d()
					.forEach(bbs::add));

			boolean doHorizontalPass = !rotation.hasVerticalRotation();
			for (boolean horizontalPass : Iterate.trueAndFalse) {
				boolean verticalPass = !horizontalPass || !doHorizontalPass;

				for (Timer bb : bbs) {
					EntityHitResult currentResponse = collisionResponse.getValue();
					obb.setCenter(obbCenter.e(currentResponse));
					ContinuousSeparationManifold intersect = obb.intersect(bb, motion);

					if (intersect == null)
						continue;
					if (verticalPass && surfaceCollision.isFalse())
						surfaceCollision.setValue(intersect.isSurfaceCollision());

					double timeOfImpact = intersect.getTimeOfImpact();
					if (timeOfImpact > 0 && timeOfImpact < 1) {
						if (temporalResponse.getValue() > timeOfImpact)
							temporalResponse.setValue(timeOfImpact);
						continue;
					}

					EntityHitResult separation = intersect.asSeparationVec(entity.G);
					if (separation != null && !separation.equals(EntityHitResult.a))
						collisionResponse.setValue(currentResponse.e(separation));
				}

				if (verticalPass)
					break;

				boolean noVerticalMotionResponse = temporalResponse.getValue() == 1;
				boolean noVerticalCollision = collisionResponse.getValue().c == 0;
				if (noVerticalCollision && noVerticalMotionResponse)
					break;

				// Re-run collisions with horizontal offset
				collisionResponse.setValue(collisionResponse.getValue()
					.d(129 / 128f, 0, 129 / 128f));
				continue;
			}

			// Resolve collision
			EntityHitResult entityMotion = entity.cB();
			EntityHitResult totalResponse = collisionResponse.getValue();
			boolean hardCollision = !totalResponse.equals(EntityHitResult.a);
			boolean temporalCollision = temporalResponse.getValue() != 1;
			EntityHitResult motionResponse = !temporalCollision ? motion
				: motion.d()
					.a(motion.f() * temporalResponse.getValue());

			rotationMatrix.transpose();
			motionResponse = rotationMatrix.transform(motionResponse)
				.e(contraptionMotion);
			totalResponse = rotationMatrix.transform(totalResponse);
			totalResponse = VecHelper.rotate(totalResponse, yawOffset, Axis.Y);
			rotationMatrix.transpose();

			if (temporalCollision) {
				double idealVerticalMotion = motionResponse.c;
				if (idealVerticalMotion != entityMotion.c) {
					entity.f(entityMotion.d(1, 0, 1)
						.b(0, idealVerticalMotion, 0));
					entityMotion = entity.cB();
				}
			}

			if (hardCollision) {
				double motionX = entityMotion.getX();
				double motionY = entityMotion.getY();
				double motionZ = entityMotion.getZ();
				double intersectX = totalResponse.getX();
				double intersectY = totalResponse.getY();
				double intersectZ = totalResponse.getZ();

				double horizonalEpsilon = 1 / 128f;
				if (motionX != 0 && Math.abs(intersectX) > horizonalEpsilon && motionX > 0 == intersectX < 0)
					entityMotion = entityMotion.d(0, 1, 1);
				if (motionY != 0 && intersectY != 0 && motionY > 0 == intersectY < 0)
					entityMotion = entityMotion.d(1, 0, 1)
						.b(0, contraptionMotion.c, 0);
				if (motionZ != 0 && Math.abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
					entityMotion = entityMotion.d(1, 1, 0);
			}

			if (!hardCollision && surfaceCollision.isFalse())
				continue;

			EntityHitResult allowedMovement = getAllowedMovement(totalResponse, entity);
			entity.d(entityPosition.entity + allowedMovement.entity, entityPosition.c + allowedMovement.c,
				entityPosition.d + allowedMovement.d);
			entityPosition = entity.cz();

			entity.w = true;
			EntityHitResult contactPointMotion = EntityHitResult.a;

			if (surfaceCollision.isTrue()) {
				entity.C = 0;
				entity.c(true);
				contraptionEntity.collidingEntities.put(entity, new MutableInt(0));
				if (entity instanceof PaintingEntity)
					entityMotion = entityMotion.d(.5f, 1, .5f);
				contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
				allowedMovement = getAllowedMovement(contactPointMotion, entity);
				entity.d(entityPosition.entity + allowedMovement.entity, entityPosition.c,
					entityPosition.d + allowedMovement.d);
			}

			entity.f(entityMotion);

			if (playerType != PlayerType.CLIENT)
				continue;

			double d0 = entity.cC() - entity.m - contactPointMotion.entity;
			double d1 = entity.cG() - entity.o - contactPointMotion.d;
			float limbSwing = afj.a(d0 * d0 + d1 * d1) * 4.0F;
			if (limbSwing > 1.0F)
				limbSwing = 1.0F;
			AllPackets.channel.sendToServer(new ClientMotionPacket(entityMotion, true, limbSwing));
		}

	}

	/** From Entity#getAllowedMovement **/
	static EntityHitResult getAllowedMovement(EntityHitResult movement, apx e) {
		Timer bb = e.cb();
		ArrayVoxelShape ctx = ArrayVoxelShape.a(e);
		GameMode world = e.l;
		VoxelShapes voxelshape = world.f()
			.c();
		Stream<VoxelShapes> stream =
			ddb.c(voxelshape, ddb.a(bb.h(1.0E-7D)), dco.i)
				? Stream.empty()
				: Stream.of(voxelshape);
		Stream<VoxelShapes> stream1 = world.c(e, bb.b(movement), entity -> false); // FIXME: 1.15 equivalent translated correctly?
		LowercaseEnumTypeAdapterFactory<VoxelShapes> reuseablestream = new LowercaseEnumTypeAdapterFactory<>(Stream.concat(stream1, stream));
		EntityHitResult Vector3d = movement.g() == 0.0D ? movement
			: a(e, movement, bb, world, ctx, reuseablestream);
		boolean flag = movement.entity != Vector3d.entity;
		boolean flag1 = movement.c != Vector3d.c;
		boolean flag2 = movement.d != Vector3d.d;
		boolean flag3 = e.an() || flag1 && movement.c < 0.0D;
		if (e.G > 0.0F && flag3 && (flag || flag2)) {
			EntityHitResult Vector3d1 = a(e, new EntityHitResult(movement.entity, (double) e.G, movement.d),
				bb, world, ctx, reuseablestream);
			EntityHitResult Vector3d2 = a(e, new EntityHitResult(0.0D, (double) e.G, 0.0D),
				bb.b(movement.entity, 0.0D, movement.d), world, ctx, reuseablestream);
			if (Vector3d2.c < (double) e.G) {
				EntityHitResult Vector3d3 = a(e, new EntityHitResult(movement.entity, 0.0D, movement.d),
					bb.c(Vector3d2), world, ctx, reuseablestream).e(Vector3d2);
				if (c(Vector3d3) > c(Vector3d1)) {
					Vector3d1 = Vector3d3;
				}
			}

			if (c(Vector3d1) > c(Vector3d)) {
				return Vector3d1.e(a(e, new EntityHitResult(0.0D, -Vector3d1.c + movement.c, 0.0D),
					bb.c(Vector3d1), world, ctx, reuseablestream));
			}
		}

		return Vector3d;
	}

	private static PlayerType getPlayerType(apx entity) {
		if (!(entity instanceof PlayerAbilities))
			return PlayerType.NONE;
		if (!entity.l.v)
			return PlayerType.SERVER;
		MutableBoolean isClient = new MutableBoolean(false);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> isClient.setValue(isClientPlayerEntity(entity)));
		return isClient.booleanValue() ? PlayerType.CLIENT : PlayerType.REMOTE;
	}

	@Environment(EnvType.CLIENT)
	private static boolean isClientPlayerEntity(apx entity) {
		return entity instanceof FishingParticle;
	}

	private static LowercaseEnumTypeAdapterFactory<VoxelShapes> getPotentiallyCollidedShapes(GameMode world, Contraption contraption,
		Timer localBB) {

		double height = localBB.c();
		double width = localBB.b();
		double horizontalFactor = (height > width && width != 0) ? height / width : 1;
		double verticalFactor = (width > height && height != 0) ? width / height : 1;
		Timer blockScanBB = localBB.g(0.5f);
		blockScanBB = blockScanBB.c(horizontalFactor, verticalFactor, horizontalFactor);

		BlockPos min = new BlockPos(blockScanBB.LOGGER, blockScanBB.callback, blockScanBB.events);
		BlockPos max = new BlockPos(blockScanBB.eventCounter, blockScanBB.eventsByName, blockScanBB.f);

		LowercaseEnumTypeAdapterFactory<VoxelShapes> potentialHits = new LowercaseEnumTypeAdapterFactory<>(BlockPos.stream(min, max)
			.filter(contraption.getBlocks()::containsKey)
			.map(p -> {
				PistonHandler blockState = contraption.getBlocks()
					.get(p).b;
				BlockPos pos = contraption.getBlocks()
					.get(p).a;
				VoxelShapes collisionShape = blockState.k(world, p);
				return collisionShape.a(pos.getX(), pos.getY(), pos.getZ());
			})
			.filter(Predicates.not(VoxelShapes::b)));

		return potentialHits;
	}

	public static boolean collideBlocks(ControlledContraptionEntity contraptionEntity) {
		if (!contraptionEntity.supportsTerrainCollision())
			return false;

		GameMode world = contraptionEntity.cf();
		EntityHitResult motion = contraptionEntity.cB();
		TranslatingContraption contraption = (TranslatingContraption) contraptionEntity.getContraption();
		Timer bounds = contraptionEntity.cb();
		EntityHitResult position = contraptionEntity.cz();
		BlockPos gridPos = new BlockPos(position);

		if (contraption == null)
			return false;
		if (bounds == null)
			return false;
		if (motion.equals(EntityHitResult.a))
			return false;

		Direction movementDirection = Direction.getFacing(motion.entity, motion.c, motion.d);

		// Blocks in the world
		if (movementDirection.getDirection() == AxisDirection.POSITIVE)
			gridPos = gridPos.offset(movementDirection);
		if (isCollidingWithWorld(world, contraption, gridPos, movementDirection))
			return true;

		// Other moving Contraptions
		for (ControlledContraptionEntity otherContraptionEntity : world.a(
			ControlledContraptionEntity.class, bounds.g(1), e -> !e.equals(contraptionEntity))) {

			if (!otherContraptionEntity.supportsTerrainCollision())
				continue;

			EntityHitResult otherMotion = otherContraptionEntity.cB();
			TranslatingContraption otherContraption = (TranslatingContraption) otherContraptionEntity.getContraption();
			Timer otherBounds = otherContraptionEntity.cb();
			EntityHitResult otherPosition = otherContraptionEntity.cz();

			if (otherContraption == null)
				return false;
			if (otherBounds == null)
				return false;

			if (!bounds.c(motion)
				.c(otherBounds.c(otherMotion)))
				continue;

			for (BlockPos colliderPos : contraption.getColliders(world, movementDirection)) {
				colliderPos = colliderPos.add(gridPos)
					.subtract(new BlockPos(otherPosition));
				if (!otherContraption.getBlocks()
					.containsKey(colliderPos))
					continue;
				return true;
			}
		}

		return false;
	}

	public static boolean isCollidingWithWorld(GameMode world, TranslatingContraption contraption, BlockPos anchor,
		Direction movementDirection) {
		for (BlockPos pos : contraption.getColliders(world, movementDirection)) {
			BlockPos colliderPos = pos.add(anchor);

			if (!world.p(colliderPos))
				return true;

			PistonHandler collidedState = world.d_(colliderPos);
			c blockInfo = contraption.getBlocks()
				.get(pos);

			if (AllMovementBehaviours.contains(blockInfo.b.b())) {
				MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.b.b());
				if (movementBehaviour instanceof BlockBreakingMovementBehaviour) {
					BlockBreakingMovementBehaviour behaviour = (BlockBreakingMovementBehaviour) movementBehaviour;
					if (!behaviour.canBreak(world, colliderPos, collidedState)
						&& !collidedState.k(world, pos)
							.b()) {
						return true;
					}
					continue;
				}
			}

			if (AllBlocks.PULLEY_MAGNET.has(collidedState) && pos.equals(BlockPos.ORIGIN)
				&& movementDirection == Direction.UP)
				continue;
			if (collidedState.b() instanceof ChestBlock)
				continue;
			if (!collidedState.c()
				.e()
				&& !collidedState.k(world, colliderPos)
					.b()) {
				return true;
			}

		}
		return false;
	}

}
