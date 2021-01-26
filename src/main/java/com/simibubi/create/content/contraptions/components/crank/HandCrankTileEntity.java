package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;

public class HandCrankTileEntity extends GeneratingKineticTileEntity {

	public int inUse;
	public boolean backwards;
	public float independentAngle;
	public float chasingVelocity;

	public HandCrankTileEntity(BellBlockEntity<? extends HandCrankTileEntity> type) {
		super(type);
	}

	public void turn(boolean back) {
		boolean update = false;

		if (getGeneratedSpeed() == 0 || back != backwards)
			update = true;

		inUse = 10;
		this.backwards = back;
		if (update && !d.v)
			updateGeneratedRotation();
	}

	@Override
	public float getGeneratedSpeed() {
		BeetrootsBlock block = p().b();
		if (!(block instanceof HandCrankBlock))
			return 0;
		HandCrankBlock crank = (HandCrankBlock) block;
		int speed = (inUse == 0 ? 0 : backwards ? -1 : 1) * crank.getRotationSpeed();
		return convertToDirection(speed, p().c(HandCrankBlock.FACING));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("InUse", inUse);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		inUse = compound.getInt("InUse");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void aj_() {
		super.aj_();

		float actualSpeed = getSpeed();
		chasingVelocity += ((actualSpeed * 10 / 3f) - chasingVelocity) * .25f;
		independentAngle += chasingVelocity;

		if (inUse > 0) {
			inUse--;

			if (inUse == 0 && !d.v)
				updateGeneratedRotation();
		}
	}
	
	@Override
	protected BeetrootsBlock getStressConfigKey() {
		return AllBlocks.HAND_CRANK.get();
	}

}