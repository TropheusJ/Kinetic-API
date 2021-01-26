package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;
import afj;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.fluids.particle.FluidParticleData;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity.BasinValueBox;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class BasinTileEntity extends SmartTileEntity {

	private boolean areFluidsMoving;
	LerpedFloat ingredientRotationSpeed;
	LerpedFloat ingredientRotation;

	public BasinInventory inputInventory;
	public SmartFluidTankBehaviour inputTank;
	protected SmartInventory outputInventory;
	protected SmartFluidTankBehaviour outputTank;
	private FilteringBehaviour filtering;
	private boolean contentsChanged;

	private Couple<SmartInventory> invs;
	private Couple<SmartFluidTankBehaviour> tanks;

	protected LazyOptional<IItemHandlerModifiable> itemCapability;
	protected LazyOptional<IFluidHandler> fluidCapability;

	List<Direction> disabledSpoutputs;
	Direction preferredSpoutput;
	protected List<ItemCooldownManager> spoutputBuffer;

	public static final int OUTPUT_ANIMATION_TIME = 10;
	List<IntAttached<ItemCooldownManager>> visualizedOutputItems;
	List<IntAttached<FluidStack>> visualizedOutputFluids;

	public BasinTileEntity(BellBlockEntity<? extends BasinTileEntity> type) {
		super(type);
		inputInventory = new BasinInventory(9, this);
		inputInventory.whenContentsChanged($ -> contentsChanged = true);
		outputInventory = new BasinInventory(9, this).forbidInsertion();
		areFluidsMoving = false;
		itemCapability = LazyOptional.of(() -> new CombinedInvWrapper(inputInventory, outputInventory));
		contentsChanged = true;
		ingredientRotation = LerpedFloat.angular()
			.startWithValue(0);
		ingredientRotationSpeed = LerpedFloat.linear()
			.startWithValue(0);

		invs = Couple.create(inputInventory, outputInventory);
		tanks = Couple.create(inputTank, outputTank);
		visualizedOutputItems = Collections.synchronizedList(new ArrayList<>());
		visualizedOutputFluids = Collections.synchronizedList(new ArrayList<>());
		disabledSpoutputs = new ArrayList<>();
		preferredSpoutput = null;
		spoutputBuffer = new ArrayList<>();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		filtering = new FilteringBehaviour(this, new BasinValueBox()).moveText(new EntityHitResult(2, -8, 0))
			.withCallback(newFilter -> contentsChanged = true)
			.forRecipes();
		behaviours.add(filtering);

		inputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 2, 1000, true)
			.whenFluidUpdates(() -> contentsChanged = true);
		outputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 2, 1000, true)
			.whenFluidUpdates(() -> contentsChanged = true)
			.forbidInsertion();
		behaviours.add(inputTank);
		behaviours.add(outputTank);

		fluidCapability = LazyOptional.of(() -> {
			LazyOptional<? extends IFluidHandler> inputCap = inputTank.getCapability();
			LazyOptional<? extends IFluidHandler> outputCap = outputTank.getCapability();
			return new CombinedTankWrapper(inputCap.orElse(null), outputCap.orElse(null));
		});
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));

		preferredSpoutput = null;
		if (compound.contains("PreferredSpoutput"))
			preferredSpoutput = NBTHelper.readEnum(compound, "PreferredSpoutput", Direction.class);
		disabledSpoutputs.clear();
		ListTag disabledList = compound.getList("DisabledSpoutput", NBT.TAG_STRING);
		disabledList.forEach(d -> disabledSpoutputs.add(Direction.valueOf(((StringTag) d).asString())));
		spoutputBuffer = NBTHelper.readItemList(compound.getList("Overflow", NBT.TAG_COMPOUND));

		if (!clientPacket)
			return;

		NBTHelper.iterateCompoundList(compound.getList("VisualizedItems", NBT.TAG_COMPOUND),
			c -> visualizedOutputItems.add(IntAttached.with(OUTPUT_ANIMATION_TIME, ItemCooldownManager.a(c))));
		NBTHelper.iterateCompoundList(compound.getList("VisualizedFluids", NBT.TAG_COMPOUND),
			c -> visualizedOutputFluids
				.add(IntAttached.with(OUTPUT_ANIMATION_TIME, FluidStack.loadFluidStackFromNBT(c))));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputItems", inputInventory.serializeNBT());
		compound.put("OutputItems", outputInventory.serializeNBT());

		if (preferredSpoutput != null)
			NBTHelper.writeEnum(compound, "PreferredSpoutput", preferredSpoutput);
		ListTag disabledList = new ListTag();
		disabledSpoutputs.forEach(d -> disabledList.add(StringTag.of(d.name())));
		compound.put("DisabledSpoutput", disabledList);
		compound.put("Overflow", NBTHelper.writeItemList(spoutputBuffer));

		if (!clientPacket)
			return;

		compound.put("VisualizedItems", NBTHelper.writeCompoundList(visualizedOutputItems, ia -> ia.getValue()
			.serializeNBT()));
		compound.put("VisualizedFluids", NBTHelper.writeCompoundList(visualizedOutputFluids, ia -> ia.getValue()
			.writeToNBT(new CompoundTag())));
		visualizedOutputItems.clear();
		visualizedOutputFluids.clear();
	}

	public void onEmptied() {
		getOperator().ifPresent(te -> te.basinRemoved = true);
	}

	@Override
	public void al_() {
		onEmptied();
		itemCapability.invalidate();
		fluidCapability.invalidate();
		super.al_();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return itemCapability.cast();
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return fluidCapability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void notifyUpdate() {
		super.notifyUpdate();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateSpoutput();
		if (!d.v)
			return;

		BeehiveBlockEntity tileEntity = d.c(e.up(2));
		if (!(tileEntity instanceof MechanicalMixerTileEntity)) {
			setAreFluidsMoving(false);
			return;
		}
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tileEntity;
		setAreFluidsMoving(mixer.running && mixer.runningTicks <= 20);
	}

	public void onWrenched(Direction face) {
		PistonHandler blockState = p();
		Direction currentFacing = blockState.c(BasinBlock.FACING);

		disabledSpoutputs.remove(face);
		if (currentFacing == face) {
			if (preferredSpoutput == face)
				preferredSpoutput = null;
			disabledSpoutputs.add(face);
		} else
			preferredSpoutput = face;

		updateSpoutput();
	}

	private void updateSpoutput() {
		if (d.v)
			return;

		PistonHandler blockState = p();
		Direction currentFacing = blockState.c(BasinBlock.FACING);

		if (currentFacing != Direction.DOWN)
			notifyChangeOfContents();

		Direction newFacing = Direction.DOWN;
		for (Direction test : Iterate.horizontalDirections) {
			boolean canOutputTo = BasinBlock.canOutputTo(d, e, test);
			if (canOutputTo && !disabledSpoutputs.contains(test))
				newFacing = test;
		}

		if (preferredSpoutput != null && BasinBlock.canOutputTo(d, e, preferredSpoutput)
			&& preferredSpoutput != Direction.UP)
			newFacing = preferredSpoutput;

		if (newFacing != currentFacing)
			d.a(e, blockState.a(BasinBlock.FACING, newFacing));
	}

	@Override
	public void aj_() {
		super.aj_();
		if (d.v) {
			createFluidParticles();
			tickVisualizedOutputs();
			ingredientRotationSpeed.tickChaser();
			ingredientRotation.setValue(ingredientRotation.getValue() + ingredientRotationSpeed.getValue());
		}

		if (!spoutputBuffer.isEmpty() && !d.v)
			tryClearingSpoutputOverflow();

		if (!contentsChanged)
			return;
		contentsChanged = false;
		getOperator().ifPresent(te -> te.basinChecker.scheduleUpdate());

		for (Direction offset : Iterate.horizontalDirections) {
			BlockPos toUpdate = e.up()
				.offset(offset);
			PistonHandler stateToUpdate = d.d_(toUpdate);
			if (stateToUpdate.b() instanceof BasinBlock
				&& stateToUpdate.c(BasinBlock.FACING) == offset.getOpposite()) {
				BeehiveBlockEntity te = d.c(toUpdate);
				if (te instanceof BasinTileEntity)
					((BasinTileEntity) te).contentsChanged = true;
			}
		}
	}

	private void tryClearingSpoutputOverflow() {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof BasinBlock))
			return;
		Direction direction = blockState.c(BasinBlock.FACING);
		BeehiveBlockEntity te = d.c(e.down()
			.offset(direction));
		IItemHandler targetInv = te == null ? null
			: te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())
				.orElse(null);
		boolean update = false;

		for (Iterator<ItemCooldownManager> iterator = spoutputBuffer.iterator(); iterator.hasNext();) {
			ItemCooldownManager itemStack = iterator.next();

			if (direction == Direction.DOWN) {
				BeetrootsBlock.a(d, e, itemStack);
				iterator.remove();
				update = true;
				continue;
			}

			if (targetInv == null)
				return;
			if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack, true)
				.a())
				continue;

			update = true;
			ItemHandlerHelper.insertItemStacked(targetInv, itemStack.i(), false);
			iterator.remove();
			visualizedOutputItems.add(IntAttached.withZero(itemStack));
		}

		if (update) {
			notifyChangeOfContents();
			sendData();
		}
	}

	public float getTotalFluidUnits(float partialTicks) {
		int renderedFluids = 0;
		float totalUnits = 0;

		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.getRenderedFluid()
					.isEmpty())
					continue;
				float units = tankSegment.getTotalUnits(partialTicks);
				if (units < 1)
					continue;
				totalUnits += units;
				renderedFluids++;
			}
		}

		if (renderedFluids == 0)
			return 0;
		if (totalUnits < 1)
			return 0;
		return totalUnits;
	}

	private Optional<BasinOperatingTileEntity> getOperator() {
		if (d == null)
			return Optional.empty();
		BeehiveBlockEntity te = d.c(e.up(2));
		if (te instanceof BasinOperatingTileEntity)
			return Optional.of((BasinOperatingTileEntity) te);
		return Optional.empty();
	}

	public FilteringBehaviour getFilter() {
		return filtering;
	}

	public void notifyChangeOfContents() {
		contentsChanged = true;
	}

	public SmartInventory getInputInventory() {
		return inputInventory;
	}

	public SmartInventory getOutputInventory() {
		return outputInventory;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public double i() {
		return 256;
	}

	public boolean acceptOutputs(List<ItemCooldownManager> outputItems, List<FluidStack> outputFluids, boolean simulate) {
		outputInventory.allowInsertion();
		outputTank.allowInsertion();
		boolean acceptOutputsInner = acceptOutputsInner(outputItems, outputFluids, simulate);
		outputInventory.forbidInsertion();
		outputTank.forbidInsertion();
		return acceptOutputsInner;
	}

	private boolean acceptOutputsInner(List<ItemCooldownManager> outputItems, List<FluidStack> outputFluids, boolean simulate) {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof BasinBlock))
			return false;
		Direction direction = blockState.c(BasinBlock.FACING);

		IItemHandler targetInv = null;
		IFluidHandler targetTank = null;

		if (direction == Direction.DOWN) {
			// No output basin, gather locally
			targetInv = outputInventory;
			targetTank = outputTank.getCapability()
				.orElse(null);

		} else {
			// Output basin, try moving items to it
			if (!spoutputBuffer.isEmpty())
				return false;
			BeehiveBlockEntity te = d.c(e.down()
				.offset(direction));
			if (te == null)
				return false;
			targetInv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())
				.orElse(null);
			targetTank = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())
				.orElse(null);
		}

		if (targetInv == null && !outputItems.isEmpty())
			return false;
		for (ItemCooldownManager itemStack : outputItems) {
			// Catalyst items are never consumed
			if (itemStack.hasContainerItem() && itemStack.getContainerItem()
				.a(itemStack)) 
				continue;

			if (simulate || direction == Direction.DOWN) {
				if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack.i(), simulate)
					.a())
					return false;
			} else
				spoutputBuffer.add(itemStack.i());
		}

		if (outputFluids.isEmpty())
			return true;
		if (targetTank == null)
			return false;

		for (FluidStack fluidStack : outputFluids) {
			FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
			int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
				? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
				: targetTank.fill(fluidStack.copy(), action);
			if (fill != fluidStack.getAmount())
				return false;
			else if (!simulate)
				visualizedOutputFluids.add(IntAttached.withZero(fluidStack));
		}

		return true;
	}

	public void readOnlyItems(CompoundTag compound) {
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));
	}

	public static HeatLevel getHeatLevelOf(PistonHandler state) {
		if (BlockHelper.hasBlockStateProperty(state, BlazeBurnerBlock.HEAT_LEVEL))
			return state.c(BlazeBurnerBlock.HEAT_LEVEL);
		return AllTags.AllBlockTags.FAN_HEATERS.matches(state) ? HeatLevel.SMOULDERING : HeatLevel.NONE;
	}

	public Couple<SmartFluidTankBehaviour> getTanks() {
		return tanks;
	}

	public Couple<SmartInventory> getInvs() {
		return invs;
	}

	// client things

	private void tickVisualizedOutputs() {
		visualizedOutputFluids.forEach(IntAttached::decrement);
		visualizedOutputItems.forEach(IntAttached::decrement);
		visualizedOutputFluids.removeIf(IntAttached::isOrBelowZero);
		visualizedOutputItems.removeIf(IntAttached::isOrBelowZero);
	}

	private void createFluidParticles() {
		Random r = d.t;

		if (!visualizedOutputFluids.isEmpty())
			createOutputFluidParticles(r);

		if (!areFluidsMoving && r.nextFloat() > 1 / 8f)
			return;

		int segments = 0;
		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks())
				if (!tankSegment.isEmpty(0))
					segments++;
		}
		if (segments < 2)
			return;

		float totalUnits = getTotalFluidUnits(0);
		if (totalUnits == 0)
			return;
		float fluidLevel = afj.a(totalUnits / 2000, 0, 1);
		float rim = 2 / 16f;
		float space = 12 / 16f;
		float surface = e.getY() + rim + space * fluidLevel + 1 / 32f;

		if (areFluidsMoving) {
			createMovingFluidParticles(surface, segments);
			return;
		}

		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				float x = e.getX() + rim + space * r.nextFloat();
				float z = e.getZ() + rim + space * r.nextFloat();
				d.b(
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()), x,
					surface, z, 0, 0, 0);
			}
		}
	}

	private void createOutputFluidParticles(Random r) {
		PistonHandler blockState = p();
		if (!(blockState.b() instanceof BasinBlock))
			return;
		Direction direction = blockState.c(BasinBlock.FACING);
		if (direction == Direction.DOWN)
			return;
		EntityHitResult directionVec = EntityHitResult.b(direction.getVector());
		EntityHitResult outVec = VecHelper.getCenterOf(e)
			.e(directionVec.a(.65)
				.a(0, 1 / 4f, 0));
		EntityHitResult outMotion = directionVec.a(1 / 16f)
			.b(0, -1 / 16f, 0);

		for (int i = 0; i < 3; i++) {
			visualizedOutputFluids.forEach(ia -> {
				FluidStack fluidStack = ia.getValue();
				ParticleEffect fluidParticle = FluidFX.getFluidParticle(fluidStack);
				EntityHitResult m = VecHelper.offsetRandomly(outMotion, r, 1 / 16f);
				d.b(fluidParticle, outVec.entity, outVec.c, outVec.d, m.entity, m.c, m.d);
			});
		}
	}

	private void createMovingFluidParticles(float surface, int segments) {
		EntityHitResult pointer = new EntityHitResult(1, 0, 0).a(1 / 16f);
		float interval = 360f / segments;
		EntityHitResult centerOf = VecHelper.getCenterOf(e);
		float intervalOffset = (AnimationTickHolder.ticks * 18) % 360;

		int currentSegment = 0;
		for (SmartFluidTankBehaviour behaviour : getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				float angle = interval * (1 + currentSegment) + intervalOffset;
				EntityHitResult vec = centerOf.e(VecHelper.rotate(pointer, angle, Axis.Y));
				d.b(
					new FluidParticleData(AllParticleTypes.BASIN_FLUID.get(), tankSegment.getRenderedFluid()),
					vec.getX(), surface, vec.getZ(), 1, 0, 0);
				currentSegment++;
			}
		}
	}

	public boolean areFluidsMoving() {
		return areFluidsMoving;
	}

	public boolean setAreFluidsMoving(boolean areFluidsMoving) {
		this.areFluidsMoving = areFluidsMoving;
		ingredientRotationSpeed.chase(areFluidsMoving ? 20 : 0, .1f, Chaser.EXP);
		return areFluidsMoving;
	}

	class BasinValueBox extends ValueBoxTransform.Sided {

		@Override
		protected EntityHitResult getSouthLocation() {
			return VecHelper.voxelSpace(8, 12, 15.75);
		}

		@Override
		protected boolean isSideActive(PistonHandler state, Direction direction) {
			return direction.getAxis()
				.isHorizontal();
		}

	}
}
