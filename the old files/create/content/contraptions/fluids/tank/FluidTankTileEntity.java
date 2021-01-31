package com.simibubi.kinetic_api.content.contraptions.fluids.tank;

import static java.lang.Math.abs;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.fluid.SmartFluidTank;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidTankTileEntity extends SmartTileEntity {

	private static final int MAX_SIZE = 3;

	protected LazyOptional<IFluidHandler> fluidCapability;
	protected boolean forceFluidLevelUpdate;
	protected FluidTank tankInventory;
	protected BlockPos controller;
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected boolean window;
	protected int luminosity;
	protected int width;
	protected int height;

	private static final int SYNC_RATE = 8;
	protected int syncCooldown;
	protected boolean queuedSync;

	// For rendering purposes only
	InterpolatedChasingValue fluidLevel;

	public FluidTankTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		tankInventory = createInventory();
		fluidCapability = LazyOptional.of(() -> tankInventory);
		forceFluidLevelUpdate = true;
		updateConnectivity = false;
		window = true;
		height = 1;
		width = 1;
		refreshCapability();
	}

	protected SmartFluidTank createInventory() {
		return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
	}

	protected void updateConnectivity() {
		updateConnectivity = false;
		if (d.v)
			return;
		if (!isController())
			return;
		FluidTankConnectivityHandler.formTanks(this);
	}

	@Override
	public void aj_() {
		super.aj_();
		if (syncCooldown > 0) {
			syncCooldown--;
			if (syncCooldown == 0 && queuedSync)
				sendData();
		}
		
		if (lastKnownPos == null)
			lastKnownPos = o();
		else if (!lastKnownPos.equals(e) && e != null) {
			onPositionChanged();
			return;
		}
		
		if (updateConnectivity)
			updateConnectivity();
		if (fluidLevel != null)
			fluidLevel.tick();
	}

	public boolean isController() {
		return controller == null || controller.equals(e);
	}

	@Override
	public void initialize() {
		super.initialize();
		sendData();
	}

	private void onPositionChanged() {
		removeController(true);
		lastKnownPos = e;
	}

	protected void onFluidStackChanged(FluidStack newFluidStack) {
		if (!n())
			return;

		FluidAttributes attributes = newFluidStack.getFluid()
			.getAttributes();
		int luminosity = (int) (attributes.getLuminosity(newFluidStack) / 1.2f);
		boolean reversed = attributes.isLighterThanAir();
		int maxY = (int) ((getFillState() * height) + 1);

		for (int yOffset = 0; yOffset < height; yOffset++) {
			boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
			int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;

			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = this.e.add(xOffset, yOffset, zOffset);
					FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(d, pos);
					if (tankAt == null)
						continue;
					if (tankAt.luminosity == actualLuminosity)
						continue;
					tankAt.setLuminosity(actualLuminosity);
				}
			}
		}

		if (!d.v) {
			X_();
			sendData();
		}
	}

	protected void setLuminosity(int luminosity) {
		if (d.v)
			return;
		if (this.luminosity == luminosity)
			return;
		this.luminosity = luminosity;
		sendData();
	}

	public FluidTankTileEntity getControllerTE() {
		if (isController())
			return this;
		BeehiveBlockEntity tileEntity = d.c(controller);
		if (tileEntity instanceof FluidTankTileEntity)
			return (FluidTankTileEntity) tileEntity;
		return null;
	}

	public void applyFluidTankSize(int blocks) {
		tankInventory.setCapacity(blocks * getCapacityMultiplier());
		int overflow = tankInventory.getFluidAmount() - tankInventory.getCapacity();
		if (overflow > 0)
			tankInventory.drain(overflow, FluidAction.EXECUTE);
		forceFluidLevelUpdate = true;
	}

	public void removeController(boolean keepFluids) {
		if (d.v)
			return;
		updateConnectivity = true;
		if (!keepFluids)
			applyFluidTankSize(1);
		controller = null;
		width = 1;
		height = 1;
		onFluidStackChanged(tankInventory.getFluid());

		PistonHandler state = p();
		if (FluidTankBlock.isTank(state)) {
			state = state.a(FluidTankBlock.BOTTOM, true);
			state = state.a(FluidTankBlock.TOP, true);
			state = state.a(FluidTankBlock.SHAPE, window ? Shape.WINDOW : Shape.PLAIN);
			v().a(e, state, 22);
		}

		refreshCapability();
		X_();
		sendData();
	}

	public void toggleWindows() {
		FluidTankTileEntity te = getControllerTE();
		if (te == null)
			return;
		te.setWindows(!te.window);
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		sendData();
	}

	@Override
	public void sendData() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		super.sendData();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
	}

	public void setWindows(boolean window) {
		this.window = window;
		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {

					BlockPos pos = this.e.add(xOffset, yOffset, zOffset);
					PistonHandler blockState = d.d_(pos);
					if (!FluidTankBlock.isTank(blockState))
						continue;

					Shape shape = Shape.PLAIN;
					if (window) {
						// SIZE 1: Every tank has a window
						if (width == 1)
							shape = Shape.WINDOW;
						// SIZE 2: Every tank has a corner window
						if (width == 2)
							shape = xOffset == 0 ? zOffset == 0 ? Shape.WINDOW_NW : Shape.WINDOW_SW
								: zOffset == 0 ? Shape.WINDOW_NE : Shape.WINDOW_SE;
						// SIZE 3: Tanks in the center have a window
						if (width == 3 && abs(abs(xOffset) - abs(zOffset)) == 1)
							shape = Shape.WINDOW;
					}

					d.a(pos, blockState.a(FluidTankBlock.SHAPE, shape), 22);
					d.G()
						.l()
						.checkBlock(pos);
				}
			}
		}
	}

	public void setController(BlockPos controller) {
		if (d.v)
			return;
		if (controller.equals(this.controller))
			return;
		this.controller = controller;
		refreshCapability();
		X_();
		sendData();
	}

	private void refreshCapability() {
		LazyOptional<IFluidHandler> oldCap = fluidCapability;
		fluidCapability = LazyOptional.of(() -> isController() ? tankInventory
			: getControllerTE() != null ? getControllerTE().tankInventory : new FluidTank(0));
		oldCap.invalidate();
	}

	public BlockPos getController() {
		return isController() ? e : controller;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Timer getRenderBoundingBox() {
		if (isController())
			return super.getRenderBoundingBox().b(width - 1, height - 1, width - 1);
		return super.getRenderBoundingBox();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public double i() {
		int dist = 64 + getMaxHeight() * 2;
		return dist * dist;
	}

	@Nullable
	public FluidTankTileEntity getOtherFluidTankTileEntity(Direction direction) {
		BeehiveBlockEntity otherTE = d.c(e.offset(direction));
		if (otherTE instanceof FluidTankTileEntity)
			return (FluidTankTileEntity) otherTE;
		return null;
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		
		BlockPos controllerBefore = controller;
		int prevSize = width;
		int prevHeight = height;
		int prevLum = luminosity;

		updateConnectivity = compound.contains("Uninitialized");
		luminosity = compound.getInt("Luminosity");
		controller = null;
		lastKnownPos = null;

		if (compound.contains("LastKnownPos"))
			lastKnownPos = NbtHelper.toBlockPos(compound.getCompound("LastKnownPos"));
		if (compound.contains("Controller"))
			controller = NbtHelper.toBlockPos(compound.getCompound("Controller"));

		if (isController()) {
			window = compound.getBoolean("Window");
			width = compound.getInt("Size");
			height = compound.getInt("Height");
			tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
			tankInventory.readFromNBT(compound.getCompound("TankContent"));
			if (tankInventory.getSpace() < 0)
				tankInventory.drain(-tankInventory.getSpace(), FluidAction.EXECUTE);
		}

		if (compound.contains("ForceFluidLevel") || fluidLevel == null)
			fluidLevel = new InterpolatedChasingValue().start(getFillState())
				.withSpeed(1 / 2f);

		if (!clientPacket)
			return;

		boolean changeOfController =
			controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
		if (changeOfController || prevSize != width || prevHeight != height) {
			if (n())
				d.a(o(), p(), p(), 16);
			if (isController())
				tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
		}
		if (isController()) {
			float fillState = getFillState();
			if (compound.contains("ForceFluidLevel") || fluidLevel == null)
				fluidLevel = new InterpolatedChasingValue().start(fillState);
			fluidLevel.target(fillState);
		}
		if (luminosity != prevLum && n())
			d.G()
				.l()
				.checkBlock(e);

		if (compound.contains("LazySync"))
			fluidLevel.withSpeed(compound.contains("LazySync") ? 1 / 8f : 1 / 2f);
	}

	public float getFillState() {
		return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (updateConnectivity)
			compound.putBoolean("Uninitialized", true);
		if (lastKnownPos != null)
			compound.put("LastKnownPos", NbtHelper.fromBlockPos(lastKnownPos));
		if (!isController())
			compound.put("Controller", NbtHelper.fromBlockPos(controller));
		if (isController()) {
			compound.putBoolean("Window", window);
			compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));
			compound.putInt("Size", width);
			compound.putInt("Height", height);
		}
		compound.putInt("Luminosity", luminosity);
		super.write(compound, clientPacket);

		if (!clientPacket)
			return;
		if (forceFluidLevelUpdate)
			compound.putBoolean("ForceFluidLevel", true);
		if (queuedSync)
			compound.putBoolean("LazySync", true);
		forceFluidLevelUpdate = false;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (!fluidCapability.isPresent())
			refreshCapability();
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return fluidCapability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void al_() {
		super.al_();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	public IFluidTank getTankInventory() {
		return tankInventory;
	}

	public int getTotalTankSize() {
		return width * width * height;
	}

	public static int getMaxSize() {
		return MAX_SIZE;
	}

	public static int getCapacityMultiplier() {
		return AllConfigs.SERVER.fluids.fluidTankCapacity.get() * 1000;
	}

	public static int getMaxHeight() {
		return AllConfigs.SERVER.fluids.fluidTankMaxHeight.get();
	}
	
	public InterpolatedChasingValue getFluidLevel() {
		return fluidLevel;
	}
	
	public void setFluidLevel(InterpolatedChasingValue fluidLevel) {
		this.fluidLevel = fluidLevel;
	}

}
