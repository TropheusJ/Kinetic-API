package com.simibubi.kinetic_api.content.logistics.block.diodes;

import static com.simibubi.kinetic_api.content.logistics.block.diodes.AdjustableRepeaterBlock.POWERING;
import static net.minecraft.block.DaylightDetectorBlock.SHAPE;

import afj;
import java.util.List;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import com.simibubi.kinetic_api.foundation.utility.Lang;

public class AdjustableRepeaterTileEntity extends SmartTileEntity {

	public int state;
	public boolean charging;
	ScrollValueBehaviour maxState;

	public AdjustableRepeaterTileEntity(BellBlockEntity<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		maxState = new ScrollValueBehaviour(Lang.translate("generic.delay"), this, new AdjustableRepeaterScrollSlot())
				.between(1, 60 * 20 * 30);
		maxState.withStepFunction(this::step);
		maxState.withFormatter(this::format);
		maxState.withUnit(this::getUnit);
		maxState.withCallback(this::onMaxDelayChanged);
		
		behaviours.add(maxState);
	}
	
	private void onMaxDelayChanged(int newMax) {
		state = afj.a(state, 0, newMax);
		sendData();
	}

	@Override
	protected void fromTag(PistonHandler blockState, CompoundTag compound, boolean clientPacket) {
		state = compound.getInt("State");
		charging = compound.getBoolean("Charging");
		super.fromTag(blockState, compound, clientPacket);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("State", state);
		compound.putBoolean("Charging", charging);
		super.write(compound, clientPacket);
	}

	private int step(StepContext context) {
		int value = context.currentValue;
		if (!context.forward)
			value--;

		if (value < 20)
			return 1;
		if (value < 20 * 60)
			return 20;
		return 20 * 60;
	}

	private String format(int value) {
		if (value < 20)
			return value + "t";
		if (value < 20 * 60)
			return (value / 20) + "s";
		return (value / 20 / 60) + "m";
	}

	private Text getUnit(int value) {
		if (value < 20)
			return Lang.translate("generic.unit.ticks");
		if (value < 20 * 60)
			return Lang.translate("generic.unit.seconds");
		return Lang.translate("generic.unit.minutes");
	}

	@Override
	public void aj_() {
		super.aj_();
		boolean powered = p().c(SHAPE);
		boolean powering = p().c(POWERING);
		boolean atMax = state >= maxState.getValue();
		boolean atMin = state <= 0;
		updateState(powered, powering, atMax, atMin);
	}

	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (!charging && powered)
			charging = true;

		if (charging && atMax) {
			if (!powering && !d.v)
				d.a(e, p().a(POWERING, true));
			if (!powered)
				charging = false;
			return;
		}

		if (!charging && atMin) {
			if (powering && !d.v)
				d.a(e, p().a(POWERING, false));
			return;
		}

		state += charging ? 1 : -1;
	}

}
