package com.simibubi.create.content.logistics.block.diodes;

import static com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock.POWERING;

import net.minecraft.block.entity.BellBlockEntity;

public class AdjustablePulseRepeaterTileEntity extends AdjustableRepeaterTileEntity {

	public AdjustablePulseRepeaterTileEntity(BellBlockEntity<? extends AdjustablePulseRepeaterTileEntity> type) {
		super(type);
	}

	@Override
	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (!charging && powered && !atMax)
			charging = true;

		if (charging && atMax) {
			if (powering) {
				d.a(e, p().a(POWERING, false));
				charging = false;
				return;
			}
			if (!powering && !d.v)
				d.a(e, p().a(POWERING, true));
			return;
		}
		
		if (!charging && powered)
			return;

		if (!charging && !atMin) {
			if (!d.v)
				d.a(e, p().a(POWERING, false));
			state = 0;
			return;
		}

		state += charging ? 1 : 0;
	}
	
}
