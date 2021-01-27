package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;

public class AdjustablePulleyTileEntity extends KineticTileEntity {

	int signal;
	boolean signalChanged;

	public AdjustablePulleyTileEntity(BellBlockEntity<? extends AdjustablePulleyTileEntity> type) {
		super(type);
		signal = 0;
		setLazyTickRate(40);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Signal", signal);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		signal = compound.getInt("Signal");
		super.fromTag(state, compound, clientPacket);
	}

	public float getModifier() {
		return getModifierForSignal(signal);
	}

	public void neighborChanged() {
		if (!n())
			return;
		int power = d.s(e);
		if (power != signal) 
			signalChanged = true;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		neighborChanged();
	}

	@Override
	public void aj_() {
		super.aj_();
		if (signalChanged) {
			signalChanged = false;
			analogSignalChanged(d.s(e));
		}
	}

	protected void analogSignalChanged(int newSignal) {
		detachKinetics();
		removeSource();
		signal = newSignal;
		attachKinetics();
	}

	protected float getModifierForSignal(int newPower) {
		if (newPower == 0)
			return 1;
		return 1 + ((newPower + 1) / 16f);
	}

}
