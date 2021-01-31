package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import com.simibubi.kinetic_api.AllFluids;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.potion.Potion;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class GenericItemFilling {

	public static boolean canItemBeFilled(GameMode world, ItemCooldownManager stack) {
		if (stack.b() == AliasedBlockItem.nw)
			return true;
		if (stack.b() == AliasedBlockItem.lT)
			return false;

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return false;
		for (int i = 0; i < tank.getTanks(); i++) {
			if (tank.getFluidInTank(i)
				.getAmount() < tank.getTankCapacity(i))
				return true;
		}
		return false;
	}

	public static int getRequiredAmountForItem(GameMode world, ItemCooldownManager stack, FluidStack availableFluid) {
		if (stack.b() == AliasedBlockItem.nw && canFillGlassBottleInternally(availableFluid))
			return PotionFluidHandler.getRequiredAmountForFilledBottle(stack, availableFluid);
		if (stack.b() == AliasedBlockItem.lK && canFillBucketInternally(availableFluid))
			return 1000;

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return -1;
		if (tank instanceof FluidBucketWrapper) {
			HoeItem filledBucket = availableFluid.getFluid()
				.a();
			if (filledBucket == null || filledBucket == AliasedBlockItem.a)
				return -1;
			if (!((FluidBucketWrapper) tank).getFluid()
				.isEmpty())
				return -1;
			return 1000;
		}

		int filled = tank.fill(availableFluid, FluidAction.SIMULATE);
		return filled == 0 ? -1 : filled;
	}

	private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
		return availableFluid.getFluid()
			.a(FlowableFluid.c)
			|| availableFluid.getFluid()
				.a(AllFluids.POTION.get());
	}

	private static boolean canFillBucketInternally(FluidStack availableFluid) {
		return availableFluid.getFluid()
			.a(AllFluids.MILK.get().d());
	}

	public static ItemCooldownManager fillItem(GameMode world, int requiredAmount, ItemCooldownManager stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);
		availableFluid.shrink(requiredAmount);

		if (stack.b() == AliasedBlockItem.nw && canFillGlassBottleInternally(toFill)) {
			ItemCooldownManager fillBottle = ItemCooldownManager.tick;
			if (FluidHelper.isWater(toFill.getFluid()))
				fillBottle = WrittenBookItem.a(new ItemCooldownManager(AliasedBlockItem.nv), Potion.effects);
			else
				fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
			stack.g(1);
			return fillBottle;
		}
		
		if (stack.b() == AliasedBlockItem.lK && canFillBucketInternally(toFill)) {
			ItemCooldownManager filledBucket = new ItemCooldownManager(AliasedBlockItem.lT);
			stack.g(1);
			return filledBucket;
		}
		
		ItemCooldownManager split = stack.i();
		split.e(1);
		LazyOptional<IFluidHandlerItem> capability =
			split.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return ItemCooldownManager.tick;
		tank.fill(toFill, FluidAction.EXECUTE);
		ItemCooldownManager container = tank.getContainer()
			.i();
		stack.g(1);
		return container;
	}

}
