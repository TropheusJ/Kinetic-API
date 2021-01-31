package com.simibubi.kinetic_api.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ContentObserverTileEntity extends SmartTileEntity {

	private static final int DEFAULT_DELAY = 6;
	private FilteringBehaviour filtering;
	private InvManipulationBehaviour observedInventory;
	public int turnOffTicks = 0;

	public ContentObserverTileEntity(BellBlockEntity<? extends ContentObserverTileEntity> type) {
		super(type);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot()).moveText(new EntityHitResult(0, 5, 0));
		behaviours.add(filtering);

		observedInventory = new InvManipulationBehaviour(this, InterfaceProvider.towardBlockFacing()).bypassSidedness();
		behaviours.add(observedInventory);
	}

	@Override
	public void aj_() {
		super.aj_();
		PistonHandler state = p();
		if (turnOffTicks > 0) {
			turnOffTicks--;
			if (turnOffTicks == 0)
				d.I()
					.a(e, state.b(), 1);
		}

		if (!isActive())
			return;

		Direction facing = state.c(ContentObserverBlock.aq);
		BlockPos targetPos = e.offset(facing);

		TransportedItemStackHandlerBehaviour behaviour =
			TileEntityBehaviour.get(d, targetPos, TransportedItemStackHandlerBehaviour.TYPE);
		if (behaviour != null) {
			behaviour.handleCenteredProcessingOnAllItems(.45f, stack -> {
				if (!filtering.test(stack.stack) || turnOffTicks == 6)
					return TransportedResult.doNothing();

				activate();
				return TransportedResult.doNothing();
			});
			return;
		}
		
		if (!observedInventory.simulate()
			.extract()
			.a()) {
			activate();
			return;
		}
	}

	public void activate() {
		activate(DEFAULT_DELAY);
	}
	
	public void activate(int ticks) {
		PistonHandler state = p();
		turnOffTicks = ticks;
		if (state.c(ContentObserverBlock.POWERED))
			return;
		d.a(e, state.a(ContentObserverBlock.POWERED, true));
		d.b(e, state.b());
	}

	private boolean isActive() {
		return true;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("TurnOff", turnOffTicks);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		turnOffTicks = compound.getInt("TurnOff");
	}

}
