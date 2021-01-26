package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour.Mode;

public class LinkBehaviour extends TileEntityBehaviour {

	public static BehaviourType<LinkBehaviour> TYPE = new BehaviourType<>();

	enum Mode {
		TRANSMIT, RECEIVE
	}

	Frequency frequencyFirst;
	Frequency frequencyLast;
	ValueBoxTransform firstSlot;
	ValueBoxTransform secondSlot;
	EntityHitResult textShift;

	public boolean newPosition;
	private Mode mode;
	private IntSupplier transmission;
	private IntConsumer signalCallback;

	protected LinkBehaviour(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots) {
		super(te);
		frequencyFirst = Frequency.EMPTY;
		frequencyLast = Frequency.EMPTY;
		firstSlot = slots.getLeft();
		secondSlot = slots.getRight();
		textShift = EntityHitResult.a;
		newPosition = true;
	}

	public static LinkBehaviour receiver(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots,
		IntConsumer signalCallback) {
		LinkBehaviour behaviour = new LinkBehaviour(te, slots);
		behaviour.signalCallback = signalCallback;
		behaviour.mode = Mode.RECEIVE;
		return behaviour;
	}

	public static LinkBehaviour transmitter(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots,
		IntSupplier transmission) {
		LinkBehaviour behaviour = new LinkBehaviour(te, slots);
		behaviour.transmission = transmission;
		behaviour.mode = Mode.TRANSMIT;
		return behaviour;
	}

	public LinkBehaviour moveText(EntityHitResult shift) {
		textShift = shift;
		return this;
	}

	public void copyItemsFrom(LinkBehaviour behaviour) {
		if (behaviour == null)
			return;
		frequencyFirst = behaviour.frequencyFirst;
		frequencyLast = behaviour.frequencyLast;
	}

	public boolean isListening() {
		return mode == Mode.RECEIVE;
	}

	public int getTransmittedStrength() {
		return mode == Mode.TRANSMIT ? transmission.getAsInt() : 0;
	}

	public void updateReceiver(int networkPower) {
		if (!newPosition)
			return;
		signalCallback.accept(networkPower);
	}

	public void notifySignalChange() {
		Create.redstoneLinkNetworkHandler.updateNetworkOf(this);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (tileEntity.v().v)
			return;
		getHandler().addToNetwork(this);
		newPosition = true;
	}

	public Pair<Frequency, Frequency> getNetworkKey() {
		return Pair.of(frequencyFirst, frequencyLast);
	}

	@Override
	public void remove() {
		super.remove();
		if (tileEntity.v().v)
			return;
		getHandler().removeFromNetwork(this);
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.put("FrequencyFirst", frequencyFirst.getStack()
			.b(new CompoundTag()));
		nbt.put("FrequencyLast", frequencyLast.getStack()
			.b(new CompoundTag()));
		nbt.putLong("LastKnownPosition", tileEntity.o()
			.asLong());
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		long positionInTag = tileEntity.o()
			.asLong();
		long positionKey = nbt.getLong("LastKnownPosition");
		newPosition = positionInTag != positionKey;

		super.read(nbt, clientPacket);
		frequencyFirst = Frequency.of(ItemCooldownManager.a(nbt.getCompound("FrequencyFirst")));
		frequencyLast = Frequency.of(ItemCooldownManager.a(nbt.getCompound("FrequencyLast")));
	}

	public void setFrequency(boolean first, ItemCooldownManager stack) {
		stack = stack.i();
		stack.e(1);
		ItemCooldownManager toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
		boolean changed =
			!ItemCooldownManager.c(stack, toCompare) || !ItemCooldownManager.a(stack, toCompare);

		if (changed)
			getHandler().removeFromNetwork(this);

		if (first)
			frequencyFirst = Frequency.of(stack);
		else
			frequencyLast = Frequency.of(stack);

		if (!changed)
			return;

		tileEntity.sendData();
		getHandler().addToNetwork(this);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	private RedstoneLinkNetworkHandler getHandler() {
		return Create.redstoneLinkNetworkHandler;
	}

	public static class SlotPositioning {
		Function<PistonHandler, Pair<EntityHitResult, EntityHitResult>> offsets;
		Function<PistonHandler, EntityHitResult> rotation;
		float scale;

		public SlotPositioning(Function<PistonHandler, Pair<EntityHitResult, EntityHitResult>> offsetsForState,
			Function<PistonHandler, EntityHitResult> rotationForState) {
			offsets = offsetsForState;
			rotation = rotationForState;
			scale = 1;
		}

		public SlotPositioning scale(float scale) {
			this.scale = scale;
			return this;
		}

	}

	public boolean testHit(Boolean first, EntityHitResult hit) {
		PistonHandler state = tileEntity.p();
		EntityHitResult localHit = hit.d(EntityHitResult.b(tileEntity.o()));
		return (first ? firstSlot : secondSlot).testHit(state, localHit);
	}

}
