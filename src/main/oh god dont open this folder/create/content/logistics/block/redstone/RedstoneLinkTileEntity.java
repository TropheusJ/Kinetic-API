package com.simibubi.create.content.logistics.block.redstone;

import static net.minecraft.block.enums.BambooLeaves.w;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RedstoneLinkTileEntity extends SmartTileEntity {

	private boolean receivedSignalChanged;
	private int receivedSignal;
	private int transmittedSignal;
	private LinkBehaviour link;
	private boolean transmitter;

	public RedstoneLinkTileEntity(BellBlockEntity<? extends RedstoneLinkTileEntity> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	protected void createLink() {
		Pair<ValueBoxTransform, ValueBoxTransform> slots =
			ValueBoxTransform.Dual.makeSlots(RedstoneLinkFrequencySlot::new);
		link = transmitter ? LinkBehaviour.transmitter(this, slots, this::getSignal)
				: LinkBehaviour.receiver(this, slots, this::setSignal);
	}

	public int getSignal() {
		return transmittedSignal;
	}

	public void setSignal(int power) {
		if (receivedSignal != power)
			receivedSignalChanged = true;
		receivedSignal = power;
	}

	public void transmit(int strength) {
		transmittedSignal = strength;
		if (link != null)
			link.notifySignalChange();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Transmitter", transmitter);
		compound.putInt("Receive", getReceivedSignal());
		compound.putBoolean("ReceivedChanged", receivedSignalChanged);
		compound.putInt("Transmit", transmittedSignal);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		transmitter = compound.getBoolean("Transmitter");
		super.fromTag(state, compound, clientPacket);
		
		receivedSignal = compound.getInt("Receive");
		receivedSignalChanged = compound.getBoolean("ReceivedChanged");
		if (d == null || d.v || !link.newPosition)
			transmittedSignal = compound.getInt("Transmit");
	}

	@Override
	public void aj_() {
		super.aj_();

		if (isTransmitterBlock() != transmitter) {
			transmitter = isTransmitterBlock();
			LinkBehaviour prevlink = link;
			removeBehaviour(LinkBehaviour.TYPE);
			createLink();
			link.copyItemsFrom(prevlink);
			attachBehaviourLate(link);
		}

		if (transmitter)
			return;
		if (d.v)
			return;
		
		PistonHandler blockState = p();
		if (!AllBlocks.REDSTONE_LINK.has(blockState))
			return;

		if ((getReceivedSignal() > 0) != blockState.c(w)) {
			receivedSignalChanged = true;
			d.a(e, blockState.a(w));
		}
		
		if (receivedSignalChanged) {
			Direction attachedFace = blockState.c(RedstoneLinkBlock.SHAPE).getOpposite();
			BlockPos attachedPos = e.offset(attachedFace);
			d.a(e, d.d_(e).b());
			d.a(attachedPos, d.d_(attachedPos).b());
		}
	}

	protected Boolean isTransmitterBlock() {
		return !p().c(RedstoneLinkBlock.RECEIVER);
	}

	public int getReceivedSignal() {
		return receivedSignal;
	}

}
