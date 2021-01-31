package com.simibubi.kinetic_api.content.contraptions.components.waterwheel;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.Iterate;

public class WaterWheelTileEntity extends GeneratingKineticTileEntity {

	private Map<Direction, Float> flows;

	public WaterWheelTileEntity(BellBlockEntity<? extends WaterWheelTileEntity> type) {
		super(type);
		flows = new HashMap<>();
		for (Direction d : Iterate.directions)
			setFlow(d, 0);
		setLazyTickRate(20);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (compound.contains("Flows")) {
			for (Direction d : Iterate.directions)
				setFlow(d, compound.getCompound("Flows")
					.getFloat(d.a()));
		}
	}

	@Override
	public Timer getRenderBoundingBox() {
		return new Timer(e).g(1);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		CompoundTag flows = new CompoundTag();
		for (Direction d : Iterate.directions)
			flows.putFloat(d.a(), this.flows.get(d));
		compound.put("Flows", flows);

		super.write(compound, clientPacket);
	}

	public void setFlow(Direction direction, float speed) {
		flows.put(direction, speed);
		X_();
	}

	@Override
	public float getGeneratedSpeed() {
		float speed = 0;
		for (Float f : flows.values())
			speed += f;
		if (speed != 0)
			speed += AllConfigs.SERVER.kinetics.waterWheelBaseSpeed.get() * Math.signum(speed);
		return speed;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		AllBlocks.WATER_WHEEL.get()
			.updateAllSides(p(), d, e);
	}

}
