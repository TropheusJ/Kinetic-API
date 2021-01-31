package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import static com.simibubi.kinetic_api.foundation.utility.AngleHelper.angleLerp;

import afj;
import apx;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.AllEntityTypes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.Couple;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Ex: Minecarts, Couplings <br>
 * Oriented Contraption Entities can rotate freely around two axes
 * simultaneously.
 */
public class OrientedContraptionEntity extends AbstractContraptionEntity {

	private static final FireworkRocketRecipe FUEL_ITEMS = FireworkRocketRecipe.a(AliasedBlockItem.ke, AliasedBlockItem.kf);

	private static final TrackedData<Optional<UUID>> COUPLING =
		DataTracker.registerData(OrientedContraptionEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	private static final TrackedData<Direction> INITIAL_ORIENTATION =
		DataTracker.registerData(OrientedContraptionEntity.class, TrackedDataHandlerRegistry.FACING);

	protected EntityHitResult motionBeforeStall;
	protected boolean forceAngle;
	private boolean isSerializingFurnaceCart;
	private boolean attachedExtraInventories;

	public float prevYaw;
	public float yaw;
	public float targetYaw;

	public float prevPitch;
	public float pitch;
	public float targetPitch;

	// When placed using a contraption item
	private float initialYawOffset;

	public OrientedContraptionEntity(EntityDimensions<?> type, GameMode world) {
		super(type, world);
		motionBeforeStall = EntityHitResult.a;
		attachedExtraInventories = false;
		isSerializingFurnaceCart = false;
		initialYawOffset = -1;
	}

	public static OrientedContraptionEntity create(GameMode world, Contraption contraption,
		Optional<Direction> initialOrientation) {
		OrientedContraptionEntity entity =
			new OrientedContraptionEntity(AllEntityTypes.ORIENTED_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		initialOrientation.ifPresent(entity::setInitialOrientation);
		entity.startAtInitialYaw();
		return entity;
	}

	public void setInitialOrientation(Direction direction) {
		R.set(INITIAL_ORIENTATION, direction);
	}

	public Direction getInitialOrientation() {
		return R.get(INITIAL_ORIENTATION);
	}

	public void deferOrientation(Direction newInitialAngle) {
		R.set(INITIAL_ORIENTATION, Direction.UP);
		yaw = initialYawOffset = newInitialAngle.asRotation();
	}

	@Override
	public float getYawOffset() {
		return getInitialYaw();
	}

	public float getInitialYaw() {
		return (isInitialOrientationPresent() ? R.get(INITIAL_ORIENTATION) : Direction.SOUTH)
			.asRotation();
	}

	@Override
	protected void e() {
		super.e();
		R.startTracking(COUPLING, Optional.empty());
		R.startTracking(INITIAL_ORIENTATION, Direction.UP);
	}

	@Override
	public ContraptionRotationState getRotationState() {
		ContraptionRotationState crs = new ContraptionRotationState();

		float yawOffset = getYawOffset();
		crs.zRotation = pitch;
		crs.yRotation = -yaw + yawOffset;

		if (pitch != 0 && yaw != 0) {
			crs.secondYRotation = -yaw;
			crs.yRotation = yawOffset;
		}

		return crs;
	}

	@Override
	public void l() {
		if (!l.v && aW())
			disassemble();
		super.l();
	}

	@Override
	protected void readAdditional(CompoundTag compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);

		if (compound.contains("InitialOrientation"))
			setInitialOrientation(NBTHelper.readEnum(compound, "InitialOrientation", Direction.class));
		if (compound.contains("ForceYaw"))
			startAtYaw(compound.getFloat("ForceYaw"));

		ListTag vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new EntityHitResult(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(EntityHitResult.a))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			f(EntityHitResult.a);
		}

		yaw = compound.getFloat("Yaw");
		pitch = compound.getFloat("Pitch");

		setCouplingId(compound.contains("OnCoupling") ? compound.getUuid("OnCoupling") : null);
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);

		if (motionBeforeStall != null)
			compound.put("CachedMotion",
				a(motionBeforeStall.entity, motionBeforeStall.c, motionBeforeStall.d));

		Direction optional = R.get(INITIAL_ORIENTATION);
		if (optional.getAxis()
			.isHorizontal())
			NBTHelper.writeEnum(compound, "InitialOrientation", optional);
		if (forceAngle) {
			compound.putFloat("ForceYaw", yaw);
			forceAngle = false;
		}

		compound.putFloat("Yaw", yaw);
		compound.putFloat("Pitch", pitch);

		if (getCouplingId() != null)
			compound.putUuid("OnCoupling", getCouplingId());
	}

	@Override
	public void a(TrackedData<?> key) {
		super.a(key);
		if (key == INITIAL_ORIENTATION && isInitialOrientationPresent())
			startAtInitialYaw();
	}

	public boolean isInitialOrientationPresent() {
		return R.get(INITIAL_ORIENTATION)
			.getAxis()
			.isHorizontal();
	}

	public void startAtInitialYaw() {
		startAtYaw(getInitialYaw());
	}

	public void startAtYaw(float yaw) {
		targetYaw = this.yaw = prevYaw = yaw;
		forceAngle = true;
	}

	@Override
	public EntityHitResult applyRotation(EntityHitResult localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getInitialYaw(), Axis.Y);
		localPos = VecHelper.rotate(localPos, g(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, h(partialTicks), Axis.Y);
		return localPos;
	}

	@Override
	public EntityHitResult reverseRotation(EntityHitResult localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -h(partialTicks), Axis.Y);
		localPos = VecHelper.rotate(localPos, -g(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, -getInitialYaw(), Axis.Y);
		return localPos;
	}

	public float h(float partialTicks) {
		return -(partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw));
	}

	public float g(float partialTicks) {
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, prevPitch, pitch);
	}

	@Override
	protected void tickContraption() {
		apx e = cs();
		if (e == null)
			return;

		boolean rotationLock = false;
		boolean pauseWhileRotating = false;
		boolean wasStalled = isStalled();
		if (contraption instanceof MountedContraption) {
			MountedContraption mountedContraption = (MountedContraption) contraption;
			rotationLock = mountedContraption.rotationMode == CartMovementMode.ROTATION_LOCKED;
			pauseWhileRotating = mountedContraption.rotationMode == CartMovementMode.ROTATE_PAUSED;
		}

		apx riding = e;
		while (riding.cs() != null && !(contraption instanceof StabilizedContraption))
			riding = riding.cs();

		boolean isOnCoupling = false;
		UUID couplingId = getCouplingId();
		isOnCoupling = couplingId != null && riding instanceof ScheduleBuilder;

		if (!attachedExtraInventories) {
			attachInventoriesFromRidingCarts(riding, isOnCoupling, couplingId);
			attachedExtraInventories = true;
		}

		boolean rotating = updateOrientation(rotationLock, wasStalled, riding, isOnCoupling);
		if (!rotating || !pauseWhileRotating)
			tickActors();
		boolean isStalled = isStalled();

		LazyOptional<MinecartController> capability =
			riding.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (capability.isPresent()) {
			if (!l.isClient())
				capability.orElse(null)
					.setStalledExternally(isStalled);
		} else {
			if (isStalled) {
				if (!wasStalled)
					motionBeforeStall = riding.cB();
				riding.n(0, 0, 0);
			}
			if (wasStalled && !isStalled) {
				riding.f(motionBeforeStall);
				motionBeforeStall = EntityHitResult.a;
			}
		}

		if (l.v)
			return;

		if (!isStalled()) {
			if (isOnCoupling) {
				Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
				if (coupledCarts == null)
					return;
				coupledCarts.map(MinecartController::cart)
					.forEach(this::powerFurnaceCartWithFuelFromStorage);
				return;
			}
			powerFurnaceCartWithFuelFromStorage(riding);
		}
	}

	protected boolean updateOrientation(boolean rotationLock, boolean wasStalled, apx riding, boolean isOnCoupling) {
		if (isOnCoupling) {
			Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
			if (coupledCarts == null)
				return false;

			EntityHitResult positionVec = coupledCarts.getFirst()
				.cart()
				.cz();
			EntityHitResult coupledVec = coupledCarts.getSecond()
				.cart()
				.cz();

			double diffX = positionVec.entity - coupledVec.entity;
			double diffY = positionVec.c - coupledVec.c;
			double diffZ = positionVec.d - coupledVec.d;

			prevYaw = yaw;
			prevPitch = pitch;
			yaw = (float) (afj.d(diffZ, diffX) * 180 / Math.PI);
			pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI);

			if (getCouplingId().equals(riding.bR())) {
				pitch *= -1;
				yaw += 180;
			}
			return false;
		}

		if (contraption instanceof StabilizedContraption) {
			if (!(riding instanceof OrientedContraptionEntity))
				return false;
			StabilizedContraption stabilized = (StabilizedContraption) contraption;
			Direction facing = stabilized.getFacing();
			if (facing.getAxis()
				.isVertical())
				return false;
			OrientedContraptionEntity parent = (OrientedContraptionEntity) riding;
			prevYaw = yaw;
			yaw = -parent.h(1);
			return false;
		}

		prevYaw = yaw;
		if (wasStalled)
			return false;

		boolean rotating = false;
		EntityHitResult movementVector = riding.cB();

		if (!(riding instanceof ScheduleBuilder))
			movementVector = cz().a(m, n, o);
		EntityHitResult motion = movementVector.d();

		if (!isInitialOrientationPresent() && !l.v) {
			if (motion.f() > 0) {
				Direction facingFromVector = Direction.getFacing(motion.entity, motion.c, motion.d);
				if (initialYawOffset != -1)
					facingFromVector = Direction.fromRotation(facingFromVector.asRotation() - initialYawOffset);
				if (facingFromVector.getAxis()
					.isHorizontal())
					setInitialOrientation(facingFromVector);
			}
		}

		if (!rotationLock) {
			if (motion.f() > 0) {
				targetYaw = yawFromVector(motion);
				if (targetYaw < 0)
					targetYaw += 360;
				if (yaw < 0)
					yaw += 360;
			}

			prevYaw = yaw;
			yaw = angleLerp(0.4f, yaw, targetYaw);
			if (Math.abs(AngleHelper.getShortestAngleDiff(yaw, targetYaw)) < 1f)
				yaw = targetYaw;
			else
				rotating = true;
		}
		return rotating;
	}

	protected void powerFurnaceCartWithFuelFromStorage(apx riding) {
		if (!(riding instanceof MinecartEntity))
			return;
		MinecartEntity furnaceCart = (MinecartEntity) riding;

		// Notify to not trigger serialization side-effects
		isSerializingFurnaceCart = true;
		CompoundTag nbt = furnaceCart.serializeNBT();
		isSerializingFurnaceCart = false;

		int fuel = nbt.getInt("Fuel");
		int fuelBefore = fuel;
		double pushX = nbt.getDouble("PushX");
		double pushZ = nbt.getDouble("PushZ");

		int i = afj.c(furnaceCart.cC());
		int j = afj.c(furnaceCart.cD());
		int k = afj.c(furnaceCart.cG());
		if (furnaceCart.l.d_(new BlockPos(i, j - 1, k))
			.a(StatHandler.H))
			--j;

		BlockPos blockpos = new BlockPos(i, j, k);
		PistonHandler blockstate = this.l.d_(blockpos);
		if (furnaceCart.canUseRail() && blockstate.a(StatHandler.H))
			if (fuel > 1)
				riding.f(riding.cB()
					.d()
					.a(1));
		if (fuel < 5 && contraption != null) {
			ItemCooldownManager coal = ItemHelper.extract(contraption.inventory, FUEL_ITEMS, 1, false);
			if (!coal.a())
				fuel += 3600;
		}

		if (fuel != fuelBefore || pushX != 0 || pushZ != 0) {
			nbt.putInt("Fuel", fuel);
			nbt.putDouble("PushX", 0);
			nbt.putDouble("PushZ", 0);
			furnaceCart.deserializeNBT(nbt);
		}
	}

	@Nullable
	public Couple<MinecartController> getCoupledCartsIfPresent() {
		UUID couplingId = getCouplingId();
		if (couplingId == null)
			return null;
		MinecartController controller = CapabilityMinecartController.getIfPresent(l, couplingId);
		if (controller == null || !controller.isPresent())
			return null;
		UUID coupledCart = controller.getCoupledCart(true);
		MinecartController coupledController = CapabilityMinecartController.getIfPresent(l, coupledCart);
		if (coupledController == null || !coupledController.isPresent())
			return null;
		return Couple.create(controller, coupledController);
	}

	protected void attachInventoriesFromRidingCarts(apx riding, boolean isOnCoupling, UUID couplingId) {
		if (isOnCoupling) {
			Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
			if (coupledCarts == null)
				return;
			coupledCarts.map(MinecartController::cart)
				.forEach(contraption::addExtraInventories);
			return;
		}
		contraption.addExtraInventories(riding);
	}

	@Override
	public CompoundTag e(CompoundTag nbt) {
		return isSerializingFurnaceCart ? nbt : super.e(nbt);
	}

	@Nullable
	public UUID getCouplingId() {
		Optional<UUID> uuid = R.get(COUPLING);
		return uuid == null ? null : uuid.isPresent() ? uuid.get() : null;
	}

	public void setCouplingId(UUID id) {
		R.set(COUPLING, Optional.ofNullable(id));
	}

	@Override
	public EntityHitResult getAnchorVec() {
		return new EntityHitResult(cC() - .5, cD(), cG() - .5);
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		BlockPos offset = new BlockPos(getAnchorVec().b(.5, .5, .5));
		return new StructureTransform(offset, 0, -yaw + getInitialYaw(), 0);
	}

	@Override
	protected float getStalledAngle() {
		return yaw;
	}

	@Override
	protected void handleStallInformation(float x, float y, float z, float angle) {
		yaw = angle;
	}

}
