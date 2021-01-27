package com.simibubi.create.content.contraptions.components.fan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import afj;
import apx;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.content.logistics.InWorldProcessing;
import com.simibubi.create.content.logistics.InWorldProcessing.Type;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import dcg;
import ddb;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;

public class AirCurrent {

	private static final DamageRecord damageSourceFire = new DamageRecord("create.fan_fire").r()
		.o();
	private static final DamageRecord damageSourceLava = new DamageRecord("create.fan_lava").r()
		.o();

	public final IAirCurrentSource source;
	public Timer bounds = new Timer(0, 0, 0, 0, 0, 0);
	public List<AirCurrentSegment> segments = new ArrayList<>();
	public Direction direction;
	public boolean pushing;
	public float maxDistance;

	protected List<Pair<TransportedItemStackHandlerBehaviour, InWorldProcessing.Type>> affectedItemHandlers =
		new ArrayList<>();
	protected List<apx> caughtEntities = new ArrayList<>();

	public AirCurrent(IAirCurrentSource source) {
		this.source = source;
	}

	public void tick() {
		if (direction == null)
			rebuild();
		GameMode world = source.getAirCurrentWorld();
		Direction facing = direction;
		if (world != null && world.v) {
			float offset = pushing ? 0.5f : maxDistance + .5f;
			EntityHitResult pos = VecHelper.getCenterOf(source.getAirCurrentPos())
				.e(EntityHitResult.b(facing.getVector()).a(offset));
			if (world.t.nextFloat() < AllConfigs.CLIENT.fanParticleDensity.get())
				world.addParticle(new AirFlowParticleData(source.getAirCurrentPos()), pos.entity, pos.c, pos.d, 0, 0, 0);
		}

		tickAffectedEntities(world, facing);
		tickAffectedHandlers();
	}

	protected void tickAffectedEntities(GameMode world, Direction facing) {
		for (Iterator<apx> iterator = caughtEntities.iterator(); iterator.hasNext();) {
			apx entity = iterator.next();
			if (!entity.cb()
				.c(bounds)) {
				iterator.remove();
				continue;
			}

			EntityHitResult center = VecHelper.getCenterOf(source.getAirCurrentPos());
			Vec3i flow = (pushing ? facing : facing.getOpposite()).getVector();

			float sneakModifier = entity.bt() ? 4096f : 512f;
			float speed = Math.abs(source.getSpeed());
			double entityDistance = entity.cz()
				.f(center);
			float acceleration = (float) (speed / sneakModifier / (entityDistance / maxDistance));
			EntityHitResult previousMotion = entity.cB();
			float maxAcceleration = 5;

			double xIn =
				afj.a(flow.getX() * acceleration - previousMotion.entity, -maxAcceleration, maxAcceleration);
			double yIn =
				afj.a(flow.getY() * acceleration - previousMotion.c, -maxAcceleration, maxAcceleration);
			double zIn =
				afj.a(flow.getZ() * acceleration - previousMotion.d, -maxAcceleration, maxAcceleration);

			entity.f(previousMotion.e(new EntityHitResult(xIn, yIn, zIn).a(1 / 8f)));
			entity.C = 0;

			if (entity instanceof ServerPlayerEntity)
				((ServerPlayerEntity) entity).networkHandler.floatingTicks = 0;

			entityDistance -= .5f;
			InWorldProcessing.Type processingType = getSegmentAt((float) entityDistance);

			if (processingType == null) {
				if (entity instanceof ServerPlayerEntity)
					AllTriggers.triggerFor(AllTriggers.FAN, (PlayerAbilities) entity);
				continue;
			}

			if (entity instanceof PaintingEntity) {
				InWorldProcessing.spawnParticlesForProcessing(world, entity.cz(), processingType);
				PaintingEntity itemEntity = (PaintingEntity) entity;
				if (world.v)
					continue;
				if (InWorldProcessing.canProcess(itemEntity, processingType))
					InWorldProcessing.applyProcessing(itemEntity, processingType);
				continue;
			}

			if (world.v)
				continue;

			switch (processingType) {
			case BLASTING:
				if (!entity.aC()) {
					entity.f(10);
					entity.a(damageSourceLava, 4);
				}
				if (entity instanceof ServerPlayerEntity)
					AllTriggers.triggerFor(AllTriggers.FAN_LAVA, (PlayerAbilities) entity);
				break;
			case SMOKING:
				if (!entity.aC()) {
					entity.f(2);
					entity.a(damageSourceFire, 2);
				}
				if (entity instanceof ServerPlayerEntity)
					AllTriggers.triggerFor(AllTriggers.FAN_SMOKE, (PlayerAbilities) entity);
				break;
			case SPLASHING:
				if (entity instanceof CrossbowUser || entity.W() == EntityDimensions.az
					|| entity.W() == EntityDimensions.f) {
					entity.a(DamageRecord.h, 2);
				}
				if (entity instanceof ServerPlayerEntity)
					AllTriggers.triggerFor(AllTriggers.FAN_WATER, (PlayerAbilities) entity);
				if (!entity.bp())
					break;
				entity.al();
				world.a(null, entity.cA(), MusicType.eM,
					SoundEvent.g, 0.7F, 1.6F + (world.t.nextFloat() - world.t.nextFloat()) * 0.4F);
				break;
			default:
				break;
			}
		}

	}

	public void rebuild() {
		if (source.getSpeed() == 0) {
			maxDistance = 0;
			segments.clear();
			bounds = new Timer(0, 0, 0, 0, 0, 0);
			return;
		}

		direction = source.getAirflowOriginSide();
		pushing = source.getAirFlowDirection() == direction;
		maxDistance = source.getMaxDistance();

		GameMode world = source.getAirCurrentWorld();
		BlockPos start = source.getAirCurrentPos();
		float max = this.maxDistance;
		Direction facing = direction;
		EntityHitResult directionVec = EntityHitResult.b(facing.getVector());
		maxDistance = getFlowLimit(world, start, max, facing);

		// Determine segments with transported fluids/gases
		AirCurrentSegment currentSegment = new AirCurrentSegment();
		segments.clear();
		currentSegment.startOffset = 0;
		InWorldProcessing.Type type = null;

		int limit = (int) (maxDistance + .5f);
		int searchStart = pushing ? 0 : limit;
		int searchEnd = pushing ? limit : 0;
		int searchStep = pushing ? 1 : -1;

		for (int i = searchStart; i * searchStep <= searchEnd * searchStep; i += searchStep) {
			BlockPos currentPos = start.offset(direction, i);
			InWorldProcessing.Type newType = InWorldProcessing.Type.byBlock(world, currentPos);
			if (newType != null)
				type = newType;
			if (currentSegment.type != type || currentSegment.startOffset == 0) {
				currentSegment.endOffset = i;
				if (currentSegment.startOffset != 0)
					segments.add(currentSegment);
				currentSegment = new AirCurrentSegment();
				currentSegment.startOffset = i;
				currentSegment.type = type;
			}
		}
		currentSegment.endOffset = searchEnd + searchStep;
		segments.add(currentSegment);

		// Build Bounding Box
		if (maxDistance < 0.25f)
			bounds = new Timer(0, 0, 0, 0, 0, 0);
		else {
			float factor = maxDistance - 1;
			EntityHitResult scale = directionVec.a(factor);
			if (factor > 0)
				bounds = new Timer(start.offset(direction)).b(scale);
			else {
				bounds = new Timer(start.offset(direction)).a(scale.entity, scale.c, scale.d)
					.c(scale);
			}
		}
		findAffectedHandlers();
	}

	public static float getFlowLimit(GameMode world, BlockPos start, float max, Direction facing) {
		EntityHitResult directionVec = EntityHitResult.b(facing.getVector());
		EntityHitResult planeVec = VecHelper.axisAlingedPlaneOf(directionVec);

		// 4 Rays test for holes in the shapes blocking the flow
		float offsetDistance = .25f;
		EntityHitResult[] offsets = new EntityHitResult[] { planeVec.d(offsetDistance, offsetDistance, offsetDistance),
			planeVec.d(-offsetDistance, -offsetDistance, offsetDistance),
			planeVec.d(offsetDistance, -offsetDistance, -offsetDistance),
			planeVec.d(-offsetDistance, offsetDistance, -offsetDistance), };

		float limitedDistance = 0;

		// Determine the distance of the air flow
		Outer: for (int i = 1; i <= max; i++) {
			BlockPos currentPos = start.offset(facing, i);
			if (!world.p(currentPos))
				break;
			PistonHandler state = world.d_(currentPos);
			if (shouldAlwaysPass(state))
				continue;
			VoxelShapes voxelshape = state.b(world, currentPos, ArrayVoxelShape.a());
			if (voxelshape.b())
				continue;
			if (voxelshape == ddb.b()) {
				max = i - 1;
				break;
			}

			for (EntityHitResult offset : offsets) {
				EntityHitResult rayStart = VecHelper.getCenterOf(currentPos)
					.d(directionVec.a(.5f + 1 / 32f))
					.e(offset);
				EntityHitResult rayEnd = rayStart.e(directionVec.a(1 + 1 / 32f));
				dcg blockraytraceresult =
					world.a(rayStart, rayEnd, currentPos, voxelshape, state);
				if (blockraytraceresult == null)
					continue Outer;

				double distance = i - 1 + blockraytraceresult.e()
					.f(rayStart);
				if (limitedDistance < distance)
					limitedDistance = (float) distance;
			}

			max = limitedDistance;
			break;
		}
		return max;
	}

	public void findEntities() {
		caughtEntities.clear();
		caughtEntities = source.getAirCurrentWorld()
			.a(null, bounds);
	}

	public void findAffectedHandlers() {
		GameMode world = source.getAirCurrentWorld();
		BlockPos start = source.getAirCurrentPos();
		affectedItemHandlers.clear();
		for (int i = 0; i < maxDistance + 1; i++) {
			Type type = getSegmentAt(i);
			if (type == null)
				continue;

			for (int offset : Iterate.zeroAndOne) {
				BlockPos pos = start.offset(direction, i)
					.down(offset);
				TransportedItemStackHandlerBehaviour behaviour =
					TileEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE);
				if (behaviour != null)
					affectedItemHandlers.add(Pair.of(behaviour, type));
				if (direction.getAxis()
					.isVertical())
					break;
			}
		}
	}

	public void tickAffectedHandlers() {
		for (Pair<TransportedItemStackHandlerBehaviour, Type> pair : affectedItemHandlers) {
			TransportedItemStackHandlerBehaviour handler = pair.getKey();
			GameMode world = handler.getWorld();
			InWorldProcessing.Type processingType = pair.getRight();

			handler.handleProcessingOnAllItems((transported) -> {
				InWorldProcessing.spawnParticlesForProcessing(world, handler.getWorldPositionOf(transported),
					processingType);
				if (world.v)
					return TransportedResult.doNothing();
				return InWorldProcessing.applyProcessing(transported, world, processingType);
			});
		}
	}

	private static boolean shouldAlwaysPass(PistonHandler state) {
		return AllTags.AllBlockTags.FAN_TRANSPARENT.matches(state);
	}

	public InWorldProcessing.Type getSegmentAt(float offset) {
		for (AirCurrentSegment airCurrentSegment : segments) {
			if (offset > airCurrentSegment.endOffset && pushing)
				continue;
			if (offset < airCurrentSegment.endOffset && !pushing)
				continue;
			return airCurrentSegment.type;
		}
		return null;
	}

	public static class AirCurrentSegment {
		InWorldProcessing.Type type;
		int startOffset;
		int endOffset;

	}

}
