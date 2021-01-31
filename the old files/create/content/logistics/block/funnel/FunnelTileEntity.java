package com.simibubi.kinetic_api.content.logistics.block.funnel;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.kinetic_api.content.contraptions.goggles.IHaveHoveringInformation;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelTileEntity.Mode;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.kinetic_api.foundation.item.TooltipHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelTileEntity extends SmartTileEntity implements IHaveHoveringInformation {

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour invManipulation;
	private InvManipulationBehaviour autoExtractor;
	private int extractionCooldown;

	int sendFlap;
	InterpolatedChasingValue flap;

	static enum Mode {
		INVALID, PAUSED, COLLECT, PUSHING_TO_BELT, TAKING_FROM_BELT, HOPPER
	}

	public FunnelTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		extractionCooldown = 0;
		flap = new InterpolatedChasingValue().start(.25f)
			.target(0)
			.withSpeed(.05f);
	}

	public Mode determineCurrentMode() {
		PistonHandler state = p();
		if (!FunnelBlock.isFunnel(state))
			return Mode.INVALID;
		if (state.d(BambooLeaves.w).orElse(false))
			return Mode.PAUSED;
		if (FunnelBlock.getFunnelFacing(state) == Direction.UP && autoExtractor.hasInventory())
			return Mode.HOPPER;
		if (state.b() instanceof BeltFunnelBlock) {
			Shape shape = state.c(BeltFunnelBlock.SHAPE);
			if (shape == Shape.PULLING)
				return Mode.TAKING_FROM_BELT;
			if (shape == Shape.PUSHING)
				return Mode.PUSHING_TO_BELT;

			BeltTileEntity belt = BeltHelper.getSegmentTE(d, e.down());
			if (belt != null)
				return belt.getMovementFacing() == state.c(BeltFunnelBlock.aq) ? Mode.PUSHING_TO_BELT
					: Mode.TAKING_FROM_BELT;
		}
		return Mode.COLLECT;
	}

	@Override
	public void aj_() {
		super.aj_();
		flap.tick();
		Mode mode = determineCurrentMode();
		if (d.v)
			return;

		// Redstone resets the extraction cooldown
		if (mode == Mode.PAUSED)
			extractionCooldown = 0;
		if (mode == Mode.TAKING_FROM_BELT)
			return;

		if (extractionCooldown > 0) {
			extractionCooldown--;
			return;
		}

		if (mode == Mode.PUSHING_TO_BELT)
			activateExtractingBeltFunnel();
		if (mode == Mode.HOPPER)
			activateHopper();
	}

	private void activateHopper() {
		if (!invManipulation.hasInventory())
			return;
		int amountToExtract = autoExtractor.getAmountFromFilter();
		if (!filtering.isActive())
			amountToExtract = 1;

		Predicate<ItemCooldownManager> filter = s -> !filtering.isActive() || filtering.test(s);
		Function<ItemCooldownManager, Integer> amountThreshold = s -> {
			int maxStackSize = s.c();
			return maxStackSize - invManipulation.simulate()
				.insert(ItemHandlerHelper.copyStackWithSize(s, maxStackSize))
				.E();
		};

		if (amountToExtract != -1 && !invManipulation.simulate()
			.insert(autoExtractor.simulate()
				.extract(amountToExtract, filter))
			.a())
			return;

		ItemCooldownManager stack = autoExtractor.extract(amountToExtract, filter, amountThreshold);
		if (stack.a())
			return;

		onTransfer(stack);
		invManipulation.insert(stack);
		startCooldown();
	}

	private void activateExtractingBeltFunnel() {
		PistonHandler blockState = p();
		Direction facing = blockState.c(BeltFunnelBlock.aq);
		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(d, e.down(), DirectBeltInputBehaviour.TYPE);

		if (inputBehaviour == null)
			return;
		if (!inputBehaviour.canInsertFromSide(facing))
			return;

		int amountToExtract = getAmountToExtract();
		ItemCooldownManager stack = invManipulation.extract(amountToExtract, s -> inputBehaviour.handleInsertion(s, facing, true)
			.a());
		if (stack.a())
			return;
		flap(false);
		onTransfer(stack);
		inputBehaviour.handleInsertion(stack, facing, false);
		startCooldown();
	}

	public int getAmountToExtract() {
		if (!supportsAmountOnFilter())
			return -1;
		int amountToExtract = invManipulation.getAmountFromFilter();
		if (!filtering.isActive())
			amountToExtract = 1;
		return amountToExtract;
	}

	private int startCooldown() {
		return extractionCooldown = AllConfigs.SERVER.logistics.defaultExtractionTimer.get();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		invManipulation = new InvManipulationBehaviour(this, InterfaceProvider.oppositeOfBlockFacing());
		behaviours.add(invManipulation);
		autoExtractor = InvManipulationBehaviour.forExtraction(this, InterfaceProvider.towardBlockFacing());
		behaviours.add(autoExtractor);

		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning());
		filtering.showCountWhen(this::supportsAmountOnFilter);
		filtering.onlyActiveWhen(this::supportsFiltering);
		behaviours.add(filtering);

		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput)
			.setInsertionHandler(this::handleDirectBeltInput));
	}

	private boolean supportsAmountOnFilter() {
		PistonHandler blockState = p();
		boolean beltFunnelsupportsAmount = false;
		if (blockState.b() instanceof BeltFunnelBlock) {
			Shape shape = blockState.c(BeltFunnelBlock.SHAPE);
			if (shape == Shape.PUSHING)
				beltFunnelsupportsAmount = true;
			else
				beltFunnelsupportsAmount = BeltHelper.getSegmentTE(d, e.down()) != null;
		}
		boolean hopper = FunnelBlock.getFunnelFacing(blockState) == Direction.UP && !d.d_(e.up())
			.c()
			.e();
		return beltFunnelsupportsAmount || hopper;
	}

	private boolean supportsDirectBeltInput(Direction side) {
		PistonHandler blockState = p();
		if (blockState == null)
			return false;
		if (!(blockState.b() instanceof FunnelBlock))
			return false;
		Direction direction = blockState.c(FunnelBlock.SHAPE);
		return direction == Direction.UP || direction == side.getOpposite();
	}

	private boolean supportsFiltering() {
		PistonHandler blockState = p();
		return AllBlocks.BRASS_BELT_FUNNEL.has(blockState) || AllBlocks.BRASS_FUNNEL.has(blockState);
	}

	private ItemCooldownManager handleDirectBeltInput(TransportedItemStack stack, Direction side, boolean simulate) {
		ItemCooldownManager inserted = stack.stack;
		if (!filtering.test(inserted))
			return inserted;
		if (determineCurrentMode() == Mode.PAUSED)
			return inserted;
		if (simulate)
			invManipulation.simulate();
		if (!simulate)
			onTransfer(inserted);
		return invManipulation.insert(inserted);
	}

	public void flap(boolean inward) {
		sendFlap = inward ? 1 : -1;
		sendData();
	}

	public boolean hasFlap() {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof BeltFunnelBlock))
			return false;
		return true;
	}

	public float getFlapOffset() {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof BeltFunnelBlock))
			return 0;
		switch (blockState.c(BeltFunnelBlock.SHAPE)) {
		default:
		case RETRACTED:
			return 0;
		case EXTENDED:
			return 8 / 16f;
		case PULLING:
		case PUSHING:
			return -2 / 16f;
		}
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("TransferCooldown", extractionCooldown);
		if (clientPacket && sendFlap != 0) {
			compound.putInt("Flap", sendFlap);
			sendFlap = 0;
		}
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		extractionCooldown = compound.getInt("TransferCooldown");
		if (clientPacket && compound.contains("Flap")) {
			int direction = compound.getInt("Flap");
			flap.set(direction);
		}
	}

	@Override
	public double i() {
		return hasFlap() ? super.i() : 64;
	}

	public void onTransfer(ItemCooldownManager stack) {
		AllBlocks.CONTENT_OBSERVER.get()
			.onFunnelTransfer(d, e, stack);
	}

	@Override
	// Hint players not to use funnels like 0.2 transposers
	public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		if (isPlayerSneaking)
			return false;
		PistonHandler state = p();
		if (!(state.b() instanceof FunnelBlock))
			return false;
		Direction funnelFacing = FunnelBlock.getFunnelFacing(state);

		if (d.d_(e.offset(funnelFacing.getOpposite()))
			.c()
			.e())
			return false;

		BlockPos inputPos = e.offset(funnelFacing);
		BeehiveBlockEntity tileEntity = d.c(inputPos);
		if (tileEntity == null)
			return false;
		if (tileEntity instanceof BeltTileEntity)
			return false;
		if (tileEntity instanceof SawTileEntity)
			return false;
		if (tileEntity instanceof ChuteTileEntity)
			return false;

		LazyOptional<IItemHandler> capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!capability.isPresent())
			return false;

		if (funnelFacing == Direction.DOWN) {
			TooltipHelper.addHint(tooltip, "hint.upward_funnel");
			return true;
		}
		if (!funnelFacing.getAxis()
			.isHorizontal())
			return false;

		TooltipHelper.addHint(tooltip, "hint.horizontal_funnel");
		return true;
	}

}
