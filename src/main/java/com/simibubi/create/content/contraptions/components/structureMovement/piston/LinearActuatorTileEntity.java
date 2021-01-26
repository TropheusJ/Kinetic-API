package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import java.util.List;
import afj;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.IControlContraption;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public abstract class LinearActuatorTileEntity extends KineticTileEntity implements IControlContraption {

	public float offset;
	public boolean running;
	public boolean assembleNextTick;
	public AbstractContraptionEntity movedContraption;
	protected boolean forceMove;
	protected ScrollOptionBehaviour<MovementMode> movementMode;
	protected boolean waitingForSpeedChange;

	// Custom position sync
	protected float clientOffsetDiff;

	public LinearActuatorTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
		setLazyTickRate(3);
		forceMove = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(MovementMode.class, Lang.translate("contraptions.movement_mode"),
			this, getMovementModeSlot());
		movementMode.requiresWrench();
		movementMode.withCallback(t -> waitingForSpeedChange = false);
		behaviours.add(movementMode);
	}

	@Override
	public void aj_() {
		super.aj_();

		if (movedContraption != null) {
			if (!movedContraption.aW())
				movedContraption = null;
		}

		if (d.v)
			clientOffsetDiff *= .75f;

		if (waitingForSpeedChange && movedContraption != null) {
			if (d.v) {
				float syncSpeed = clientOffsetDiff / 2f;
				offset += syncSpeed;
				movedContraption.setContraptionMotion(toMotionVector(syncSpeed));
				return;
			}
			movedContraption.setContraptionMotion(EntityHitResult.a);
			return;
		}

		if (!d.v && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				if (getSpeed() == 0)
					tryDisassemble();
				else
					sendData();
				return;
			} else {
				if (getSpeed() != 0)
					assemble();
			}
			return;
		}

		if (!running)
			return;

		boolean contraptionPresent = movedContraption != null;
		float movementSpeed = getMovementSpeed();
		float newOffset = offset + movementSpeed;
		if ((int) newOffset != (int) offset)
			visitNewPosition();

		if (!contraptionPresent || !movedContraption.isStalled())
			offset = newOffset;

		if (contraptionPresent)
			applyContraptionMotion();

		int extensionRange = getExtensionRange();
		if (offset <= 0 || offset >= extensionRange) {
			offset = offset <= 0 ? 0 : extensionRange;
			if (!d.v) {
				applyContraptionMotion();
				applyContraptionPosition();
				tryDisassemble();
				if (waitingForSpeedChange) {
					forceMove = true;
					sendData();
				}
			}
			return;
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !d.v)
			sendData();
	}

	protected int getGridOffset(float offset) {
		return afj.a((int) (offset + .5f), 0, getExtensionRange());
	}

	public float getInterpolatedOffset(float partialTicks) {
		float interpolatedOffset =
			afj.a(offset + (partialTicks - .5f) * getMovementSpeed(), 0, getExtensionRange());
		return interpolatedOffset;
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
		waitingForSpeedChange = false;
	}

	@Override
	public void al_() {
		this.f = true;
		if (!d.v)
			disassemble();
		super.al_();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putBoolean("Waiting", waitingForSpeedChange);
		compound.putFloat("Offset", offset);
		super.write(compound, clientPacket);
		
		if (clientPacket && forceMove) {
			compound.putBoolean("ForceMovement", forceMove);
			forceMove = false;
		}
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		boolean forceMovement = compound.contains("ForceMovement");
		float offsetBefore = offset;

		running = compound.getBoolean("Running");
		waitingForSpeedChange = compound.getBoolean("Waiting");
		offset = compound.getFloat("Offset");
		super.fromTag(state, compound, clientPacket);

		if (!clientPacket)
			return;
		if (forceMovement)
			applyContraptionPosition();
		else if (running) {
			clientOffsetDiff = offset - offsetBefore;
			offset = offsetBefore;
		}
		if (!running)
			movedContraption = null;
	}

	public abstract void disassemble();

	protected abstract void assemble();

	protected abstract int getExtensionRange();

	protected abstract int getInitialOffset();

	protected abstract ValueBoxTransform getMovementModeSlot();

	protected abstract EntityHitResult toMotionVector(float speed);

	protected abstract EntityHitResult toPosition(float offset);

	protected void visitNewPosition() {}

	protected void tryDisassemble() {
		if (f) {
			disassemble();
			return;
		}
		if (movementMode.get() == MovementMode.MOVE_NEVER_PLACE) {
			waitingForSpeedChange = true;
			return;
		}
		int initial = getInitialOffset();
		if ((int) (offset + .5f) != initial && movementMode.get() == MovementMode.MOVE_PLACE_RETURNED) {
			waitingForSpeedChange = true;
			return;
		}
		disassemble();
	}

	@Override
	public void collided() {
		if (d.v) {
			waitingForSpeedChange = true;
			return;
		}
		offset = getGridOffset(offset - getMovementSpeed());
		applyContraptionPosition();
		tryDisassemble();
	}

	protected void applyContraptionMotion() {
		if (movedContraption == null)
			return;
		if (movedContraption.isStalled()) {
			movedContraption.setContraptionMotion(EntityHitResult.a);
			return;
		}
		movedContraption.setContraptionMotion(getMotionVector());
	}

	protected void applyContraptionPosition() {
		if (movedContraption == null)
			return;
		EntityHitResult vec = toPosition(offset);
		movedContraption.d(vec.entity, vec.c, vec.d);
		if (getSpeed() == 0 || waitingForSpeedChange)
			movedContraption.setContraptionMotion(EntityHitResult.a);
	}

	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f + clientOffsetDiff / 2f;
		if (d.v)
			movementSpeed *= ServerSpeedProvider.get();
		return movementSpeed;
	}

	public EntityHitResult getMotionVector() {
		return toMotionVector(getMovementSpeed());
	}

	@Override
	public void onStall() {
		if (!d.v) {
			forceMove = true;
			sendData();
		}
	}

	@Override
	public boolean isValid() {
		return !q();
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		this.movedContraption = contraption;
		if (!d.v) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		return movedContraption == contraption;
	}
	
	@Override
	public BlockPos getBlockPosition() {
		return e;
	}

}