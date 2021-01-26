package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import static net.minecraft.block.enums.BambooLeaves.M;

import afj;
import java.util.List;

import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity implements IBearingTileEntity {

	protected ScrollOptionBehaviour<RotationMode> movementMode;
	protected ControlledContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected float clientAngleDiff;

	public MechanicalBearingTileEntity(BellBlockEntity<? extends MechanicalBearingTileEntity> type) {
		super(type);
		setLazyTickRate(3);
	}

	@Override
	public boolean isWoodenTop() {
		return false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(RotationMode.class, Lang.translate("contraptions.movement_mode"),
			this, getMovementModeSlot());
		movementMode.requiresWrench();
		behaviours.add(movementMode);
	}

	@Override
	public void al_() {
		if (!d.v)
			disassemble();
		super.al_();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putFloat("Angle", angle);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		float angleBefore = angle;
		running = compound.getBoolean("Running");
		angle = compound.getFloat("Angle");
		super.fromTag(state, compound, clientPacket);

		if (!clientPacket)
			return;
		if (running) {
			clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
			angle = angleBefore;
		} else
			movedContraption = null;
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (movedContraption == null || movedContraption.isStalled() || !running)
			partialTicks = 0;
		return afj.g(partialTicks, angle, angle + getAngularSpeed());
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	public float getAngularSpeed() {
		float speed = (isWindmill() ? getGeneratedSpeed() : getSpeed()) * 3 / 10f;
		if (getSpeed() == 0)
			speed = 0;
		if (d.v) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	protected boolean isWindmill() {
		return false;
	}

	@Override
	public BlockPos getBlockPosition() {
		return e;
	}

	public void assemble() {
		if (!(d.d_(e)
			.b() instanceof BearingBlock))
			return;

		Direction direction = p().c(M);
		BearingContraption contraption = new BearingContraption(isWindmill(), direction);
		if (!contraption.assemble(d, e))
			return;

		if (isWindmill())
			AllTriggers.triggerForNearbyPlayers(AllTriggers.WINDMILL, d, e, 5);
		if (contraption.getSailBlocks() >= 16 * 8)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.MAXED_WINDMILL, d, e, 5);
		
		contraption.removeBlocksFromWorld(d, BlockPos.ORIGIN);
		movedContraption = ControlledContraptionEntity.create(d, this, contraption);
		BlockPos anchor = e.offset(direction);
		movedContraption.d(anchor.getX(), anchor.getY(), anchor.getZ());
		movedContraption.setRotationAxis(direction.getAxis());
		d.c(movedContraption);

		running = true;
		angle = 0;
		sendData();
		updateGeneratedRotation();
	}

	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		angle = 0;
		if (isWindmill())
			applyRotation();
		if (movedContraption != null)
			movedContraption.disassemble();

		movedContraption = null;
		running = false;
		updateGeneratedRotation();
		assembleNextTick = false;
		sendData();
	}

	@Override
	public void aj_() {
		super.aj_();

		if (d.v)
			clientAngleDiff /= 2;

		if (!d.v && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = movementMode.get() == RotationMode.ROTATE_PLACE
					|| (isNearInitialAngle() && movementMode.get() == RotationMode.ROTATE_PLACE_RETURNED);
				if (speed == 0 && (canDisassemble || movedContraption == null || movedContraption.getContraption()
					.getBlocks()
					.isEmpty())) {
					if (movedContraption != null)
						movedContraption.getContraption()
							.stop(d);
					disassemble();
				}
				return;
			} else {
				if (speed == 0 && !isWindmill())
					return;
				assemble();
			}
			return;
		}

		if (!running)
			return;

		if (!(movedContraption != null && movedContraption.isStalled())) {
			float angularSpeed = getAngularSpeed();
			float newAngle = angle + angularSpeed;
			angle = (float) (newAngle % 360);
		}

		applyRotation();
	}

	public boolean isNearInitialAngle() {
		return Math.abs(angle) < 45 || Math.abs(angle) > 7 * 45;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !d.v)
			sendData();
	}

	protected void applyRotation() {
		if (movedContraption == null)
			return;
		movedContraption.setAngle(angle);
		PistonHandler blockState = p();
		if (blockState.b(BambooLeaves.M))
			movedContraption.setRotationAxis(blockState.c(BambooLeaves.M)
				.getAxis());
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		PistonHandler blockState = p();
		if (!(contraption.getContraption() instanceof BearingContraption))
			return;
		if (!BlockHelper.hasBlockStateProperty(blockState, M))
			return;

		this.movedContraption = contraption;
		X_();
		BlockPos anchor = e.offset(blockState.c(M));
		movedContraption.d(anchor.getX(), anchor.getY(), anchor.getZ());
		if (!d.v) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public void onStall() {
		if (!d.v)
			sendData();
	}

	@Override
	public boolean isValid() {
		return !q();
	}

	@Override
	public void collided() {}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		return movedContraption == contraption;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;
		if (isPlayerSneaking)
			return false;
		if (!isWindmill() && getSpeed() == 0)
			return false;
		if (running)
			return false;
		PistonHandler state = p();
		if (!(state.b() instanceof BearingBlock))
			return false;
		
		PistonHandler attachedState = d.d_(e.offset(state.c(BearingBlock.FACING)));
		if (attachedState.c()
			.e())
			return false;
		TooltipHelper.addHint(tooltip, "hint.empty_bearing");
		return true;
	}

}
