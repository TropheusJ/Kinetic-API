package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import static com.simibubi.kinetic_api.foundation.utility.AngleHelper.angleLerp;

import com.simibubi.kinetic_api.AllEntityTypes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Ex: Pistons, bearings <br>
 * Controlled Contraption Entities can rotate around one axis and translate.
 * <br>
 * They are bound to an {@link IControlContraption}
 */
public class ControlledContraptionEntity extends AbstractContraptionEntity {

	protected BlockPos controllerPos;
	protected Axis rotationAxis;
	protected float prevAngle;
	protected float angle;

	public ControlledContraptionEntity(EntityDimensions<?> type, GameMode world) {
		super(type, world);
	}

	public static ControlledContraptionEntity create(GameMode world, IControlContraption controller,
		Contraption contraption) {
		ControlledContraptionEntity entity =
			new ControlledContraptionEntity(AllEntityTypes.CONTROLLED_CONTRAPTION.get(), world);
		entity.controllerPos = controller.getBlockPosition();
		entity.setContraption(contraption);
		return entity;
	}

	public boolean supportsTerrainCollision() {
		return contraption instanceof TranslatingContraption;
	}
	
	@Override
		public EntityHitResult getContactPointMotion(EntityHitResult globalContactPoint) {
			if (contraption instanceof TranslatingContraption)
				return cB();
			return super.getContactPointMotion(globalContactPoint);
		}

	@Override
	protected void setContraption(Contraption contraption) {
		super.setContraption(contraption);
		if (contraption instanceof BearingContraption)
			rotationAxis = ((BearingContraption) contraption).getFacing()
				.getAxis();

	}

	@Override
	protected void readAdditional(CompoundTag compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);
		controllerPos = NbtHelper.toBlockPos(compound.getCompound("Controller"));
		if (compound.contains("Axis"))
			rotationAxis = NBTHelper.readEnum(compound, "Axis", Axis.class);
		angle = compound.getFloat("Angle");
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);
		compound.put("Controller", NbtHelper.fromBlockPos(controllerPos));
		if (rotationAxis != null)
			NBTHelper.writeEnum(compound, "Axis", rotationAxis);
		compound.putFloat("Angle", angle);
	}

	@Override
	public ContraptionRotationState getRotationState() {
		ContraptionRotationState crs = new ContraptionRotationState();
		if (rotationAxis == Axis.X)
			crs.xRotation = angle;
		if (rotationAxis == Axis.Y)
			crs.yRotation = angle;
		if (rotationAxis == Axis.Z)
			crs.zRotation = angle;
		return crs;
	}

	@Override
	public EntityHitResult applyRotation(EntityHitResult localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getAngle(partialTicks), rotationAxis);
		return localPos;
	}

	@Override
	public EntityHitResult reverseRotation(EntityHitResult localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -getAngle(partialTicks), rotationAxis);
		return localPos;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle(float partialTicks) {
		return partialTicks == 1.0F ? angle : angleLerp(partialTicks, prevAngle, angle);
	}

	public void setRotationAxis(Axis rotationAxis) {
		this.rotationAxis = rotationAxis;
	}

	public Axis getRotationAxis() {
		return rotationAxis;
	}

	@Override
	public void a(double p_70634_1_, double p_70634_3_, double p_70634_5_) {}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(double x, double y, double z, float yw, float pt, int inc, boolean t) {}

	protected void tickContraption() {
		prevAngle = angle;
		tickActors();
		
		if (controllerPos == null)
			return;
		if (!l.p(controllerPos))
			return;
		IControlContraption controller = getController();
		if (controller == null) {
			ac();
			return;
		}
		if (!controller.isAttachedTo(this)) {
			controller.attach(this);
			if (l.v)
				d(cC(), cD(), cG());
		}

		EntityHitResult motion = cB();
		if (motion.f() < 1 / 4098f)
			f(EntityHitResult.a);
		move(motion.entity, motion.c, motion.d);
		if (ContraptionCollider.collideBlocks(this))
			getController().collided();
	}

	@Override
	protected boolean shouldActorTrigger(MovementContext context, c blockInfo, MovementBehaviour actor,
		EntityHitResult actorPosition, BlockPos gridPosition) {
		if (super.shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition))
			return true;

		// Special activation timer for actors in the center of a bearing contraption
		if (!(contraption instanceof BearingContraption))
			return false;
		BearingContraption bc = (BearingContraption) contraption;
		Direction facing = bc.getFacing();
		EntityHitResult activeAreaOffset = actor.getActiveAreaOffset(context);
		if (!activeAreaOffset.h(VecHelper.axisAlingedPlaneOf(EntityHitResult.b(facing.getVector())))
			.equals(EntityHitResult.a))
			return false;
		if (!VecHelper.onSameAxis(blockInfo.a, BlockPos.ORIGIN, facing.getAxis()))
			return false;
		context.motion = EntityHitResult.b(facing.getVector()).a(angle - prevAngle);
		context.relativeMotion = context.motion;
		int timer = context.data.getInt("StationaryTimer");
		if (timer > 0) {
			context.data.putInt("StationaryTimer", timer - 1);
			return false;
		}

		context.data.putInt("StationaryTimer", 20);
		return true;
	}

	protected IControlContraption getController() {
		if (controllerPos == null)
			return null;
		if (!l.p(controllerPos))
			return null;
		BeehiveBlockEntity te = l.c(controllerPos);
		if (!(te instanceof IControlContraption))
			return null;
		return (IControlContraption) te;
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		BlockPos offset = new BlockPos(getAnchorVec().b(.5, .5, .5));
		float xRot = rotationAxis == Axis.X ? angle : 0;
		float yRot = rotationAxis == Axis.Y ? angle : 0;
		float zRot = rotationAxis == Axis.Z ? angle : 0;
		return new StructureTransform(offset, xRot, yRot, zRot);
	}

	@Override
	protected void onContraptionStalled() {
		IControlContraption controller = getController();
		if (controller != null)
			controller.onStall();
		super.onContraptionStalled();
	}

	@Override
	protected float getStalledAngle() {
		return angle;
	}

	@Override
	protected void handleStallInformation(float x, float y, float z, float angle) {
		o(x, y, z);
		this.angle = angle;
	}
}
