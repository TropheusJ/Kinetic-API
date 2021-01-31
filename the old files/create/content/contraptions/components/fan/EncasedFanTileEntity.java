package com.simibubi.kinetic_api.content.contraptions.components.fan;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.AllTags.AllBlockTags;
import com.simibubi.kinetic_api.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;


@MethodsReturnNonnullByDefault
public class EncasedFanTileEntity extends GeneratingKineticTileEntity implements IAirCurrentSource {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean isGenerator;
	protected boolean updateAirFlow;

	public EncasedFanTileEntity(BellBlockEntity<? extends EncasedFanTileEntity> type) {
		super(type);
		isGenerator = false;
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		isGenerator = compound.getBoolean("Generating");
		if (clientPacket)
			airCurrent.rebuild();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Generating", isGenerator);
		super.write(compound, clientPacket);
	}

	@Override
	public float calculateAddedStressCapacity() {
		return isGenerator ? super.calculateAddedStressCapacity() : 0;
	}

	@Override
	public float calculateStressApplied() {
		return isGenerator ? 0 : super.calculateStressApplied();
	}

	@Override
	public float getGeneratedSpeed() {
		return isGenerator ? AllConfigs.SERVER.kinetics.generatingFanSpeed.get() : 0;
	}

	public void updateGenerator(Direction facing) {
		boolean shouldGenerate = d.r(e) && facing == Direction.DOWN
			&& d.p(e.down()) && blockBelowIsHot();
		if (shouldGenerate == isGenerator)
			return;

		isGenerator = shouldGenerate;
		updateGeneratedRotation();
	}

	public boolean blockBelowIsHot() {
		if (d == null)
			return false;
		PistonHandler checkState = d.d_(e.down());

		if (!checkState.b()
			.a(AllBlockTags.FAN_HEATERS.tag))
			return false;

		if (BlockHelper.hasBlockStateProperty(checkState, BlazeBurnerBlock.HEAT_LEVEL) && !checkState.c(BlazeBurnerBlock.HEAT_LEVEL)
			.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
			return false;

		if (BlockHelper.hasBlockStateProperty(checkState, BambooLeaves.r) && !checkState.c(BambooLeaves.r))
			return false;

		return true;
	}

	@Override
	public AirCurrent getAirCurrent() {
		return airCurrent;
	}

	@Nullable
	@Override
	public GameMode getAirCurrentWorld() {
		return d;
	}

	@Override
	public BlockPos getAirCurrentPos() {
		return e;
	}

	@Override
	public Direction getAirflowOriginSide() {
		return this.p()
			.c(EncasedFanBlock.FACING);
	}

	@Override
	public Direction getAirFlowDirection() {
		float speed = getSpeed();
		if (speed == 0)
			return null;
		Direction facing = p().c(BambooLeaves.M);
		speed = convertToDirection(speed, facing);
		return speed > 0 ? facing : facing.getOpposite();
	}

	@Override
	public boolean isSourceRemoved() {
		return f;
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		updateAirFlow = true;
		updateChute();
	}

	public void updateChute() {
		Direction direction = p().c(EncasedFanBlock.FACING);
		if (!direction.getAxis()
			.isVertical())
			return;
		BeehiveBlockEntity poweredChute = d.c(e.offset(direction));
		if (!(poweredChute instanceof ChuteTileEntity))
			return;
		ChuteTileEntity chuteTE = (ChuteTileEntity) poweredChute;
		if (direction == Direction.DOWN)
			chuteTE.updatePull();
		else
			chuteTE.updatePush(1);
	}

	public void blockInFrontChanged() {
		updateAirFlow = true;
	}

	@Override
	public void aj_() {
		super.aj_();

		if (!d.v && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = AllConfigs.SERVER.kinetics.fanBlockCheckRate.get();
			updateAirFlow = true;
		}

		if (updateAirFlow) {
			updateAirFlow = false;
			airCurrent.rebuild();
			sendData();
		}

		if (getSpeed() == 0 || isGenerator)
			return;

		if (entitySearchCooldown-- <= 0) {
			entitySearchCooldown = 5;
			airCurrent.findEntities();
		}

		airCurrent.tick();
	}

}
