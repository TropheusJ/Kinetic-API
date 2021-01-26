package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class HosePulleyTileEntity extends KineticTileEntity {

	LerpedFloat offset;
	boolean isMoving;

	private SmartFluidTank internalTank;
	private LazyOptional<IFluidHandler> capability;
	private FluidDrainingBehaviour drainer;
	private FluidFillingBehaviour filler;
	private HosePulleyFluidHandler handler;
	private boolean infinite;

	public HosePulleyTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
		offset = LerpedFloat.linear()
			.startWithValue(0);
		isMoving = true;
		internalTank = new SmartFluidTank(1500, this::onTankContentsChanged);
		handler = new HosePulleyFluidHandler(internalTank, filler, drainer,
			() -> e.down((int) Math.ceil(offset.getValue())), () -> !this.isMoving);
		capability = LazyOptional.of(() -> handler);
	}

	@Override
	public void sendData() {
		infinite = filler.infinite || drainer.infinite;
		super.sendData();
	}

	@Override
	public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		boolean addToGoggleTooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (infinite)
			TooltipHelper.addHint(tooltip, "hint.hose_pulley");
		return addToGoggleTooltip;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		drainer = new FluidDrainingBehaviour(this);
		filler = new FluidFillingBehaviour(this);
		behaviours.add(drainer);
		behaviours.add(filler);
		super.addBehaviours(behaviours);
	}

	protected void onTankContentsChanged(FluidStack contents) {}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		isMoving = true;
		if (getSpeed() == 0) {
			offset.forceNextSync();
			offset.setValue(Math.round(offset.getValue()));
			isMoving = false;
		}

		if (isMoving) {
			float newOffset = offset.getValue() + getMovementSpeed();
			if (newOffset < 0)
				isMoving = false;
			if (!d.d_(e.down((int) Math.ceil(newOffset)))
				.c()
				.e()) {
				isMoving = false;
			}
			if (isMoving) {
				drainer.reset();
				filler.reset();
			}
		}

		super.onSpeedChanged(previousSpeed);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().b(0, -offset.getValue(), 0);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public double i() {
		return super.i() + offset.getValue() * offset.getValue();
	}

	@Override
	public void aj_() {
		super.aj_();
		float newOffset = offset.getValue() + getMovementSpeed();
		if (newOffset < 0) {
			newOffset = 0;
			isMoving = false;
		}
		if (!d.d_(e.down((int) Math.ceil(newOffset)))
			.c()
			.e()) {
			newOffset = (int) newOffset;
			isMoving = false;
		}
		if (getSpeed() == 0)
			isMoving = false;

		offset.setValue(newOffset);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (d.v)
			return;
		if (isMoving)
			return;

		int ceil = (int) Math.ceil(offset.getValue() + getMovementSpeed());
		if (getMovementSpeed() > 0 && d.d_(e.down(ceil))
			.c()
			.e()) {
			isMoving = true;
			drainer.reset();
			filler.reset();
			return;
		}

		sendData();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Offset", offset.writeNBT());
		compound.put("Tank", internalTank.writeToNBT(new CompoundTag()));
		super.write(compound, clientPacket);
		if (clientPacket)
			compound.putBoolean("Infinite", infinite);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		offset.readNBT(compound.getCompound("Offset"), clientPacket);
		internalTank.readFromNBT(compound.getCompound("Tank"));
		super.fromTag(state, compound, clientPacket);
		if (clientPacket)
			infinite = compound.getBoolean("Infinite");
	}

	@Override
	public void al_() {
		super.al_();
		capability.invalidate();
	}

	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f;
		if (d.v)
			movementSpeed *= ServerSpeedProvider.get();
		return movementSpeed;
	}

	public float getInterpolatedOffset(float pt) {
		return offset.getValue(pt);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isFluidHandlerCap(cap)
			&& (side == null || HosePulleyBlock.hasPipeTowards(d, e, p(), side)))
			return this.capability.cast();
		return super.getCapability(cap, side);
	}

}
