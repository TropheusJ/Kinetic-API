package com.simibubi.kinetic_api.content.contraptions.components.actors;

import java.util.List;
import afj;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.LerpedFloat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PortableStorageInterfaceTileEntity extends SmartTileEntity {

	protected int transferTimer;
	protected float distance;
	protected LerpedFloat connectionAnimation;
	protected boolean powered;

	public PortableStorageInterfaceTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		transferTimer = 0;
		connectionAnimation = LerpedFloat.linear()
			.startWithValue(0);
		powered = false;
	}

	public void startTransferringTo(Contraption contraption, float distance) {
		this.distance = distance;
		startConnecting();
		notifyUpdate();
	}

	protected abstract void stopTransferring();

	protected abstract void invalidateCapability();

	@Override
	public void aj_() {
		super.aj_();
		boolean wasConnected = isConnected();

		if (transferTimer > 0) {
			transferTimer--;
			if (transferTimer == 0 || powered)
				stopTransferring();
		}

		boolean isConnected = isConnected();
		if (wasConnected != isConnected && !d.v)
			X_();

		float progress = 0;
		int timeUnit = getTransferTimeout() / 2;
		if (isConnected)
			progress = 1;
		else if (transferTimer >= timeUnit * 3)
			progress = afj.g((transferTimer - timeUnit * 3) / (float) timeUnit, 1, 0);
		else if (transferTimer < timeUnit)
			progress = afj.g(transferTimer / (float) timeUnit, 0, 1);
		connectionAnimation.setValue(progress);
	}

	@Override
	public void al_() {
		super.al_();
		invalidateCapability();
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		transferTimer = compound.getInt("Timer");
		distance = compound.getFloat("Distance");
		powered = compound.getBoolean("Powered");
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("Timer", transferTimer);
		compound.putFloat("Distance", distance);
		compound.putBoolean("Powered", powered);
	}

	public void neighbourChanged() {
		boolean isBlockPowered = d.r(e);
		if (isBlockPowered == powered)
			return;
		powered = isBlockPowered;
		sendData();
	}
	
	public boolean isPowered() {
		return powered;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().g(2);
	}

	public boolean isTransferring() {
		return transferTimer != 0;
	}

	boolean isConnected() {
		int timeUnit = getTransferTimeout() / 2;
		return transferTimer >= timeUnit && transferTimer <= timeUnit * 3;
	}

	float getExtensionDistance(float partialTicks) {
		return connectionAnimation.getValue(partialTicks) * distance / 2;
	}

	float getConnectionDistance() {
		return distance;
	}

	public void startConnecting() {
		transferTimer = getTransferTimeout() * 2;
	}

	public void onContentTransferred() {
		int timeUnit = getTransferTimeout() / 2;
		transferTimer = timeUnit * 3;
		sendData();
	}

	protected Integer getTransferTimeout() {
		return AllConfigs.SERVER.logistics.psiTimeout.get();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
