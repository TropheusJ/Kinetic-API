package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing;

import java.util.List;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import afj;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.WindmillBearingTileEntity.RotationDirection;
import com.simibubi.kinetic_api.foundation.gui.AllIcons;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Lang;

public class WindmillBearingTileEntity extends MechanicalBearingTileEntity {

	protected ScrollOptionBehaviour<RotationDirection> movementDirection;
	protected float lastGeneratedSpeed;

	public WindmillBearingTileEntity(BellBlockEntity<? extends MechanicalBearingTileEntity> type) {
		super(type);
	}

	@Override
	public void updateGeneratedRotation() {
		super.updateGeneratedRotation();
		lastGeneratedSpeed = getGeneratedSpeed();
	}
	
	@Override
	public void onSpeedChanged(float prevSpeed) {
		boolean cancelAssembly = assembleNextTick;
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = cancelAssembly;
	}

	@Override
	public float getGeneratedSpeed() {
		if (!running)
			return 0;
		if (movedContraption == null)
			return lastGeneratedSpeed;
		int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks() / 8;
		return afj.a(sails, 1, 16) * getAngleSpeedDirection();
	}

	@Override
	protected boolean isWindmill() {
		return true;
	}

	protected float getAngleSpeedDirection() {
		RotationDirection rotationDirection = RotationDirection.values()[movementDirection.getValue()];
		return (rotationDirection == RotationDirection.CLOCKWISE ? 1 : -1);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("LastGenerated", lastGeneratedSpeed);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		lastGeneratedSpeed = compound.getFloat("LastGenerated");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.remove(movementMode);
		movementDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
			Lang.translate("contraptions.windmill.rotation_direction"), this, getMovementModeSlot());
		movementDirection.requiresWrench();
		movementDirection.withCallback($ -> onDirectionChanged());
		behaviours.add(movementDirection);
	}

	private void onDirectionChanged() {
		if (!running)
			return;
		if (!d.v)
			updateGeneratedRotation();
	}

	@Override
	public boolean isWoodenTop() {
		return true;
	}

	static enum RotationDirection implements INamedIconOptions {

		CLOCKWISE(AllIcons.I_REFRESH), COUNTER_CLOCKWISE(AllIcons.I_ROTATE_CCW),

		;

		private String translationKey;
		private AllIcons icon;

		private RotationDirection(AllIcons icon) {
			this.icon = icon;
			translationKey = "generic." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}

	}

}
