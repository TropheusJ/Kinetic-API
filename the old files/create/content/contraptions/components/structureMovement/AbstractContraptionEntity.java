package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;
import apx;
import com.simibubi.kinetic_api.AllMovementBehaviours;
import com.simibubi.kinetic_api.content.contraptions.components.actors.SeatEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.kinetic_api.foundation.collision.Matrix3d;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public abstract class AbstractContraptionEntity extends apx implements IEntityAdditionalSpawnData {

	private static final TrackedData<Boolean> STALLED =
		DataTracker.registerData(AbstractContraptionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public final Map<apx, MutableInt> collidingEntities;

	protected Contraption contraption;
	protected boolean initialized;
	private boolean prevPosInvalid;

	public AbstractContraptionEntity(EntityDimensions<?> entityTypeIn, GameMode worldIn) {
		super(entityTypeIn, worldIn);
		prevPosInvalid = true;
		collidingEntities = new IdentityHashMap<>();
	}

	protected void setContraption(Contraption contraption) {
		this.contraption = contraption;
		if (contraption == null)
			return;
		if (l.v)
			return;
		contraption.onEntityCreated(this);
	}

	protected void contraptionInitialize() {
		contraption.onEntityInitialize(l, this);
		initialized = true;
	}

	public boolean collisionEnabled() {
		return true;
	}

	public void addSittingPassenger(apx passenger, int seatIndex) {
		passenger.a(this, true);
		if (l.v)
			return;
		contraption.getSeatMapping()
			.put(passenger.bR(), seatIndex);
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(X(), contraption.getSeatMapping()));
	}

	@Override
	protected void p(apx passenger) {
		EntityHitResult transformedVector = getPassengerPosition(passenger, 1);
		super.p(passenger);
		if (l.v)
			return;
		if (transformedVector != null)
			passenger.getPersistentData()
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.getSeatMapping()
			.remove(passenger.bR());
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(X(), contraption.getSeatMapping()));
	}

	@Override
	public void a(apx passenger, a callback) {
		if (!w(passenger))
			return;
		EntityHitResult transformedVector = getPassengerPosition(passenger, 1);
		if (transformedVector == null)
			return;
		callback.accept(passenger, transformedVector.entity, transformedVector.c, transformedVector.d);
	}

	protected EntityHitResult getPassengerPosition(apx passenger, float partialTicks) {
		UUID id = passenger.bR();
		if (passenger instanceof OrientedContraptionEntity) {
			BlockPos localPos = contraption.getBearingPosOf(id);
			if (localPos != null)
				return toGlobalVector(VecHelper.getCenterOf(localPos), partialTicks)
					.e(VecHelper.getCenterOf(BlockPos.ORIGIN))
					.a(.5f, 1, .5f);
		}

		Timer bb = passenger.cb();
		double ySize = bb.c();
		BlockPos seat = contraption.getSeatOf(id);
		if (seat == null)
			return null;
		EntityHitResult transformedVector =
			toGlobalVector(EntityHitResult.b(seat).b(.5, passenger.ba() + ySize - .15f, .5), partialTicks)
				.e(VecHelper.getCenterOf(BlockPos.ORIGIN))
				.a(0.5, ySize, 0.5);
		return transformedVector;
	}

	@Override
	protected boolean q(apx p_184219_1_) {
		if (p_184219_1_ instanceof OrientedContraptionEntity)
			return true;
		return contraption.getSeatMapping()
			.size() < contraption.getSeats()
				.size();
	}

	public boolean handlePlayerInteraction(PlayerAbilities player, BlockPos localPos, Direction side,
		ItemScatterer interactionHand) {
		int indexOfSeat = contraption.getSeats()
			.indexOf(localPos);
		if (indexOfSeat == -1)
			return false;

		// Eject potential existing passenger
		apx toDismount = null;
		for (Entry<UUID, Integer> entry : contraption.getSeatMapping()
			.entrySet()) {
			if (entry.getValue() != indexOfSeat)
				continue;
			for (apx entity : cm()) {
				if (!entry.getKey()
					.equals(entity.bR()))
					continue;
				if (entity instanceof PlayerAbilities)
					return false;
				toDismount = entity;
			}
		}

		if (toDismount != null && !l.v) {
			EntityHitResult transformedVector = getPassengerPosition(toDismount, 1);
			toDismount.l();
			if (transformedVector != null)
				toDismount.a(transformedVector.entity, transformedVector.c, transformedVector.d);
		}

		if (l.v)
			return true;
		addSittingPassenger(player, indexOfSeat);
		return true;
	}

	public EntityHitResult toGlobalVector(EntityHitResult localVec, float partialTicks) {
		EntityHitResult rotationOffset = VecHelper.getCenterOf(BlockPos.ORIGIN);
		localVec = localVec.d(rotationOffset);
		localVec = applyRotation(localVec, partialTicks);
		localVec = localVec.e(rotationOffset)
			.e(getAnchorVec());
		return localVec;
	}

	public EntityHitResult toLocalVector(EntityHitResult globalVec, float partialTicks) {
		EntityHitResult rotationOffset = VecHelper.getCenterOf(BlockPos.ORIGIN);
		globalVec = globalVec.d(getAnchorVec())
			.d(rotationOffset);
		globalVec = reverseRotation(globalVec, partialTicks);
		globalVec = globalVec.e(rotationOffset);
		return globalVec;
	}

	@Override
	public final void j() {
		if (contraption == null) {
			ac();
			return;
		}

		for (Iterator<Entry<apx, MutableInt>> iterator = collidingEntities.entrySet()
			.iterator(); iterator.hasNext();)
			if (iterator.next()
				.getValue()
				.incrementAndGet() > 3)
				iterator.remove();

		m = cC();
		n = cD();
		o = cG();
		prevPosInvalid = false;

		if (!initialized)
			contraptionInitialize();
		contraption.onEntityTick(l);
		tickContraption();
		super.j();
	}

	protected abstract void tickContraption();

	public abstract EntityHitResult applyRotation(EntityHitResult localPos, float partialTicks);

	public abstract EntityHitResult reverseRotation(EntityHitResult localPos, float partialTicks);

	public void tickActors() {
		boolean stalledPreviously = contraption.stalled;

		if (!l.v)
			contraption.stalled = false;

		for (MutablePair<c, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			c blockInfo = pair.left;
			MovementBehaviour actor = AllMovementBehaviours.of(blockInfo.b);

			EntityHitResult actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.a)
				.e(actor.getActiveAreaOffset(context)), 1);
			BlockPos gridPosition = new BlockPos(actorPosition);
			boolean newPosVisited =
				!context.stall && shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition);

			context.rotation = v -> applyRotation(v, 1);
			context.position = actorPosition;

			EntityHitResult oldMotion = context.motion;
			if (!actor.isActive(context))
				continue;
			if (newPosVisited && !context.stall) {
				actor.visitNewPosition(context, gridPosition);
				context.firstMovement = false;
			}
			if (!oldMotion.equals(context.motion))
				actor.onSpeedChanged(context, oldMotion, context.motion);
			actor.tick(context);
			contraption.stalled |= context.stall;
		}

		for (apx entity : cm()) {
			if (!(entity instanceof OrientedContraptionEntity))
				continue;
			if (!contraption.stabilizedSubContraptions.containsKey(entity.bR()))
				continue;
			OrientedContraptionEntity orientedCE = (OrientedContraptionEntity) entity;
			if (orientedCE.contraption != null && orientedCE.contraption.stalled) {
				contraption.stalled = true;
				break;
			}
		}

		if (!l.v) {
			if (!stalledPreviously && contraption.stalled)
				onContraptionStalled();
			R.set(STALLED, contraption.stalled);
			return;
		}

		contraption.stalled = isStalled();
	}

	protected void onContraptionStalled() {
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionStallPacket(X(), cC(), cD(), cG(), getStalledAngle()));
	}

	protected boolean shouldActorTrigger(MovementContext context, c blockInfo, MovementBehaviour actor,
		EntityHitResult actorPosition, BlockPos gridPosition) {
		EntityHitResult previousPosition = context.position;
		if (previousPosition == null)
			return false;

		context.motion = actorPosition.d(previousPosition);
		EntityHitResult relativeMotion = context.motion;
		relativeMotion = reverseRotation(relativeMotion, 1);
		context.relativeMotion = relativeMotion;
		return !new BlockPos(previousPosition).equals(gridPosition)
			|| context.relativeMotion.f() > 0 && context.firstMovement;
	}

	public void move(double x, double y, double z) {
		d(cC() + x, cD() + y, cG() + z);
	}

	public EntityHitResult getAnchorVec() {
		return cz();
	}

	public float getYawOffset() {
		return 0;
	}

	@Override
	public void d(double x, double y, double z) {
		super.d(x, y, z);
		if (contraption == null)
			return;
		Timer cbox = contraption.bounds;
		if (cbox == null)
			return;
		EntityHitResult actualVec = getAnchorVec();
		a(cbox.c(actualVec));
	}

	public static float yawFromVector(EntityHitResult vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.d, vec.entity)) / Math.PI * 180);
	}

	public static float pitchFromVector(EntityHitResult vec) {
		return (float) ((Math.acos(vec.c)) / Math.PI * 180);
	}

	public static EntityDimensions.a<?> build(EntityDimensions.a<?> builder) {
		@SuppressWarnings("unchecked")
		EntityDimensions.a<AbstractContraptionEntity> entityBuilder =
			(EntityDimensions.a<AbstractContraptionEntity>) builder;
		return entityBuilder.a(1, 1);
	}

	@Override
	protected void e() {
		this.R.startTracking(STALLED, false);
	}

	@Override
	public Packet<?> P() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		writeAdditional(compound, true);
		buffer.writeCompoundTag(compound);
	}
	
	@Override
	protected final void b(CompoundTag compound) {
		writeAdditional(compound, false);
	}
	
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		if (contraption != null)
			compound.put("Contraption", contraption.writeNBT(spawnPacket));
		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);
	}

	@Override
	public void readSpawnData(PacketByteBuf additionalData) {
		readAdditional(additionalData.readCompoundTag(), true);
	}
	
	@Override
	protected final void a(CompoundTag compound) {
		readAdditional(compound, false);
	}
	
	protected void readAdditional(CompoundTag compound, boolean spawnData) {
		initialized = compound.getBoolean("Initialized");
		contraption = Contraption.fromNBT(l, compound.getCompound("Contraption"), spawnData);
		contraption.entity = this;
		R.set(STALLED, compound.getBoolean("Stalled"));
	}

	public void disassemble() {
		if (!aW())
			return;
		if (contraption == null)
			return;

		ac();

		StructureTransform transform = makeStructureTransform();
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionDisassemblyPacket(this.X(), transform));

		contraption.addBlocksToWorld(l, transform);
		contraption.addPassengersToWorld(l, transform, cm());

		for (apx entity : cm()) {
			if (!(entity instanceof OrientedContraptionEntity))
				continue;
			UUID id = entity.bR();
			if (!contraption.stabilizedSubContraptions.containsKey(id))
				continue;
			BlockPos transformed = transform.apply(contraption.stabilizedSubContraptions.get(id)
				.getConnectedPos());
			entity.d(transformed.getX(), transformed.getY(), transformed.getZ());
			((AbstractContraptionEntity) entity).disassemble();
		}

		bd();
		moveCollidedEntitiesOnDisassembly(transform);
	}

	private void moveCollidedEntitiesOnDisassembly(StructureTransform transform) {
		for (apx entity : collidingEntities.keySet()) {
			EntityHitResult localVec = toLocalVector(entity.cz(), 0);
			EntityHitResult transformed = transform.apply(localVec);
			if (l.v)
				entity.d(transformed.entity, transformed.c + 1 / 16f, transformed.d);
			else
				entity.a(transformed.entity, transformed.c + 1 / 16f, transformed.d);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(boolean keepData) {
		if (!l.v && !y && contraption != null) {
			contraption.stop(l);
		}
		super.remove(keepData);
	}

	protected abstract StructureTransform makeStructureTransform();

	@Override
	public void Z() {
		bd();
		super.Z();
	}

	@Override
	protected void am() {
		bd();
		super.am();
	}

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		if (l != null && l.v)
			return;
		cm().forEach(apx::ac);
	}

	@Override
	protected void aL() {}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return R.get(STALLED);
	}

	@Environment(EnvType.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		apx entity = KeyBinding.B().r.a(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.handleStallInformation(packet.x, packet.y, packet.z, packet.angle);
	}

	@Environment(EnvType.CLIENT)
	static void handleDisassemblyPacket(ContraptionDisassemblyPacket packet) {
  		apx entity = KeyBinding.B().r.a(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.moveCollidedEntitiesOnDisassembly(packet.transform);
	}

	protected abstract float getStalledAngle();

	protected abstract void handleStallInformation(float x, float y, float z, float angle);

	@Override
	@SuppressWarnings("deprecation")
	public CompoundTag e(CompoundTag nbt) {
		EntityHitResult vec = cz();
		List<apx> passengers = cm();

		for (apx entity : passengers) {
			// setPos has world accessing side-effects when removed == false
			entity.y = true;

			// Gather passengers into same chunk when saving
			EntityHitResult prevVec = entity.cz();
			entity.o(vec.entity, prevVec.c, vec.d);

			// Super requires all passengers to not be removed in order to write them to the
			// tag
			entity.y = false;
		}

		CompoundTag tag = super.e(nbt);
		return tag;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void f(EntityHitResult motionIn) {}

	@Override
	public LavaFluid y_() {
		return LavaFluid.d;
	}

	public void setContraptionMotion(EntityHitResult vec) {
		super.f(vec);
	}

	@Override
	public boolean aS() {
		return false;
	}

	@Override
	public boolean a(DamageRecord source, float amount) {
		return false;
	}

	public EntityHitResult getPrevPositionVec() {
		return prevPosInvalid ? cz() : new EntityHitResult(m, n, o);
	}

	public abstract ContraptionRotationState getRotationState();

	public EntityHitResult getContactPointMotion(EntityHitResult globalContactPoint) {
		if (prevPosInvalid)
			return EntityHitResult.a;
		EntityHitResult contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0), 1);
		return contactPoint.d(globalContactPoint)
			.e(cz().d(getPrevPositionVec()));
	}

	public boolean canCollideWith(apx e) {
		if (e instanceof PlayerAbilities && e.a_())
			return false;
		if (e.H)
			return false;
		if (e instanceof WitherEntity)
			return false;
		if (e instanceof ScheduleBuilder)
			return !(contraption instanceof MountedContraption);
		if (e instanceof SuperGlueEntity)
			return false;
		if (e instanceof SeatEntity)
			return false;
		if (e instanceof FlyingItemEntity)
			return false;
		if (e.cs() != null)
			return false;

		apx riding = this.cs();
		while (riding != null) {
			if (riding == e)
				return false;
			riding = riding.cs();
		}

		return e.y_() == LavaFluid.a;
	}

	@Override
	public boolean cp() {
		return false;
	}

	public static class ContraptionRotationState {
		static final ContraptionRotationState NONE = new ContraptionRotationState();

		float xRotation = 0;
		float yRotation = 0;
		float zRotation = 0;
		float secondYRotation = 0;
		Matrix3d matrix;

		public Matrix3d asMatrix() {
			if (matrix != null)
				return matrix;

			matrix = new Matrix3d().asIdentity();
			if (xRotation != 0)
				matrix.multiply(new Matrix3d().asXRotation(AngleHelper.rad(-xRotation)));
			if (yRotation != 0)
				matrix.multiply(new Matrix3d().asYRotation(AngleHelper.rad(yRotation)));
			if (zRotation != 0)
				matrix.multiply(new Matrix3d().asZRotation(AngleHelper.rad(-zRotation)));
			return matrix;
		}

		public boolean hasVerticalRotation() {
			return xRotation != 0 || zRotation != 0;
		}

		public float getYawOffset() {
			return secondYRotation;
		}

	}

}
