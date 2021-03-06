package com.tropheus_jay.kinetic_api.foundation.tileEntity;

import com.tropheus_jay.kinetic_api.foundation.behaviour.BehaviourType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
/* todo: replace these
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
*/
public abstract class SmartTileEntity extends SyncedTileEntity implements Tickable {

	private Map<BehaviourType<?>, TileEntityBehaviour> behaviours;
	private boolean initialized;
	private boolean firstNbtRead;
	private int lazyTickRate;
	private int lazyTickCounter;

	public SmartTileEntity(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		behaviours = new HashMap<>();
		initialized = false;
		firstNbtRead = true;
		setLazyTickRate(10);

		ArrayList<TileEntityBehaviour> list = new ArrayList<>();
		addBehaviours(list);
		list.forEach(b -> behaviours.put(b.getType(), b));
	}

	public abstract void addBehaviours(List<TileEntityBehaviour> behaviours);

	/**
	 * Gets called just before reading tile data_unused for behaviours. Register anything
	 * here that depends on your custom te data_unused.
	 */
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void tick() {
		if (!initialized && hasWorld()) {
			initialize();
			initialized = true;
		}

		if (lazyTickCounter-- <= 0) {
			lazyTickCounter = lazyTickRate;
			lazyTick();
		}

		behaviours.values()
			.forEach(TileEntityBehaviour::tick);
	}

	public void initialize() {
		behaviours.values()
			.forEach(TileEntityBehaviour::initialize);
		lazyTick();
	}

	@Override
	public final CompoundTag toTag(CompoundTag compound) {
		write(compound, false);
		return compound;
	}

	@Override
	public final CompoundTag writeToClient(CompoundTag compound) {
		write(compound, true);
		return compound;
	}

	@Override
	public final void readClientUpdate(BlockState state, CompoundTag tag) {
		fromTag(state, tag, true);
	}

	@Override
	public final void fromTag(BlockState state, CompoundTag tag) {
		fromTag(state, tag, false);
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		if (firstNbtRead) {
			firstNbtRead = false;
			ArrayList<TileEntityBehaviour> list = new ArrayList<>();
			addBehavioursDeferred(list);
			list.forEach(b -> behaviours.put(b.getType(), b));
		}
		super.fromTag(state, compound);
		behaviours.values()
			.forEach(tb -> tb.read(compound, clientPacket));
	}

	/**
	 * Hook only these in future subclasses of STE
	 */
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.toTag(compound);
		behaviours.values()
			.forEach(tb -> tb.write(compound, clientPacket));
	}

	@Override
	public void markRemoved() {
		forEachBehaviour(TileEntityBehaviour::remove);
		super.markRemoved();
	}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
	}

	public void lazyTick() {

	}

	protected void forEachBehaviour(Consumer<TileEntityBehaviour> action) {
		behaviours.values()
			.forEach(action);
	}

	protected void attachBehaviourLate(TileEntityBehaviour behaviour) {
		behaviours.put(behaviour.getType(), behaviour);
		behaviour.initialize();
	}

	protected void removeBehaviour(BehaviourType<?> type) {
		TileEntityBehaviour remove = behaviours.remove(type);
		if (remove != null)
			remove.remove();
	}

	@SuppressWarnings("unchecked")
	public <T extends TileEntityBehaviour> T getBehaviour(BehaviourType<T> type) {
		if (behaviours.containsKey(type))
			return (T) behaviours.get(type);
		return null;
	}
	/*todo: item and fluid handling
	protected boolean isItemHandlerCap(Capability<?> cap) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}
	
	protected boolean isFluidHandlerCap(Capability<?> cap) {
		return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}
*/
}
