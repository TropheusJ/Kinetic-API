package com.simibubi.kinetic_api.content.contraptions.relays.advanced;

import java.util.List;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import com.simibubi.kinetic_api.content.contraptions.RotationPropagator;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;

public class SpeedControllerTileEntity extends KineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	protected ScrollValueBehaviour targetSpeed;

	public SpeedControllerTileEntity(BellBlockEntity<? extends SpeedControllerTileEntity> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

		targetSpeed =
			new ScrollValueBehaviour(Lang.translate("generic.speed"), this, new ControllerValueBoxTransform());
		targetSpeed.between(-max, max);
		targetSpeed.value = DEFAULT_SPEED;
		targetSpeed.moveText(new EntityHitResult(9, 0, 10));
		targetSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
		targetSpeed.withCallback(i -> this.updateTargetRotation());
		targetSpeed.withStepFunction(CreativeMotorTileEntity::step);
		behaviours.add(targetSpeed);
	}

	private void updateTargetRotation() {
		if (hasNetwork())
			getOrCreateNetwork().remove(this);
		RotationPropagator.handleRemoved(d, e, this);
		removeSource();
		attachKinetics();
	}

	public static float getConveyedSpeed(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
			boolean targetingController) {
		if (!(speedControllerIn instanceof SpeedControllerTileEntity))
			return 0;

		float speed = speedControllerIn.getTheoreticalSpeed();
		float wheelSpeed = cogWheel.getTheoreticalSpeed();
		float desiredOutputSpeed = getDesiredOutputSpeed(cogWheel, speedControllerIn, targetingController);

		float compareSpeed = targetingController ? speed : wheelSpeed;
		if (desiredOutputSpeed >= 0 && compareSpeed >= 0)
			return Math.max(desiredOutputSpeed, compareSpeed);
		if (desiredOutputSpeed < 0 && compareSpeed < 0)
			return Math.min(desiredOutputSpeed, compareSpeed);

		return desiredOutputSpeed;
	}

	public static float getDesiredOutputSpeed(KineticTileEntity cogWheel, KineticTileEntity speedControllerIn,
			boolean targetingController) {
		SpeedControllerTileEntity speedController = (SpeedControllerTileEntity) speedControllerIn;
		float targetSpeed = speedController.targetSpeed.getValue();
		float speed = speedControllerIn.getTheoreticalSpeed();
		float wheelSpeed = cogWheel.getTheoreticalSpeed();

		if (targetSpeed == 0)
			return 0;
		if (targetingController && wheelSpeed == 0)
			return 0;
		if (!speedController.hasSource()) {
			if (targetingController)
				return targetSpeed;
			return 0;
		}

		boolean wheelPowersController = speedController.source.equals(cogWheel.o());

		if (wheelPowersController) {
			if (targetingController)
				return targetSpeed;
			return wheelSpeed;
		}

		if (targetingController)
			return speed;
		return targetSpeed;
	}

	private class ControllerValueBoxTransform extends ValueBoxTransform.Sided {

		@Override
		protected EntityHitResult getSouthLocation() {
			return VecHelper.voxelSpace(8, 11.5f, 14);
		}

		@Override
		protected boolean isSideActive(PistonHandler state, Direction direction) {
			if (direction.getAxis().isVertical())
				return false;
			return state.c(SpeedControllerBlock.HORIZONTAL_AXIS) != direction.getAxis();
		}

		@Override
		protected float getScale() {
			return 0.275f;
		}

	}

}
