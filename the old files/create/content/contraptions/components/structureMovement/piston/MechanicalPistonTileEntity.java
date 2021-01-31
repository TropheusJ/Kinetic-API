package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston;

import afj;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.base.IRotate;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.DirectionalExtenderScrollOptionSlot;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.ServerSpeedProvider;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class MechanicalPistonTileEntity extends LinearActuatorTileEntity {

	protected boolean hadCollisionWithOtherPiston;
	protected int extensionLength;

	public MechanicalPistonTileEntity(BellBlockEntity<? extends MechanicalPistonTileEntity> type) {
		super(type);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		extensionLength = compound.getInt("ExtensionLength");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		tag.putInt("ExtensionLength", extensionLength);
		super.write(tag, clientPacket);
	}

	@Override
	public void assemble() {
		if (!(d.d_(e)
			.b() instanceof MechanicalPistonBlock))
			return;

		Direction direction = p().c(BambooLeaves.M);

		// Collect Construct
		PistonContraption contraption = new PistonContraption(direction, getMovementSpeed() < 0);
		if (!contraption.assemble(d, e))
			return;

		Direction positive = Direction.get(AxisDirection.POSITIVE, direction.getAxis());
		Direction movementDirection =
			getSpeed() > 0 ^ direction.getAxis() != Axis.Z ? positive : positive.getOpposite();

		BlockPos anchor = contraption.anchor.offset(direction, contraption.initialExtensionProgress);
		if (ContraptionCollider.isCollidingWithWorld(d, contraption, anchor.offset(movementDirection),
			movementDirection))
			return;

		// Check if not at limit already
		extensionLength = contraption.extensionLength;
		float resultingOffset = contraption.initialExtensionProgress + Math.signum(getMovementSpeed()) * .5f;
		if (resultingOffset <= 0 || resultingOffset >= extensionLength) {
			return;
		}

		// Run
		running = true;
		offset = contraption.initialExtensionProgress;
		sendData();
		clientOffsetDiff = 0;

		BlockPos startPos = BlockPos.ORIGIN.offset(direction, contraption.initialExtensionProgress);
		contraption.removeBlocksFromWorld(d, startPos);
		movedContraption = ControlledContraptionEntity.create(v(), this, contraption);
		applyContraptionPosition();
		forceMove = true;
		d.c(movedContraption);
	}

	@Override
	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		if (!f)
			v().a(e, p().a(MechanicalPistonBlock.STATE, PistonState.EXTENDED),
				3 | 16);
		if (movedContraption != null) {
			applyContraptionPosition();
			movedContraption.disassemble();
		}
		running = false;
		movedContraption = null;
		sendData();

		if (f)
			AllBlocks.MECHANICAL_PISTON.get()
				.a(d, e, p(), null);
	}

	@Override
	public void collided() {
		super.collided();
		if (!running && getMovementSpeed() > 0)
			assembleNextTick = true;
	}

	@Override
	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f;
		if (d.v)
			movementSpeed *= ServerSpeedProvider.get();
		Direction pistonDirection = p().c(BambooLeaves.M);
		int movementModifier = pistonDirection.getDirection()
			.offset() * (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
		movementSpeed = movementSpeed * -movementModifier + clientOffsetDiff / 2f;

		int extensionRange = getExtensionRange();
		movementSpeed = afj.a(movementSpeed, 0 - offset, extensionRange - offset);
		return movementSpeed;
	}

	@Override
	protected int getExtensionRange() {
		return extensionLength;
	}

	@Override
	protected void visitNewPosition() {}

	@Override
	protected EntityHitResult toMotionVector(float speed) {
		Direction pistonDirection = p().c(BambooLeaves.M);
		return EntityHitResult.b(pistonDirection.getVector())
			.a(speed);
	}

	@Override
	protected EntityHitResult toPosition(float offset) {
		EntityHitResult position = EntityHitResult.b(p().c(BambooLeaves.M)
			.getVector())
			.a(offset);
		return position.e(EntityHitResult.b(movedContraption.getContraption().anchor));
	}

	@Override
	protected ValueBoxTransform getMovementModeSlot() {
		return new DirectionalExtenderScrollOptionSlot((state, d) -> {
			Axis axis = d.getAxis();
			Axis extensionAxis = state.c(MechanicalPistonBlock.FACING)
				.getAxis();
			Axis shaftAxis = ((IRotate) state.b()).getRotationAxis(state);
			return extensionAxis != axis && shaftAxis != axis;
		});
	}

	@Override
	protected int getInitialOffset() {
		return movedContraption == null ? 0
			: ((PistonContraption) movedContraption.getContraption()).initialExtensionProgress;
	}

}
