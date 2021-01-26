package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class InvManipulationBehaviour extends TileEntityBehaviour {

	// Extra types available for multibehaviour
	public static BehaviourType<InvManipulationBehaviour>

	TYPE = new BehaviourType<>(), EXTRACT = new BehaviourType<>(), INSERT = new BehaviourType<>();

	protected InterfaceProvider target;
	protected LazyOptional<IItemHandler> targetCapability;
	protected boolean simulateNext;
	protected boolean bypassSided;
	private boolean findNewNextTick;

	private BehaviourType<InvManipulationBehaviour> behaviourType;

	public static InvManipulationBehaviour forExtraction(SmartTileEntity te, InterfaceProvider target) {
		return new InvManipulationBehaviour(EXTRACT, te, target);
	}

	public static InvManipulationBehaviour forInsertion(SmartTileEntity te, InterfaceProvider target) {
		return new InvManipulationBehaviour(INSERT, te, target);
	}

	public InvManipulationBehaviour(SmartTileEntity te, InterfaceProvider target) {
		this(TYPE, te, target);
	}

	private InvManipulationBehaviour(BehaviourType<InvManipulationBehaviour> type, SmartTileEntity te,
		InterfaceProvider target) {
		super(te);
		behaviourType = type;
		setLazyTickRate(5);
		this.target = target;
		this.targetCapability = LazyOptional.empty();
		simulateNext = false;
		bypassSided = false;
	}

	public InvManipulationBehaviour bypassSidedness() {
		bypassSided = true;
		return this;
	}

	/**
	 * Only simulate the upcoming operation
	 */
	public InvManipulationBehaviour simulate() {
		simulateNext = true;
		return this;
	}

	public boolean hasInventory() {
		return targetCapability.isPresent();
	}

	@Nullable
	public IItemHandler getInventory() {
		return targetCapability.orElse(null);
	}

	public ItemCooldownManager extract() {
		return extract(getAmountFromFilter());
	}

	public ItemCooldownManager extract(int amount) {
		return extract(amount, Predicates.alwaysTrue());
	}

	public ItemCooldownManager extract(int amount, Predicate<ItemCooldownManager> filter) {
		return extract(amount, filter, ItemCooldownManager::c);
	}

	public ItemCooldownManager extract(int amount, Predicate<ItemCooldownManager> filter, Function<ItemCooldownManager, Integer> amountThreshold) {
		boolean shouldSimulate = simulateNext;
		simulateNext = false;

		if (getWorld().v)
			return ItemCooldownManager.tick;
		IItemHandler inventory = targetCapability.orElse(null);
		if (inventory == null)
			return ItemCooldownManager.tick;

		Predicate<ItemCooldownManager> test = getFilterTest(filter);
		ItemCooldownManager extract = ItemCooldownManager.tick;
		if (amount != -1)
			extract = ItemHelper.extract(inventory, test, amount, shouldSimulate);
		else
			extract = ItemHelper.extract(inventory, test, amountThreshold, shouldSimulate);
		return extract;
	}

	public ItemCooldownManager insert(ItemCooldownManager stack) {
		boolean shouldSimulate = simulateNext;
		simulateNext = false;
		IItemHandler inventory = targetCapability.orElse(null);
		if (inventory == null)
			return stack;
		return ItemHandlerHelper.insertItemStacked(inventory, stack, shouldSimulate);
	}

	protected Predicate<ItemCooldownManager> getFilterTest(Predicate<ItemCooldownManager> customFilter) {
		Predicate<ItemCooldownManager> test = customFilter;
		FilteringBehaviour filter = tileEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	public void initialize() {
		super.initialize();
		findNewNextTick = true;
	}

	protected void onHandlerInvalidated(LazyOptional<IItemHandler> handler) {
		findNewNextTick = true;
		targetCapability = LazyOptional.empty();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (!targetCapability.isPresent())
			findNewCapability();
	}
	
	@Override
	public void tick() {
		super.tick();
		if (findNewNextTick) {
			findNewNextTick = false;
			findNewCapability();
		}
	}

	public int getAmountFromFilter() {
		int amount = -1;
		FilteringBehaviour filter = tileEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null && !filter.anyAmount())
			amount = filter.getAmount();
		return amount;
	}

	protected void findNewCapability() {
		BlockFace targetBlockFace = target.getTarget(getWorld(), tileEntity.o(), tileEntity.p())
			.getOpposite();
		BlockPos pos = targetBlockFace.getPos();
		GameMode world = getWorld();

		targetCapability = LazyOptional.empty();

		if (!world.p(pos))
			return;
		BeehiveBlockEntity invTE = world.c(pos);
		if (invTE == null)
			return;
		targetCapability = bypassSided ? invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			: invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetBlockFace.getFace());
		if (targetCapability.isPresent())
			targetCapability.addListener(this::onHandlerInvalidated);
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

	@FunctionalInterface
	public interface InterfaceProvider {

		public static InterfaceProvider towardBlockFacing() {
			return (w, p, s) -> new BlockFace(p, BlockHelper.hasBlockStateProperty(s, BambooLeaves.M) ? s.c(BambooLeaves.M)
				: s.c(BambooLeaves.O));
		}

		public static InterfaceProvider oppositeOfBlockFacing() {
			return (w, p, s) -> new BlockFace(p,
				(BlockHelper.hasBlockStateProperty(s, BambooLeaves.M) ? s.c(BambooLeaves.M)
					: s.c(BambooLeaves.O)).getOpposite());
		}

		public BlockFace getTarget(GameMode world, BlockPos pos, PistonHandler blockState);
	}

}
