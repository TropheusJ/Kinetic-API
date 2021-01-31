package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.simibubi.kinetic_api.content.logistics.item.filter.FilterItem;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class FilteringBehaviour extends TileEntityBehaviour {

	public static BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	boolean showCount;
	EntityHitResult textShift;

	private ItemCooldownManager filter;
	public int count;
	private Consumer<ItemCooldownManager> callback;
	private Supplier<Boolean> isActive;
	private Supplier<Boolean> showCountPredicate;

	int scrollableValue;
	int ticksUntilScrollPacket;
	boolean forceClientState;
	boolean recipeFilter;
	boolean fluidFilter;

	public FilteringBehaviour(SmartTileEntity te, ValueBoxTransform slot) {
		super(te);
		filter = ItemCooldownManager.tick;
		slotPositioning = slot;
		showCount = false;
		callback = stack -> {
		};
		isActive = () -> true;
		textShift = EntityHitResult.a;
		count = 0;
		ticksUntilScrollPacket = -1;
		showCountPredicate = () -> showCount;
		recipeFilter = false;
		fluidFilter = false;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.put("Filter", getFilter().serializeNBT());
		nbt.putInt("FilterAmount", count);

		if (clientPacket && forceClientState) {
			nbt.putBoolean("ForceScrollable", true);
			forceClientState = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		filter = ItemCooldownManager.a(nbt.getCompound("Filter"));
		count = nbt.getInt("FilterAmount");
		if (nbt.contains("ForceScrollable")) {
			scrollableValue = count;
			ticksUntilScrollPacket = -1;
		}
		super.read(nbt, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		if (!getWorld().v)
			return;
		if (ticksUntilScrollPacket == -1)
			return;
		if (ticksUntilScrollPacket > 0) {
			ticksUntilScrollPacket--;
			return;
		}

		AllPackets.channel.sendToServer(new FilteringCountUpdatePacket(getPos(), scrollableValue));
		ticksUntilScrollPacket = -1;
	}

	public FilteringBehaviour withCallback(Consumer<ItemCooldownManager> filterCallback) {
		callback = filterCallback;
		return this;
	}

	public FilteringBehaviour forRecipes() {
		recipeFilter = true;
		return this;
	}

	public FilteringBehaviour forFluids() {
		fluidFilter = true;
		return this;
	}

	public FilteringBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
		isActive = condition;
		return this;
	}

	public FilteringBehaviour showCountWhen(Supplier<Boolean> condition) {
		showCountPredicate = condition;
		return this;
	}

	public FilteringBehaviour showCount() {
		showCount = true;
		return this;
	}

	public FilteringBehaviour moveText(EntityHitResult shift) {
		textShift = shift;
		return this;
	}

	@Override
	public void initialize() {
		super.initialize();
		scrollableValue = count;
	}

	public void setFilter(Direction face, ItemCooldownManager stack) {
		setFilter(stack);
	}

	public void setFilter(ItemCooldownManager stack) {
		boolean confirm = ItemHandlerHelper.canItemStacksStack(stack, filter);
		filter = stack.i();
		callback.accept(filter);
		count = !confirm ? 0
			: (filter.b() instanceof FilterItem) ? 0 : Math.min(stack.E(), stack.c());
		forceClientState = true;

		tileEntity.X_();
		tileEntity.sendData();
	}

	@Override
	public void destroy() {
		if (filter.b() instanceof FilterItem) {
			EntityHitResult pos = VecHelper.getCenterOf(getPos());
			GameMode world = getWorld();
			world.c(new PaintingEntity(world, pos.entity, pos.c, pos.d, filter.i()));
		}

		super.destroy();
	}

	public ItemCooldownManager getFilter(Direction side) {
		return getFilter();
	}

	public ItemCooldownManager getFilter() {
		return filter.i();
	}

	public boolean isCountVisible() {
		return showCountPredicate.get();
	}

	public boolean test(ItemCooldownManager stack) {
		return !isActive() || filter.a() || FilterItem.test(tileEntity.v(), stack, filter);
	}

	public boolean test(FluidStack stack) {
		return !isActive() || filter.a() || FilterItem.test(tileEntity.v(), stack, filter);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean testHit(EntityHitResult hit) {
		PistonHandler state = tileEntity.p();
		EntityHitResult localHit = hit.d(EntityHitResult.b(tileEntity.o()));
		return slotPositioning.testHit(state, localHit);
	}

	public int getAmount() {
		return count;
	}

	public boolean anyAmount() {
		return count == 0;
	}

	public boolean isActive() {
		return isActive.get();
	}

}
