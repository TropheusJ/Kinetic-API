package com.simibubi.create.content.contraptions.components.millstone;

import java.util.Optional;
import afj;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MillstoneTileEntity extends KineticTileEntity {

	public ItemStackHandler inputInv;
	public ItemStackHandler outputInv;
	public LazyOptional<IItemHandler> capability;
	public int timer;
	private MillingRecipe lastRecipe;

	public MillstoneTileEntity(BellBlockEntity<? extends MillstoneTileEntity> type) {
		super(type);
		inputInv = new ItemStackHandler(1);
		outputInv = new ItemStackHandler(9);
		capability = LazyOptional.of(MillstoneInventoryHandler::new);
	}

	@Override
	public void aj_() {
		super.aj_();

		if (getSpeed() == 0)
			return;
		for (int i = 0; i < outputInv.getSlots(); i++)
			if (outputInv.getStackInSlot(i)
				.E() == outputInv.getSlotLimit(i))
				return;

		if (timer > 0) {
			timer -= getProcessingSpeed();

			if (d.v) {
				spawnParticles();
				return;
			}
			if (timer <= 0)
				process();
			return;
		}

		if (inputInv.getStackInSlot(0)
			.a())
			return;

		RecipeWrapper inventoryIn = new RecipeWrapper(inputInv);
		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, d)) {
			Optional<MillingRecipe> recipe = AllRecipeTypes.MILLING.find(inventoryIn, d);
			if (!recipe.isPresent()) {
				timer = 100;
				sendData();
			} else {
				lastRecipe = recipe.get();
				timer = lastRecipe.getProcessingDuration();
				sendData();
			}
			return;
		}

		timer = lastRecipe.getProcessingDuration();
		sendData();
	}

	@Override
	public void al_() {
		super.al_();
		capability.invalidate();
	}
	
	private void process() {
		RecipeWrapper inventoryIn = new RecipeWrapper(inputInv);

		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, d)) {
			Optional<MillingRecipe> recipe = AllRecipeTypes.MILLING.find(inventoryIn, d);
			if (!recipe.isPresent())
				return;
			lastRecipe = recipe.get();
		}

		ItemCooldownManager stackInSlot = inputInv.getStackInSlot(0);
		stackInSlot.g(1);
		inputInv.setStackInSlot(0, stackInSlot);
		lastRecipe.rollResults()
			.forEach(stack -> ItemHandlerHelper.insertItemStacked(outputInv, stack, false));
		sendData();
		X_();
	}

	public void spawnParticles() {
		ItemCooldownManager stackInSlot = inputInv.getStackInSlot(0);
		if (stackInSlot.a())
			return;

		ItemStackParticleEffect data = new ItemStackParticleEffect(ParticleTypes.ITEM, stackInSlot);
		float angle = d.t.nextFloat() * 360;
		EntityHitResult offset = new EntityHitResult(0, 0, 0.5f);
		offset = VecHelper.rotate(offset, angle, Axis.Y);
		EntityHitResult target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y);

		EntityHitResult center = offset.e(VecHelper.getCenterOf(e));
		target = VecHelper.offsetRandomly(target.d(offset), d.t, 1 / 128f);
		d.addParticle(data, center.entity, center.c, center.d, target.entity, target.c, target.d);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Timer", timer);
		compound.put("InputInventory", inputInv.serializeNBT());
		compound.put("OutputInventory", outputInv.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		timer = compound.getInt("Timer");
		inputInv.deserializeNBT(compound.getCompound("InputInventory"));
		outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
		super.fromTag(state, compound, clientPacket);
	}

	public int getProcessingSpeed() {
		return afj.a((int) Math.abs(getSpeed() / 16f), 1, 512);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return capability.cast();
		return super.getCapability(cap, side);
	}

	private boolean canProcess(ItemCooldownManager stack) {
		ItemStackHandler tester = new ItemStackHandler(1);
		tester.setStackInSlot(0, stack);
		RecipeWrapper inventoryIn = new RecipeWrapper(tester);

		if (lastRecipe != null && lastRecipe.matches(inventoryIn, d))
			return true;
		return AllRecipeTypes.MILLING.find(inventoryIn, d)
			.isPresent();
	}

	private class MillstoneInventoryHandler extends CombinedInvWrapper {

		public MillstoneInventoryHandler() {
			super(inputInv, outputInv);
		}

		@Override
		public boolean isItemValid(int slot, ItemCooldownManager stack) {
			if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)))
				return false;
			return canProcess(stack) && super.isItemValid(slot, stack);
		}

		@Override
		public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
			if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)))
				return stack;
			if (!isItemValid(slot, stack))
				return stack;
			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemCooldownManager extractItem(int slot, int amount, boolean simulate) {
			if (inputInv == getHandlerFromIndex(getIndexForSlot(slot)))
				return ItemCooldownManager.tick;
			return super.extractItem(slot, amount, simulate);
		}

	}

}
