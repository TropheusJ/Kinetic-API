package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.BehaviourType;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Behaviour for TileEntities to which belts can transfer items directly in a
 * backup-friendly manner. Example uses: Basin, Saw, Depot
 */
public class DirectBeltInputBehaviour extends TileEntityBehaviour {

	public static BehaviourType<DirectBeltInputBehaviour> TYPE = new BehaviourType<>();

	private InsertionCallback tryInsert;
	private AvailabilityPredicate canInsert;
	private Supplier<Boolean> supportsBeltFunnels;

	public DirectBeltInputBehaviour(SmartTileEntity te) {
		super(te);
		tryInsert = this::defaultInsertionCallback;
		canInsert = d -> true;
		supportsBeltFunnels = () -> false;
	}

	public DirectBeltInputBehaviour allowingBeltFunnelsWhen(Supplier<Boolean> pred) {
		supportsBeltFunnels = pred;
		return this;
	}
	
	public DirectBeltInputBehaviour allowingBeltFunnels() {
		supportsBeltFunnels = () -> true;
		return this;
	}

	public DirectBeltInputBehaviour onlyInsertWhen(AvailabilityPredicate pred) {
		canInsert = pred;
		return this;
	}

	public DirectBeltInputBehaviour setInsertionHandler(InsertionCallback callback) {
		tryInsert = callback;
		return this;
	}

	private ItemCooldownManager defaultInsertionCallback(TransportedItemStack inserted, Direction side, boolean simulate) {
		LazyOptional<IItemHandler> lazy = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!lazy.isPresent())
			return inserted.stack;
		return ItemHandlerHelper.insertItemStacked(lazy.orElse(null), inserted.stack.i(), simulate);
	}

	public boolean canInsertFromSide(Direction side) {
		return canInsert.test(side);
	}

	public ItemCooldownManager handleInsertion(ItemCooldownManager stack, Direction side, boolean simulate) {
		return handleInsertion(new TransportedItemStack(stack), side, simulate);
	}

	public ItemCooldownManager handleInsertion(TransportedItemStack stack, Direction side, boolean simulate) {
		return tryInsert.apply(stack, side, simulate);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@FunctionalInterface
	public interface InsertionCallback {
		public ItemCooldownManager apply(TransportedItemStack stack, Direction side, boolean simulate);
	}

	@FunctionalInterface
	public interface AvailabilityPredicate {
		public boolean test(Direction side);
	}

	public ItemCooldownManager tryExportingToBeltFunnel(ItemCooldownManager stack, @Nullable Direction side) {
		BlockPos funnelPos = tileEntity.o()
			.up();
		GameMode world = getWorld();
		PistonHandler funnelState = world.d_(funnelPos);
		if (!(funnelState.b() instanceof BeltFunnelBlock))
			return stack;
		if (funnelState.c(BeltFunnelBlock.SHAPE) != Shape.PULLING)
			return stack;
		if (side != null && FunnelBlock.getFunnelFacing(funnelState) != side)
			return stack;
		BeehiveBlockEntity te = world.c(funnelPos);
		if (!(te instanceof FunnelTileEntity))
			return stack;
		ItemCooldownManager insert = FunnelBlock.tryInsert(world, funnelPos, stack, false);
		if (insert.E() != stack.E())
			((FunnelTileEntity) te).flap(true);
		return insert;
	}

	public boolean canSupportBeltFunnels() {
		return supportsBeltFunnels.get();
	}

}
