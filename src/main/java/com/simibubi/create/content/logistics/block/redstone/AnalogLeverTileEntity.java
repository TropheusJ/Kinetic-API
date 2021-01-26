package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import afj;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;

public class AnalogLeverTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	int state = 0;
	int lastChange;
	InterpolatedChasingValue clientState = new InterpolatedChasingValue().withSpeed(.2f);

	public AnalogLeverTileEntity(BellBlockEntity<? extends AnalogLeverTileEntity> type) {
		super(type);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("State", state);
		compound.putInt("ChangeTimer", lastChange);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler blockState, CompoundTag compound, boolean clientPacket) {
		state = compound.getInt("State");
		lastChange = compound.getInt("ChangeTimer");
		clientState.target(state);
		super.fromTag(blockState, compound, clientPacket);
	}

	@Override
	public void aj_() {
		super.aj_();
		if (lastChange > 0) {
			lastChange--;
			if (lastChange == 0)
				updateOutput();
		}
		if (d.v)
			clientState.tick();
	}

	private void updateOutput() {
		AnalogLeverBlock.updateNeighbors(p(), d, e);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	public void changeState(boolean back) {
		int prevState = state;
		state += back ? -1 : 1;
		state = afj.a(state, 0, 15);
		if (prevState != state)
			lastChange = 15;
		sendData();
	}

	@Override
	public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.analogStrength", this.state)));

		return true;
	}

	public int getState() {
		return state;
	}
}
