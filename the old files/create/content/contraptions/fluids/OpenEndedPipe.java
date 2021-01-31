package com.simibubi.kinetic_api.content.contraptions.fluids;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.AllFluids;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.utility.BlockFace;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.effect.DamageModifierStatusEffect;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OpenEndedPipe extends FlowSource {

	GameMode world;
	BlockPos pos;
	Timer aoe;

	private OpenEndFluidHandler fluidHandler;
	private BlockPos outputPos;
	private boolean wasPulling;

	private FluidStack cachedFluid;
	private List<InstantStatusEffect> cachedEffects;

	public OpenEndedPipe(BlockFace face) {
		super(face);
		fluidHandler = new OpenEndFluidHandler();
		outputPos = face.getConnectedPos();
		pos = face.getPos();
		aoe = new Timer(outputPos).b(0, -1, 0);
		if (face.getFace() == Direction.DOWN)
			aoe = aoe.b(0, -1, 0);
	}

	@Override
	public void manageSource(GameMode world) {
		this.world = world;
	}

	private FluidStack removeFluidFromSpace(boolean simulate) {
		FluidStack empty = FluidStack.EMPTY;
		if (world == null)
			return empty;
		if (!world.isAreaLoaded(outputPos, 0))
			return empty;

		PistonHandler state = world.d_(outputPos);
		EmptyFluid fluidState = state.m();
		boolean waterlog = BlockHelper.hasBlockStateProperty(state, BambooLeaves.C);

		if (!waterlog && !state.c()
			.e())
			return empty;
		if (fluidState.c() || !fluidState.b())
			return empty;

		FluidStack stack = new FluidStack(fluidState.a(), 1000);

		if (simulate)
			return stack;
		
		AllTriggers.triggerForNearbyPlayers(AllTriggers.PIPE_SPILL, world, pos, 5);

		if (waterlog) {
			world.a(outputPos, state.a(BambooLeaves.C, false), 3);
			world.H()
				.a(outputPos, FlowableFluid.c, 1);
			return stack;
		}
		world.a(outputPos, fluidState.g()
			.a(LecternBlock.FACING, 14), 3);
		return stack;
	}

	private boolean provideFluidToSpace(FluidStack fluid, boolean simulate) {
		if (world == null)
			return false;
		if (!world.isAreaLoaded(outputPos, 0))
			return false;

		PistonHandler state = world.d_(outputPos);
		EmptyFluid fluidState = state.m();
		boolean waterlog = state.b(BambooLeaves.C);

		if (!waterlog && !state.c()
			.e())
			return false;
		if (fluid.isEmpty())
			return false;
		if (!FluidHelper.hasBlockState(fluid.getFluid())) {
			if (!simulate)
				applyEffects(world, fluid);
			return true;
		}

		if (!fluidState.c() && fluidState.a() != fluid.getFluid()) {
			FluidReactions.handlePipeSpillCollision(world, outputPos, fluid.getFluid(), fluidState);
			return false;
		}

		if (fluidState.b())
			return false;
		if (waterlog && fluid.getFluid() != FlowableFluid.c)
			return false;
		if (simulate)
			return true;

		if (world.k().d() && fluid.getFluid()
			.a(BlockTags.field_15481)) {
			int i = outputPos.getX();
			int j = outputPos.getY();
			int k = outputPos.getZ();
			world.a(null, i, j, k, MusicType.ej, SoundEvent.e, 0.5F,
				2.6F + (world.t.nextFloat() - world.t.nextFloat()) * 0.8F);
			return true;
		}
		
		AllTriggers.triggerForNearbyPlayers(AllTriggers.PIPE_SPILL, world, pos, 5);

		if (waterlog) {
			world.a(outputPos, state.a(BambooLeaves.C, true), 3);
			world.H()
				.a(outputPos, FlowableFluid.c, 1);
			return true;
		}
		world.a(outputPos, fluid.getFluid()
			.h()
			.g(), 3);
		return true;
	}

	private void applyEffects(GameMode world, FluidStack fluid) {
		if (!fluid.getFluid()
			.a(AllFluids.POTION.get())) {
			// other fx
			return;
		}

		if (cachedFluid == null || cachedEffects == null || !fluid.isFluidEqual(cachedFluid)) {
			FluidStack copy = fluid.copy();
			copy.setAmount(250);
			ItemCooldownManager bottle = PotionFluidHandler.fillBottle(new ItemCooldownManager(AliasedBlockItem.nw), fluid);
			cachedEffects = WrittenBookItem.a(bottle);
		}

		if (cachedEffects.isEmpty())
			return;

		List<SaddledComponent> list =
			this.world.a(SaddledComponent.class, aoe, SaddledComponent::eg);
		for (SaddledComponent livingentity : list) {
			for (InstantStatusEffect effectinstance : cachedEffects) {
				DamageModifierStatusEffect effect = effectinstance.a();
				if (effect.a()) {
					effect.a(null, null, livingentity, effectinstance.c(), 0.5D);
					continue;
				}
				livingentity.c(new InstantStatusEffect(effectinstance));
			}
		}

	}

	@Override
	public LazyOptional<IFluidHandler> provideHandler() {
		return LazyOptional.of(() -> fluidHandler);
	}

	public CompoundTag serializeNBT() {
		CompoundTag compound = new CompoundTag();
		fluidHandler.writeToNBT(compound);
		compound.putBoolean("Pulling", wasPulling);
		compound.put("Location", location.serializeNBT());
		return compound;
	}

	public static OpenEndedPipe fromNBT(CompoundTag compound) {
		OpenEndedPipe oep = new OpenEndedPipe(BlockFace.fromNBT(compound.getCompound("Location")));
		oep.fluidHandler.readFromNBT(compound);
		oep.wasPulling = compound.getBoolean("Pulling");
		return oep;
	}

	private class OpenEndFluidHandler extends FluidTank {

		public OpenEndFluidHandler() {
			super(1000);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			// Never allow being filled when a source is attached
			if (world == null)
				return 0;
			if (!world.isAreaLoaded(outputPos, 0))
				return 0;
			if (resource.isEmpty())
				return 0;
			if (!provideFluidToSpace(resource, true))
				return 0;

			if (!getFluid().isEmpty() && !getFluid().isFluidEqual(resource))
				setFluid(FluidStack.EMPTY);
			if (wasPulling)
				wasPulling = false;

			int fill = super.fill(resource, action);
			if (action.execute() && (getFluidAmount() == 1000 || !FluidHelper.hasBlockState(getFluid().getFluid()))
				&& provideFluidToSpace(getFluid(), false))
				setFluid(FluidStack.EMPTY);
			return fill;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return drainInner(resource.getAmount(), resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return drainInner(maxDrain, null, action);
		}

		private FluidStack drainInner(int amount, @Nullable FluidStack filter, FluidAction action) {
			FluidStack empty = FluidStack.EMPTY;
			boolean filterPresent = filter != null;

			if (world == null)
				return empty;
			if (!world.isAreaLoaded(outputPos, 0))
				return empty;
			if (amount == 0)
				return empty;
			if (amount > 1000) {
				amount = 1000;
				if (filterPresent)
					filter = FluidHelper.copyStackWithAmount(filter, amount);
			}

			if (!wasPulling)
				wasPulling = true;

			FluidStack drainedFromInternal = filterPresent ? super.drain(filter, action) : super.drain(amount, action);
			if (!drainedFromInternal.isEmpty())
				return drainedFromInternal;

			FluidStack drainedFromWorld = removeFluidFromSpace(action.simulate());
			if (drainedFromWorld.isEmpty())
				return FluidStack.EMPTY;
			if (filterPresent && !drainedFromWorld.isFluidEqual(filter))
				return FluidStack.EMPTY;

			int remainder = drainedFromWorld.getAmount() - amount;
			drainedFromWorld.setAmount(amount);

			if (!action.simulate() && remainder > 0) {
				if (!getFluid().isEmpty() && !getFluid().isFluidEqual(drainedFromWorld))
					setFluid(FluidStack.EMPTY);
				super.fill(FluidHelper.copyStackWithAmount(drainedFromWorld, remainder), FluidAction.EXECUTE);
			}
			return drainedFromWorld;
		}

	}

	@Override
	public boolean isEndpoint() {
		return true;
	}

}
