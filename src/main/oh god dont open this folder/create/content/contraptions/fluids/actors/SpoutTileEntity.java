package com.simibubi.create.content.contraptions.fluids.actors;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;

public class SpoutTileEntity extends SmartTileEntity {
	private static final boolean IS_TIC_LOADED = ModList.get()
		.isLoaded("tconstruct");
	private static final Class<?> CASTING_FLUID_HANDLER_CLASS;
	static {
		Class<?> testClass;
		try {
			testClass = Class.forName("slimeknights.tconstruct.library.smeltery.CastingFluidHandler");
		} catch (ClassNotFoundException e) {
			testClass = null;
		}
		CASTING_FLUID_HANDLER_CLASS = testClass;
	}

	public static final int FILLING_TIME = 20;

	protected BeltProcessingBehaviour beltProcessing;
	protected int processingTicks;
	protected boolean sendSplash;
	private boolean shouldAnimate = true;

	SmartFluidTankBehaviour tank;

	public SpoutTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		processingTicks = -1;
	}

	@Override
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().b(0, -2, 0);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		tank = SmartFluidTankBehaviour.single(this, 1000);
		behaviours.add(tank);

		beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
			.whileItemHeld(this::whenItemHeld);
		behaviours.add(beltProcessing);

	}

	protected ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (!FillingBySpout.canItemBeFilled(d, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		if (FillingBySpout.getRequiredAmountForItem(d, transported.stack, getCurrentFluidInTank()) == -1)
			return PASS;
		return HOLD;
	}

	protected ProcessingResult whenItemHeld(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		shouldAnimate = true;
		if (processingTicks != -1 && processingTicks != 5)
			return HOLD;
		if (!FillingBySpout.canItemBeFilled(d, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		FluidStack fluid = getCurrentFluidInTank();
		int requiredAmountForItem = FillingBySpout.getRequiredAmountForItem(d, transported.stack, fluid.copy());
		if (requiredAmountForItem == -1)
			return PASS;
		if (requiredAmountForItem > fluid.getAmount())
			return HOLD;

		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			notifyUpdate();
			return HOLD;
		}

		// Process finished
		ItemCooldownManager out = FillingBySpout.fillItem(d, requiredAmountForItem, transported.stack, fluid);
		if (!out.a()) {
			List<TransportedItemStack> outList = new ArrayList<>();
			TransportedItemStack held = null;
			TransportedItemStack result = transported.copy();
			result.stack = out;
			if (!transported.stack.a())
				held = transported.copy();
			outList.add(result);
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outList, held));
		}

		AllTriggers.triggerForNearbyPlayers(AllTriggers.SPOUT, d, e, 5);
		if (out.b() instanceof NameTagItem && !WrittenBookItem.a(out).isEmpty())
			AllTriggers.triggerForNearbyPlayers(AllTriggers.SPOUT_POTION, d, e, 5);
		
		tank.getPrimaryHandler()
			.setFluid(fluid);
		sendSplash = true;
		notifyUpdate();
		return HOLD;
	}

	private void processTicCastBlock() {
		if (!IS_TIC_LOADED || CASTING_FLUID_HANDLER_CLASS == null)
			return;
		if (d == null)
			return;
		IFluidHandler localTank = this.tank.getCapability()
			.orElse(null);
		if (localTank == null)
			return;
		FluidStack fluid = getCurrentFluidInTank();
		if (fluid.getAmount() == 0)
			return;
		BeehiveBlockEntity te = d.c(e.down(2));
		if (te == null)
			return;
		IFluidHandler handler = getFluidHandler(e.down(2), Direction.UP);
		if (!CASTING_FLUID_HANDLER_CLASS.isInstance(handler))
			return;
		if (handler.getTanks() != 1)
			return;
		if (!handler.isFluidValid(0, this.getCurrentFluidInTank()))
			return;
		FluidStack containedFluid = handler.getFluidInTank(0);
		if (!(containedFluid.isEmpty() || containedFluid.isFluidEqual(fluid)))
			return;
		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			notifyUpdate();
			return;
		}
		FluidStack drained = localTank.drain(144, IFluidHandler.FluidAction.SIMULATE);
		if (!drained.isEmpty()) {
			int filled = handler.fill(drained, IFluidHandler.FluidAction.SIMULATE);
			shouldAnimate = filled > 0;
			sendSplash = shouldAnimate;
			if (processingTicks == 5) {
				if (filled > 0) {
					drained = localTank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
					if (!drained.isEmpty()) {
						FluidStack fillStack = drained.copy();
						fillStack.setAmount(Math.min(drained.getAmount(), 6));
						drained.shrink(filled);
						fillStack.setAmount(filled);
						handler.fill(fillStack, IFluidHandler.FluidAction.EXECUTE);
					}
				}
				tank.getPrimaryHandler()
					.setFluid(fluid);
				this.notifyUpdate();
			}
		}
	}

	private FluidStack getCurrentFluidInTank() {
		return tank.getPrimaryHandler()
			.getFluid();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		compound.putInt("ProcessingTicks", processingTicks);
		if (sendSplash && clientPacket) {
			compound.putBoolean("Splash", true);
			sendSplash = false;
		}
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		processingTicks = compound.getInt("ProcessingTicks");
		if (!clientPacket)
			return;
		if (compound.contains("Splash"))
			spawnSplash(tank.getPrimaryTank()
				.getRenderedFluid());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != Direction.DOWN)
			return tank.getCapability()
				.cast();
		return super.getCapability(cap, side);
	}

	public void aj_() {
		super.aj_();
		processTicCastBlock();
		if (processingTicks >= 0)
			processingTicks--;
		if (processingTicks >= 8 && d.v && shouldAnimate)
			spawnProcessingParticles(tank.getPrimaryTank()
				.getRenderedFluid());
	}

	protected void spawnProcessingParticles(FluidStack fluid) {
		EntityHitResult vec = VecHelper.getCenterOf(e);
		vec = vec.a(0, 8 / 16f, 0);
		ParticleEffect particle = FluidFX.getFluidParticle(fluid);
		d.b(particle, vec.entity, vec.c, vec.d, 0, -.1f, 0);
	}

	protected static int SPLASH_PARTICLE_COUNT = 20;

	protected void spawnSplash(FluidStack fluid) {
		EntityHitResult vec = VecHelper.getCenterOf(e);
		vec = vec.a(0, 2 - 5 / 16f, 0);
		ParticleEffect particle = FluidFX.getFluidParticle(fluid);
		for (int i = 0; i < SPLASH_PARTICLE_COUNT; i++) {
			EntityHitResult m = VecHelper.offsetRandomly(EntityHitResult.a, d.t, 0.125f);
			m = new EntityHitResult(m.entity, Math.abs(m.c), m.d);
			d.b(particle, vec.entity, vec.c, vec.d, m.entity, m.c, m.d);
		}
	}

	@Nullable
	private IFluidHandler getFluidHandler(BlockPos pos, Direction direction) {
		if (this.d == null) {
			return null;
		} else {
			BeehiveBlockEntity te = this.d.c(pos);
			return te != null ? te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction)
				.orElse(null) : null;
		}
	}

	public int getCorrectedProcessingTicks() {
		if (shouldAnimate)
			return processingTicks;
		return -1;
	}

}
