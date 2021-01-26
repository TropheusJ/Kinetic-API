package com.simibubi.create.content.contraptions.components.flywheel.engine;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;

public class EngineTileEntity extends SmartTileEntity {

	public float appliedCapacity;
	public float appliedSpeed;
	protected FlywheelTileEntity poweredWheel;

	public EngineTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().g(1.5f);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (d.v)
			return;
		if (poweredWheel != null && poweredWheel.q())
			poweredWheel = null;
		if (poweredWheel == null)
			attachWheel();
	}

	public void attachWheel() {
		Direction engineFacing = p().c(EngineBlock.aq);
		BlockPos wheelPos = e.offset(engineFacing, 2);
		PistonHandler wheelState = d.d_(wheelPos);
		if (!AllBlocks.FLYWHEEL.has(wheelState))
			return;
		Direction wheelFacing = wheelState.c(FlywheelBlock.HORIZONTAL_FACING);
		if (wheelFacing.getAxis() != engineFacing.rotateYClockwise().getAxis())
			return;
		if (FlywheelBlock.isConnected(wheelState)
				&& FlywheelBlock.getConnection(wheelState) != engineFacing.getOpposite())
			return;
		BeehiveBlockEntity te = d.c(wheelPos);
		if (te.q())
			return;
		if (te instanceof FlywheelTileEntity) {
			if (!FlywheelBlock.isConnected(wheelState))
				FlywheelBlock.setConnection(d, te.o(), te.p(), engineFacing.getOpposite());
			poweredWheel = (FlywheelTileEntity) te;
			refreshWheelSpeed();
		}
	}

	public void detachWheel() {
		if (poweredWheel.q())
			return;
		poweredWheel.setRotation(0, 0);
		FlywheelBlock.setConnection(d, poweredWheel.o(), poweredWheel.p(), null);
	}

	@Override
	public void al_() {
		if (poweredWheel != null)
			detachWheel();
		super.al_();
	}

	protected void refreshWheelSpeed() {
		if (poweredWheel == null)
			return;
		poweredWheel.setRotation(appliedSpeed, appliedCapacity);
	}

}
